package com.clinicalpsychology.app.dto;

import com.clinicalpsychology.app.enums.AccountStatus;
import com.clinicalpsychology.app.enums.ApprovalStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TherapistProfileDTO {

    private Long therapistId;
    private String name;
    private String phone;
    private String email;
    private String linkedinUrl;
    private String profileUrl;
    private String resumeUrl;
    private String yearsOfExperience;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private List<String> categories;
    private String summary;
    private String description;
    private Double amount;
    private Boolean terms;
    private Boolean termsAndConditions;
    private String location;
    private String timezone; // 👈 Needed to convert local to UTC
    private List<String> timeSlots; // 👈 List of time slot DTOs
    private AccountStatus accountStatus;
    private ApprovalStatus approvalStatus;
    private List<String> education;
    private List<String> languages;

}
