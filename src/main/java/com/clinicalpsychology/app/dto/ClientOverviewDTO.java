package com.clinicalpsychology.app.dto;

import com.clinicalpsychology.app.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ClientOverviewDTO {

    private Long clientId;

    private String clientName;

    private String joinDate;

    private Long futureSessions;

    private Long completedSessions;

    private String profileUrl;

    private AccountStatus accountStatus;
}
