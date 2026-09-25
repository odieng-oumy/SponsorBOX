package com.sponsorbox.controllers;

import com.sponsorbox.models.Message;
import com.sponsorbox.services.DealService;
import com.sponsorbox.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final DealService dealService;

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('CREATOR', 'PARTNER')")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> request,
                                          Authentication authentication) {
        Long dealId = Long.valueOf(request.get("dealId").toString());
        Long senderId = (Long) authentication.getPrincipal();
        String content = request.get("content").toString();

        dealService.verifyDealParticipant(dealId, senderId);

        return ResponseEntity.ok(messageService.sendMessage(dealId, senderId, content));
    }

    @GetMapping("/deal/{dealId}")
    @PreAuthorize("hasAnyRole('CREATOR', 'PARTNER', 'ADMIN')")
    public ResponseEntity<?> getMessages(@PathVariable Long dealId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            dealService.verifyDealParticipant(dealId, userId);
        }

        return ResponseEntity.ok(messageService.getMessagesByDeal(dealId));
    }
}
