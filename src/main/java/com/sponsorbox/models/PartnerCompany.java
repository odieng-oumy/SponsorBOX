package com.sponsorbox.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "partner_companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Column(nullable = false)
    private String companyName;

    @NotBlank(message = "Le SIRET est obligatoire")
    @Column(nullable = false, unique = true)
    private String siret;

    @NotBlank(message = "L'email officiel est obligatoire")
    @Column(nullable = false)
    private String officialEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CertificationStatus certificationStatus = CertificationStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
