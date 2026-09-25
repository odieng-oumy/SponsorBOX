package com.sponsorbox.services;

import com.sponsorbox.models.*;
import com.sponsorbox.repositories.CreatorProfileRepository;
import com.sponsorbox.repositories.DealRepository;
import com.sponsorbox.repositories.PartnerCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;
    private final CreatorProfileRepository creatorProfileRepository;
    private final PartnerCompanyRepository partnerCompanyRepository;

    public Deal createDeal(Long creatorId, Long partnerId, String title, String brief, double amount, LocalDateTime deadline) {
        CreatorProfile creator = creatorProfileRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Createur introuvable"));

        if (!creator.isEligible()) {
            throw new RuntimeException("Ce createur n'est pas eligible (moins de 5 000 abonnes)");
        }

        PartnerCompany partner = partnerCompanyRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));

        Deal deal = Deal.builder()
                .creator(creator)
                .partner(partner)
                .title(title)
                .brief(brief)
                .amount(amount)
                .deadline(deadline)
                .build();

        return dealRepository.save(deal);
    }

    public Deal fundEscrow(Long dealId) {
        Deal deal = getDealById(dealId);
        deal.setStatus(DealStatus.ESCROW_FUNDED);
        return dealRepository.save(deal);
    }

    public Deal acceptDeal(Long dealId) {
        Deal deal = getDealById(dealId);
        if (deal.getStatus() != DealStatus.ESCROW_FUNDED) {
            throw new RuntimeException("Les fonds doivent etre sequestres avant d'accepter");
        }
        deal.setStatus(DealStatus.ACCEPTED);
        return dealRepository.save(deal);
    }

    public Deal submitVideo(Long dealId, String tiktokVideoUrl) {
        Deal deal = getDealById(dealId);
        if (deal.getStatus() != DealStatus.ACCEPTED) {
            throw new RuntimeException("Le deal doit etre accepte avant de soumettre la video");
        }
        deal.setTiktokVideoUrl(tiktokVideoUrl);
        deal.setStatus(DealStatus.VIDEO_SUBMITTED);
        return dealRepository.save(deal);
    }

    public Deal completeDeal(Long dealId) {
        Deal deal = getDealById(dealId);
        if (deal.getStatus() != DealStatus.VIDEO_SUBMITTED) {
            throw new RuntimeException("La video doit etre soumise avant de completer le deal");
        }
        deal.setStatus(DealStatus.COMPLETED);
        return dealRepository.save(deal);
    }

    public Deal getDealById(Long id) {
        return dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal introuvable"));
    }

    public List<Deal> getDealsByCreator(Long creatorId) {
        return dealRepository.findByCreatorId(creatorId);
    }

    public List<Deal> getDealsByPartner(Long partnerId) {
        return dealRepository.findByPartnerId(partnerId);
    }

    public void verifyCreatorOwnership(Long dealId, Long userId) {
        Deal deal = getDealById(dealId);
        CreatorProfile creator = creatorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil createur introuvable"));
        if (!deal.getCreator().getId().equals(creator.getId())) {
            throw new RuntimeException("Acces refuse : ce deal ne vous appartient pas");
        }
    }

    public void verifyPartnerOwnership(Long dealId, Long userId) {
        Deal deal = getDealById(dealId);
        PartnerCompany partner = partnerCompanyRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil partenaire introuvable"));
        if (!deal.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Acces refuse : ce deal ne vous appartient pas");
        }
    }

    public void verifyDealParticipant(Long dealId, Long userId) {
        Deal deal = getDealById(dealId);
        boolean isCreator = creatorProfileRepository.findByUserId(userId)
                .map(c -> c.getId().equals(deal.getCreator().getId()))
                .orElse(false);
        boolean isPartner = partnerCompanyRepository.findByUserId(userId)
                .map(p -> p.getId().equals(deal.getPartner().getId()))
                .orElse(false);
        if (!isCreator && !isPartner) {
            throw new RuntimeException("Acces refuse : vous n'etes pas participant de ce deal");
        }
    }

    public void verifyCreatorIdentity(Long creatorId, Long userId) {
        CreatorProfile creator = creatorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil createur introuvable"));
        if (!creator.getId().equals(creatorId)) {
            throw new RuntimeException("Acces refuse : ce n'est pas votre profil");
        }
    }

    public void verifyPartnerIdentity(Long partnerId, Long userId) {
        PartnerCompany partner = partnerCompanyRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil partenaire introuvable"));
        if (!partner.getId().equals(partnerId)) {
            throw new RuntimeException("Acces refuse : ce n'est pas votre profil");
        }
    }
}
