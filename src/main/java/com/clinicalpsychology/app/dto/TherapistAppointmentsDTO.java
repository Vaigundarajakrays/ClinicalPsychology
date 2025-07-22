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
public class TherapistAppointmentsDTO {

    private Long therapistId;

    private String therapistName;

    private List<TherapistDashboardDTO> therapistDashboardDTOS;
}
