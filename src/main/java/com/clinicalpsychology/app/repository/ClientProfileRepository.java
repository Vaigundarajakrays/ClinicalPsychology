package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.model.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, Long> {
    boolean existsByEmailOrPhone(String email, String phone);

    boolean existsByEmail(String email);

    Optional<ClientProfile> findByEmail(String emailId);

    boolean existsByIdAndEmail(Long clientId, String email);
}
