package com.sponsorbox.repositories;

import com.sponsorbox.models.Deal;
import com.sponsorbox.models.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Long> {

    List<Deal> findByCreatorId(Long creatorId);

    List<Deal> findByPartnerId(Long partnerId);

    List<Deal> findByStatus(DealStatus status);

    List<Deal> findByCreatorIdAndStatus(Long creatorId, DealStatus status);
}
