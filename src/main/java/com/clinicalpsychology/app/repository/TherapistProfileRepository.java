package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.enumUtil.AccountStatus;
import com.clinicalpsychology.app.enumUtil.ApprovalStatus;
import com.clinicalpsychology.app.model.TherapistProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TherapistProfileRepository extends JpaRepository<TherapistProfile, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailOrPhone(String email, String phone);

    Optional<TherapistProfile> findByEmail(String emailId);

    Long countByApprovalStatus(ApprovalStatus approvalStatus);

    List<TherapistProfile> findByApprovalStatus(ApprovalStatus approvalStatus);

    List<TherapistProfile> findAllByApprovalStatusAndAccountStatus(ApprovalStatus approvalStatus, AccountStatus accountStatus);

    boolean existsByIdAndEmail(Long id, String email);
}
