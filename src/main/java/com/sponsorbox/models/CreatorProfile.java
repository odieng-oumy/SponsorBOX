package com.sponsorbox.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "creator_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "Le handle TikTok est obligatoire")
    @Column(nullable = false)
    private String tiktokHandle;

    @Column(nullable = false)
    private int followersCount;

    private double engagementRate;

    @Column(nullable = false)
    private boolean eligible;

    private String bio;

    private String category;

    @Column(unique = true)
    private String verificationCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CertificationStatus certificationStatus = CertificationStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.eligible = this.followersCount >= 5000;
    }
}
