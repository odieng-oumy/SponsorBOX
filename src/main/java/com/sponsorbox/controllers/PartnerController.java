package com.sponsorbox.controllers;

import com.sponsorbox.models.PartnerCompany;
import com.sponsorbox.services.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> registerCompany(@RequestBody Map<String, Object> request,
                                              Authentication authentication) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long authenticatedUserId = (Long) authentication.getPrincipal();

        if (!userId.equals(authenticatedUserId)) {
            return ResponseEntity.status(403).body(Map.of("message", "Acces refuse"));
        }

        String companyName = request.get("companyName").toString();
        String siret = request.get("siret").toString();
        String officialEmail = request.get("officialEmail").toString();

        PartnerCompany company = partnerService.createCompany(userId, companyName, siret, officialEmail);
        return ResponseEntity.ok(company);
    }

    @PostMapping("/{partnerId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartnerCompany> verifyPartner(@PathVariable Long partnerId) {
        return ResponseEntity.ok(partnerService.verifyPartner(partnerId));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('PARTNER', 'ADMIN')")
    public ResponseEntity<?> getByUserId(@PathVariable Long userId, Authentication authentication) {
        Long authenticatedUserId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!userId.equals(authenticatedUserId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "Acces refuse"));
        }

        return ResponseEntity.ok(partnerService.getByUserId(userId));
    }
}
