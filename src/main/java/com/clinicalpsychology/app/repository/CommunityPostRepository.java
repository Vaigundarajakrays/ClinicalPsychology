package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.model.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    boolean existsByUserIdAndTitle(Long userId, String title);
}

