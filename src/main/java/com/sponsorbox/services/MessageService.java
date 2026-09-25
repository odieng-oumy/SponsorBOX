package com.sponsorbox.services;

import com.sponsorbox.models.Deal;
import com.sponsorbox.models.Message;
import com.sponsorbox.models.User;
import com.sponsorbox.repositories.DealRepository;
import com.sponsorbox.repositories.MessageRepository;
import com.sponsorbox.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;

    public Message sendMessage(Long dealId, Long senderId, String content) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal introuvable"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Message message = Message.builder()
                .deal(deal)
                .sender(sender)
                .content(content)
                .build();

        return messageRepository.save(message);
    }

    public List<Message> getMessagesByDeal(Long dealId) {
        return messageRepository.findByDealIdOrderBySentAtAsc(dealId);
    }
}
