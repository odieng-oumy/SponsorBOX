package com.sponsorbox.repositories;

import com.sponsorbox.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByDealIdOrderBySentAtAsc(Long dealId);
}
