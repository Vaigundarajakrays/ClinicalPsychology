package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryNewRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}
