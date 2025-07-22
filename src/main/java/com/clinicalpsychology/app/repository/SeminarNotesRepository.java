package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.model.SeminarNotes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeminarNotesRepository extends JpaRepository<SeminarNotes, Long> {

    boolean existsByUserIdAndTitle(Long userId, String title);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);

    SeminarNotes findByUserId(Long userId);
}
