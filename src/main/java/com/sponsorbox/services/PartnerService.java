package com.sponsorbox.services;

import com.sponsorbox.models.CertificationStatus;
import com.sponsorbox.models.PartnerCompany;
import com.sponsorbox.models.User;
import com.sponsorbox.repositories.PartnerCompanyRepository;
import com.sponsorbox.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerCompanyRepository partnerCompanyRepository;
    private final UserRepository userRepository;

    public PartnerCompany createCompany(Long userId, String companyName, String siret, String officialEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (partnerCompanyRepository.existsBySiret(siret)) {
            throw new RuntimeException("Ce SIRET est deja enregistre");
        }

        PartnerCompany company = PartnerCompany.builder()
                .user(user)
                .companyName(companyName)
                .siret(siret)
                .officialEmail(officialEmail)
                .build();

        return partnerCompanyRepository.save(company);
    }

    public PartnerCompany verifyPartner(Long partnerId) {
        PartnerCompany company = partnerCompanyRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));

        boolean isGenericEmail = company.getOfficialEmail().contains("@gmail.com")
                || company.getOfficialEmail().contains("@yahoo.com")
                || company.getOfficialEmail().contains("@hotmail.com");

        if (isGenericEmail) {
            company.setCertificationStatus(CertificationStatus.REJECTED);
        } else {
            company.setCertificationStatus(CertificationStatus.VERIFIED);
        }

        return partnerCompanyRepository.save(company);
    }

    public PartnerCompany getByUserId(Long userId) {
        return partnerCompanyRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));
    }
}
