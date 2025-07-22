package com.clinicalpsychology.app.model;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@EqualsAndHashCode(callSuper = true)
public class ChatMessage extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;

    private Long recipientId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean read;

    @PrePersist
    public void prePersist() {
        this.read = false;
    }

}
