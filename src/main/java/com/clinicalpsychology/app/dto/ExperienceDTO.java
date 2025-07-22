package com.clinicalpsychology.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDTO {

    private Long id;
    private String role;
    private String companyName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

}
