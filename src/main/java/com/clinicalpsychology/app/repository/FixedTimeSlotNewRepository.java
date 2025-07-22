package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.model.FixedTimeSlotNew;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FixedTimeSlotNewRepository extends JpaRepository<FixedTimeSlotNew, Long> {
    List<FixedTimeSlotNew> findByTherapistId(Long therapistId);
}
