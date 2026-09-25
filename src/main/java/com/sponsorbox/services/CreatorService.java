package com.sponsorbox.services;

import com.sponsorbox.models.*;
import com.sponsorbox.repositories.CreatorProfileRepository;
import com.sponsorbox.repositories.SuccessShowcaseRepository;
import com.sponsorbox.repositories.TestimonialRepository;
import com.sponsorbox.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatorService {

    private final CreatorProfileRepository creatorProfileRepository;
    private final SuccessShowcaseRepository successShowcaseRepository;
    private final TestimonialRepository testimonialRepository;
    private final UserRepository userRepository;

    public CreatorProfile createProfile(Long userId, String tiktokHandle, int followersCount,
                                         double engagementRate, String bio, String category) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String code = "SB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        CreatorProfile profile = CreatorProfile.builder()
                .user(user)
                .tiktokHandle(tiktokHandle)
                .followersCount(followersCount)
                .engagementRate(engagementRate)
                .bio(bio)
                .category(category)
                .eligible(followersCount >= 5000)
                .verificationCode(code)
                .certificationStatus(CertificationStatus.PENDING)
                .build();

        return creatorProfileRepository.save(profile);
    }

    public CreatorProfile verifyCreator(Long creatorId, int realFollowersCount, double realEngagementRate) {
        CreatorProfile profile = creatorProfileRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
        profile.setFollowersCount(realFollowersCount);
        profile.setEngagementRate(realEngagementRate);
        profile.setEligible(realFollowersCount >= 5000);
        profile.setCertificationStatus(CertificationStatus.VERIFIED);
        return creatorProfileRepository.save(profile);
    }

    public CreatorProfile rejectCreator(Long creatorId) {
        CreatorProfile profile = creatorProfileRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
        profile.setCertificationStatus(CertificationStatus.REJECTED);
        profile.setEligible(false);
        return creatorProfileRepository.save(profile);
    }

    public List<CreatorProfile> getPendingCreators() {
        return creatorProfileRepository.findByCertificationStatus(CertificationStatus.PENDING);
    }

    public CreatorProfile updateProfile(Long userId, String tiktokHandle, String bio, String category) {
        CreatorProfile profile = creatorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));

        if (tiktokHandle != null) profile.setTiktokHandle(tiktokHandle);
        if (bio != null) profile.setBio(bio);
        if (category != null) profile.setCategory(category);

        return creatorProfileRepository.save(profile);
    }

    public boolean checkEligibility(String tiktokHandle, int followersCount) {
        return followersCount >= 5000;
    }

    public CreatorProfile getProfileByUserId(Long userId) {
        return creatorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil createur introuvable"));
    }

    public List<CreatorProfile> getEligibleCreators() {
        return creatorProfileRepository.findByCertificationStatus(CertificationStatus.VERIFIED);
    }

    public List<CreatorProfile> getAllCreators() {
        return creatorProfileRepository.findAll();
    }

    public SuccessShowcase addSuccess(Long creatorId, String title, String description, String brandName,
                                       String tiktokVideoUrl, int viewsCount, int likesCount,
                                       int sharesCount, int commentsCount, String resultSummary) {
        CreatorProfile creator = creatorProfileRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));

        SuccessShowcase showcase = SuccessShowcase.builder()
                .creator(creator)
                .title(title)
                .description(description)
                .brandName(brandName)
                .tiktokVideoUrl(tiktokVideoUrl)
                .viewsCount(viewsCount)
                .likesCount(likesCount)
                .sharesCount(sharesCount)
                .commentsCount(commentsCount)
                .resultSummary(resultSummary)
                .build();

        return successShowcaseRepository.save(showcase);
    }

    public List<SuccessShowcase> getSuccesses(Long creatorId) {
        return successShowcaseRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId);
    }

    public void deleteSuccess(Long successId) {
        successShowcaseRepository.deleteById(successId);
    }

    public void verifySuccessOwnership(Long successId, Long creatorId) {
        SuccessShowcase showcase = successShowcaseRepository.findById(successId)
                .orElseThrow(() -> new RuntimeException("Showcase introuvable"));
        if (!showcase.getCreator().getId().equals(creatorId)) {
            throw new RuntimeException("Acces refuse : ce showcase ne vous appartient pas");
        }
    }

    public Testimonial addTestimonial(Long showcaseId, Long authorId, String authorName,
                                       String authorCompany, String content, int rating) {
        SuccessShowcase showcase = successShowcaseRepository.findById(showcaseId)
                .orElseThrow(() -> new RuntimeException("Showcase introuvable"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Testimonial testimonial = Testimonial.builder()
                .showcase(showcase)
                .author(author)
                .authorName(authorName)
                .authorCompany(authorCompany)
                .content(content)
                .rating(rating)
                .build();

        return testimonialRepository.save(testimonial);
    }

    public List<Testimonial> getTestimonials(Long showcaseId) {
        return testimonialRepository.findByShowcaseIdOrderByCreatedAtDesc(showcaseId);
    }

    public List<Testimonial> getAllTestimonialsForCreator(Long creatorId) {
        return testimonialRepository.findByShowcaseCreatorIdOrderByCreatedAtDesc(creatorId);
    }

    public void deleteTestimonial(Long testimonialId) {
        testimonialRepository.deleteById(testimonialId);
    }

    public void verifyTestimonialOwnership(Long testimonialId, Long creatorId) {
        Testimonial testimonial = testimonialRepository.findById(testimonialId)
                .orElseThrow(() -> new RuntimeException("Temoignage introuvable"));
        if (!testimonial.getShowcase().getCreator().getId().equals(creatorId)) {
            throw new RuntimeException("Acces refuse : ce temoignage ne vous appartient pas");
        }
    }
}
