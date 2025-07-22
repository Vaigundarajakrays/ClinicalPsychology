package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTherapistIdAndIsRead(Long therapistId, Boolean isRead);

    List<Notification> findByRecipientIdAndIsRead(Long userId, boolean isRead);

    List<Notification> findByIsRead(Boolean isRead);
}
