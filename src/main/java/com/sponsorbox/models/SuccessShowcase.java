package com.sponsorbox.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "success_showcases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuccessShowcase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private CreatorProfile creator;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String brandName;

    private String tiktokVideoUrl;

    private int viewsCount;

    private int likesCount;

    private int sharesCount;

    private int commentsCount;

    @Column(columnDefinition = "TEXT")
    private String resultSummary;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
