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
public class ClientAppointmentsDTO {

    private Long clientId;

    private String clientName;

    private List<ClientDashboardDTO> clientDashboardDTOS;
}
