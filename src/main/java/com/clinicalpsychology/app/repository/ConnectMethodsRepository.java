package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.model.ConnectMethods;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectMethodsRepository extends JpaRepository<ConnectMethods, Long> {
    boolean existsByName(String name);
}
