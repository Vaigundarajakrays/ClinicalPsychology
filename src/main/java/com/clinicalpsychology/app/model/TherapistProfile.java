package com.clinicalpsychology.app.model;

import com.clinicalpsychology.app.enumUtil.AccountStatus;
import com.clinicalpsychology.app.enumUtil.ApprovalStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "therapist_new")
@EqualsAndHashCode(callSuper = true)
public class TherapistProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String linkedinUrl;

    @Column(nullable = false)
    private String profileUrl;

    @Column(nullable = false)
    private String resumeUrl;

    @Column(nullable = false)
    private String yearsOfExperience;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    private List<String> categories;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Boolean terms;

    @Column(nullable = false)
    private Boolean termsAndConditions;

    @Column(nullable = false)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // When you use the Builder pattern (via @Builder), it bypasses default field values unless you explicitly include them in the builder call.
    // private AccountStatus accountStatus = AccountStatus.INACTIVE;
    private AccountStatus accountStatus;

    @PrePersist
    protected void onCreate() {
        if (approvalStatus == null) {
            approvalStatus = ApprovalStatus.PENDING;
        }
        if (accountStatus == null) {
            accountStatus = AccountStatus.INACTIVE;
        }
    }


    @OneToMany(mappedBy = "therapist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FixedTimeSlotNew> timeSlots;
}
