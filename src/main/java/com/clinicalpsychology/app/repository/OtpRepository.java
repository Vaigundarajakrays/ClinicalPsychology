package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmail(String email);

    Optional<Otp> findByEmailAndOtp(String email, String otp);
}
