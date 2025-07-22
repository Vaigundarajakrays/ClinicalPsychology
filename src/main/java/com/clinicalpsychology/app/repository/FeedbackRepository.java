package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    boolean existsByClientId(Long clientId);
}
