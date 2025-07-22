package com.clinicalpsychology.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass // table not create for this entity
@EntityListeners(AuditingEntityListener.class) // enable spring data jpa auditing, had to use @EnableJpaAuditing in main file too
@Data
public class BaseEntity {

    @CreatedDate //spring insert time when data in inserted
    @Column(updatable = false, nullable = false)
    private Instant createdAt;  // Store date time in UTC

    @LastModifiedDate // spring insert time when data is modified
    @Column(nullable = false)
    private Instant updatedAt;

    @JsonIgnore
    @Column(nullable = false)
    private boolean isActive = true;  // to not delete instead make that data active false
}
