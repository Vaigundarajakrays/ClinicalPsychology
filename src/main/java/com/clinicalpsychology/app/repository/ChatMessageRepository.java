package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderIdAndRecipientId(Long senderId, Long recipientId);

    List<ChatMessage> findBySenderIdAndRecipientIdAndRead(Long senderId, Long recipientId, boolean read);

    @Transactional
    @Modifying // Returns the count of updated rows
    @Query("UPDATE ChatMessage c SET c.read = true WHERE c.senderId = :senderId AND c.recipientId = :recipientId AND c.read = false")
    int markAllAsRead(@Param("senderId") Long senderId, @Param("recipientId") Long recipientId);

}
