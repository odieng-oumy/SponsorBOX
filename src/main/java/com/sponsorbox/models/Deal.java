package com.sponsorbox.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private CreatorProfile creator;

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private PartnerCompany partner;

    @NotBlank(message = "Le titre de la campagne est obligatoire")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String brief;

    @Positive(message = "Le montant doit etre positif")
    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DealStatus status = DealStatus.PENDING;

    private String tiktokVideoUrl;

    private LocalDateTime deadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
