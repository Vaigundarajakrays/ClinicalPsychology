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
public class AllTherapistsResponseDTO {

    private Long therapistId;

    private String name;

    private String profileUrl;

    private List<String> categories;

    private String summary;
}
