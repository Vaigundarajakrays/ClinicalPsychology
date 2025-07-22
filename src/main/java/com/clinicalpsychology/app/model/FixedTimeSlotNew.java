package com.clinicalpsychology.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@ToString(exclude = "therapist")
@Table(name = "fixed_time_slots_new")
@EqualsAndHashCode(callSuper = true)
public class FixedTimeSlotNew extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timeStart;  //UTC

    @ManyToOne
    @JoinColumn(name = "therapist_id", nullable = false)
    @JsonIgnore
    private TherapistProfile therapist;
}
