package com.sponsorbox.repositories;

import com.sponsorbox.models.CertificationStatus;
import com.sponsorbox.models.CreatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, Long> {

    Optional<CreatorProfile> findByUserId(Long userId);

    Optional<CreatorProfile> findByTiktokHandle(String tiktokHandle);

    List<CreatorProfile> findByEligibleTrue();

    List<CreatorProfile> findByCertificationStatus(CertificationStatus status);
}
