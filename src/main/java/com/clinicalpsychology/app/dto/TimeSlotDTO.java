package com.clinicalpsychology.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TimeSlotDTO {

    private Long id;
    private String timeStart;
    private String timeEnd;
    private String status;

}
