package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.enums.AccountStatus;
import com.clinicalpsychology.app.enums.ApprovalStatus;
import com.clinicalpsychology.app.model.TherapistProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // We are checking the null at last because of a reason. See it in docs
    @Query("""
    SELECT DISTINCT t FROM TherapistProfile t 
    JOIN t.categories c 
    WHERE 
        (LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL) AND 
        (LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%')) OR :location IS NULL) AND 
        (t.amount >= :minPrice OR :minPrice IS NULL) AND 
        (t.amount <= :maxPrice OR :maxPrice IS NULL) AND 
        (LOWER(c) LIKE LOWER(CONCAT('%', :category, '%')) OR :category IS NULL)
    """)
    List<TherapistProfile> searchTherapists(
            @Param("name") String name,
            @Param("location") String location,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("category") String category);

}
