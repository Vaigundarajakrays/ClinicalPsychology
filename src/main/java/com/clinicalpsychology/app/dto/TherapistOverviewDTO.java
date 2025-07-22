package com.clinicalpsychology.app.dto;

import com.clinicalpsychology.app.enumUtil.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TherapistOverviewDTO {

    private Long therapistId;

    private String therapistName;

    private String joinDate;

    private Long futureSessions;

    private Long completedSessions;

    private String profileUrl;

    private AccountStatus accountStatus;
}
