package com.sponsorbox.repositories;

import com.sponsorbox.models.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    List<Testimonial> findByShowcaseIdOrderByCreatedAtDesc(Long showcaseId);

    List<Testimonial> findByShowcaseCreatorIdOrderByCreatedAtDesc(Long creatorId);
}
