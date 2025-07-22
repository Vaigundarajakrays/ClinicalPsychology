package com.clinicalpsychology.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FixedTimeSlotDTO {

    private Long id;
    private LocalTime timeStart;
    private LocalTime timeEnd;

}
