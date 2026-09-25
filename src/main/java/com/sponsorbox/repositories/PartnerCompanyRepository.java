package com.sponsorbox.repositories;

import com.sponsorbox.models.PartnerCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerCompanyRepository extends JpaRepository<PartnerCompany, Long> {

    Optional<PartnerCompany> findByUserId(Long userId);

    Optional<PartnerCompany> findBySiret(String siret);

    boolean existsBySiret(String siret);
}
