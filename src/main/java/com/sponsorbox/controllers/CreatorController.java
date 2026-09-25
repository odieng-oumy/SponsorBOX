package com.sponsorbox.controllers;

import com.sponsorbox.models.CreatorProfile;
import com.sponsorbox.models.SuccessShowcase;
import com.sponsorbox.models.Testimonial;
import com.sponsorbox.services.CreatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/creators")
@RequiredArgsConstructor
public class CreatorController {

    private final CreatorService creatorService;

    @PostMapping("/profile")
    @PreAuthorize("hasRole('CREATOR')")
    public ResponseEntity<CreatorProfile> createProfile(@RequestBody Map<String, Object> request,
                                                         Authentication authentication) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long authenticatedUserId = (Long) authentication.getPrincipal();

        if (!userId.equals(authenticatedUserId)) {
            return ResponseEntity.status(403).build();
        }

        String tiktokHandle = request.get("tiktokHandle").toString();
        int followersCount = Integer.parseInt(request.get("followersCount").toString());
        double engagementRate = Double.parseDouble(request.get("engagementRate").toString());
        String bio = request.getOrDefault("bio", "").toString();
        String category = request.getOrDefault("category", "").toString();

        CreatorProfile profile = creatorService.createProfile(userId, tiktokHandle, followersCount, engagementRate, bio, category);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile/{userId}")
    @PreAuthorize("hasRole('CREATOR')")
    public ResponseEntity<CreatorProfile> updateProfile(@PathVariable Long userId,
                                                         @RequestBody Map<String, String> request,
                                                         Authentication authentication) {
        Long authenticatedUserId = (Long) authentication.getPrincipal();
        if (!userId.equals(authenticatedUserId)) {
            return ResponseEntity.status(403).build();
        }

        CreatorProfile profile = creatorService.updateProfile(
                userId, request.get("tiktokHandle"), request.get("bio"), request.get("category")
        );
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/verify-tiktok")
    public ResponseEntity<?> verifyTiktok(@RequestParam String handle, @RequestParam int followers) {
        boolean eligible = creatorService.checkEligibility(handle, followers);
        return ResponseEntity.ok(Map.of(
                "handle", handle, "followers", followers, "eligible", eligible,
                "message", eligible ? "Compte eligible !" : "Il vous manque " + (5000 - followers) + " abonnes."
        ));
    }

    @GetMapping("/eligible")
    public ResponseEntity<List<CreatorProfile>> getEligibleCreators() {
        return ResponseEntity.ok(creatorService.getEligibleCreators());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CreatorProfile>> getPendingCreators() {
        return ResponseEntity.ok(creatorService.getPendingCreators());
    }

    @PostMapping("/{creatorId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> verifyCreator(@PathVariable Long creatorId,
                                            @RequestBody Map<String, Object> request) {
        int realFollowers = Integer.parseInt(request.get("followersCount").toString());
        double realEngagement = Double.parseDouble(request.getOrDefault("engagementRate", "0").toString());

        CreatorProfile profile = creatorService.verifyCreator(creatorId, realFollowers, realEngagement);
        return ResponseEntity.ok(Map.of(
                "message", "Createur verifie avec succes",
                "creatorId", profile.getId(),
                "tiktokHandle", profile.getTiktokHandle(),
                "followersCount", profile.getFollowersCount(),
                "engagementRate", profile.getEngagementRate(),
                "certificationStatus", profile.getCertificationStatus().name()
        ));
    }

    @PostMapping("/{creatorId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectCreator(@PathVariable Long creatorId) {
        CreatorProfile profile = creatorService.rejectCreator(creatorId);
        return ResponseEntity.ok(Map.of(
                "message", "Createur rejete",
                "creatorId", profile.getId(),
                "certificationStatus", profile.getCertificationStatus().name()
        ));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('CREATOR', 'PARTNER', 'ADMIN')")
    public ResponseEntity<CreatorProfile> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(creatorService.getProfileByUserId(userId));
    }

    // ===== Vitrine des succes =====

    @PostMapping("/{creatorId}/successes")
    @PreAuthorize("hasRole('CREATOR')")
    public ResponseEntity<?> addSuccess(@PathVariable Long creatorId,
                                        @RequestBody Map<String, Object> request,
                                        Authentication authentication) {
        Long authenticatedUserId = (Long) authentication.getPrincipal();
        CreatorProfile profile = creatorService.getProfileByUserId(authenticatedUserId);
        if (!profile.getId().equals(creatorId)) {
            return ResponseEntity.status(403).body(Map.of("message", "Acces refuse : ce n'est pas votre profil"));
        }

        SuccessShowcase showcase = creatorService.addSuccess(
                creatorId,
                request.get("title").toString(),
                request.getOrDefault("description", "").toString(),
                request.getOrDefault("brandName", "").toString(),
                request.getOrDefault("tiktokVideoUrl", "").toString(),
                Integer.parseInt(request.getOrDefault("viewsCount", "0").toString()),
                Integer.parseInt(request.getOrDefault("likesCount", "0").toString()),
                Integer.parseInt(request.getOrDefault("sharesCount", "0").toString()),
                Integer.parseInt(request.getOrDefault("commentsCount", "0").toString()),
                request.getOrDefault("resultSummary", "").toString()
        );
        return ResponseEntity.ok(showcase);
    }

    @GetMapping("/{creatorId}/successes")
    @PreAuthorize("hasAnyRole('CREATOR', 'PARTNER', 'ADMIN')")
    public ResponseEntity<List<SuccessShowcase>> getSuccesses(@PathVariable Long creatorId) {
        return ResponseEntity.ok(creatorService.getSuccesses(creatorId));
    }

    @DeleteMapping("/successes/{successId}")
    @PreAuthorize("hasRole('CREATOR')")
    public ResponseEntity<?> deleteSuccess(@PathVariable Long successId, Authentication authentication) {
        Long authenticatedUserId = (Long) authentication.getPrincipal();
        CreatorProfile profile = creatorService.getProfileByUserId(authenticatedUserId);
        creatorService.verifySuccessOwnership(successId, profile.getId());

        creatorService.deleteSuccess(successId);
        return ResponseEntity.ok(Map.of("message", "Succes supprime"));
    }

    // ===== Temoignages =====

    @PostMapping("/successes/{showcaseId}/testimonials")
    @PreAuthorize("hasAnyRole('PARTNER', 'ADMIN')")
    public ResponseEntity<Testimonial> addTestimonial(@PathVariable Long showcaseId,
                                                       @RequestBody Map<String, Object> request,
                                                       Authentication authentication) {
        Long authorId = (Long) authentication.getPrincipal();
        String authorName = request.getOrDefault("authorName", "").toString();
        String authorCompany = request.getOrDefault("authorCompany", "").toString();
        String content = request.get("content").toString();
        int rating = Integer.parseInt(request.getOrDefault("rating", "5").toString());

        Testimonial testimonial = creatorService.addTestimonial(showcaseId, authorId, authorName, authorCompany, content, rating);
        return ResponseEntity.ok(testimonial);
    }

    @GetMapping("/successes/{showcaseId}/testimonials")
    @PreAuthorize("hasAnyRole('CREATOR', 'PARTNER', 'ADMIN')")
    public ResponseEntity<List<Testimonial>> getTestimonials(@PathVariable Long showcaseId) {
        return ResponseEntity.ok(creatorService.getTestimonials(showcaseId));
    }

    @GetMapping("/{creatorId}/testimonials")
    @PreAuthorize("hasAnyRole('CREATOR', 'PARTNER', 'ADMIN')")
    public ResponseEntity<List<Testimonial>> getAllTestimonials(@PathVariable Long creatorId) {
        return ResponseEntity.ok(creatorService.getAllTestimonialsForCreator(creatorId));
    }

    @DeleteMapping("/testimonials/{testimonialId}")
    @PreAuthorize("hasRole('CREATOR')")
    public ResponseEntity<?> deleteTestimonial(@PathVariable Long testimonialId, Authentication authentication) {
        Long authenticatedUserId = (Long) authentication.getPrincipal();
        CreatorProfile profile = creatorService.getProfileByUserId(authenticatedUserId);
        creatorService.verifyTestimonialOwnership(testimonialId, profile.getId());

        creatorService.deleteTestimonial(testimonialId);
        return ResponseEntity.ok(Map.of("message", "Temoignage supprime"));
    }
}
