package com.sponsorbox.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "testimonials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Testimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "showcase_id", nullable = false)
    private SuccessShowcase showcase;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    private String authorName;

    private String authorCompany;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private int rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
