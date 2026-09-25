package com.sponsorbox.repositories;

import com.sponsorbox.models.SuccessShowcase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuccessShowcaseRepository extends JpaRepository<SuccessShowcase, Long> {

    List<SuccessShowcase> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
}
