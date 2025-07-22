package com.clinicalpsychology.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ClientDashboardDTO {

    private Long bookingId;

    private Long therapistId;

    private String sessionTime;

    private String therapistName;

    private String meetType;

    private String clientMeetLink;

    private String status;
}
