package com.clinicalpsychology.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AdminDashboardDTO {

    private Long noOfTherapistApproved;

    private Long noOfTherapistNotApproved;

    private Long noOfClients;

    private List<TherapistProfileDTO> therapistProfileDTOS;
}
