package com.sponsorbox.controllers;

import com.sponsorbox.models.Deal;
import com.sponsorbox.services.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<Deal> createDeal(@RequestBody Map<String, Object> request) {
        Long creatorId = Long.valueOf(request.get("creatorId").toString());
        Long partnerId = Long.valueOf(request.get("partnerId").toString());
        String title = request.get("title").toString();
        String brief = request.getOrDefault("brief", "").toString();
        double amount = Double.parseDouble(request.get("amount").toString());
        LocalDateTime deadline = request.containsKey("deadline")
                ? LocalDateTime.parse(request.get("deadline").toString())
                : null;

        Deal deal = dealService.createDeal(creatorId, partnerId, title, brief, amount, deadline);
        return ResponseEntity.ok(deal);
    }

    @PostMapping("/{dealId}/fund-escrow")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> fundEscrow(@PathVariable Long dealId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        dealService.verifyPartnerOwnership(dealId, userId);
        return ResponseEntity.ok(dealService.fundEscrow(dealId));
    }

    @PostMapping("/{dealId}/accept")
    @PreAuthorize("hasRole('CREATOR')")
    public ResponseEntity<?> acceptDeal(@PathVariable Long dealId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        dealService.verifyCreatorOwnership(dealId, userId);
        return ResponseEntity.ok(dealService.acceptDeal(dealId));
    }

    @PostMapping("/{dealId}/submit-video")
    @PreAuthorize("hasRole('CREATOR')")
    public ResponseEntity<?> submitVideo(@PathVariable Long dealId,
                                          @RequestBody Map<String, String> request,
                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        dealService.verifyCreatorOwnership(dealId, userId);
        return ResponseEntity.ok(dealService.submitVideo(dealId, request.get("tiktokVideoUrl")));
    }

    @PostMapping("/{dealId}/complete")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> completeDeal(@PathVariable Long dealId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        dealService.verifyPartnerOwnership(dealId, userId);
        return ResponseEntity.ok(dealService.completeDeal(dealId));
    }

    @GetMapping("/{dealId}")
    @PreAuthorize("hasAnyRole('CREATOR', 'PARTNER', 'ADMIN')")
    public ResponseEntity<?> getDeal(@PathVariable Long dealId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Deal deal = dealService.getDealById(dealId);

        if (!isAdmin) {
            dealService.verifyDealParticipant(dealId, userId);
        }

        return ResponseEntity.ok(deal);
    }

    @GetMapping("/creator/{creatorId}")
    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    public ResponseEntity<?> getDealsByCreator(@PathVariable Long creatorId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            dealService.verifyCreatorIdentity(creatorId, userId);
        }

        return ResponseEntity.ok(dealService.getDealsByCreator(creatorId));
    }

    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasAnyRole('PARTNER', 'ADMIN')")
    public ResponseEntity<?> getDealsByPartner(@PathVariable Long partnerId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            dealService.verifyPartnerIdentity(partnerId, userId);
        }

        return ResponseEntity.ok(dealService.getDealsByPartner(partnerId));
    }
}
