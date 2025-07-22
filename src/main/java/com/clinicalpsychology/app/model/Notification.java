package com.clinicalpsychology.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Builder
@EqualsAndHashCode(callSuper = true)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private Long therapistId;

    @Column(nullable = false)
    private Boolean isRead;

    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        this.isRead = false;
    }
}
