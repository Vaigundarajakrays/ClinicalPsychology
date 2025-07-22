package com.clinicalpsychology.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientProfileDTO {
    private Long clientId;
    private String name;
    private String email;
    private String phone;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String description;
    private List<String> languages;
    private String timezone;
    private String profileUrl;
    private String subscriptionPlan;
    private Long customerId;
    private String joinDate;
    private String industry;
    private String location;
    private List<String> goals;
    private String status;


//    private String businessStage;
//    private String revenueRange;
//    private Integer employees;
//
//    private String therapistName;
//    private String therapistSpeciality;
//    private String sessionStatus;
//    private String nextSession;
//    private String therapistRating;
//
//    private Integer sessionsEnrolled;
//    private Integer workshopsAttended;
//    private String satisfactionScore;
//    private String mostValuedSupport;
//    private String suggestions;
//    private String notes;
//    private List<String> goals;
//    private String status;
}
