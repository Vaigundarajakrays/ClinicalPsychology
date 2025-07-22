package com.clinicalpsychology.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TherapistDashboardDTO {

    private String sessionTime;

    private String sessionDuration;

    private String clientName;

    private String sessionName;

    private String meetType;

    private String status;

    private String therapistMeetLink;

    private Long bookingId;

}
