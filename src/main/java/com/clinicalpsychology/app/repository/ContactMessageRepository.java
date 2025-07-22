package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
