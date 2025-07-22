package com.clinicalpsychology.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {

    private Long id;
    private String name;
    private String provideBy;
    private String createDate;
    private String imageUrl;

}
