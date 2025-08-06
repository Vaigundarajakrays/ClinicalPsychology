package com.clinicalpsychology.app.seed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TherapistSeederDTO {

    private String therapistEmail;
    private String phone;
    private String timezone;
    private String password;
    private String name;
    private String profileUrl;
    private String linkedinUrl;
    private String resumeUrl;
    private String yearsOfExperience;
    private List<String> categories;
    private String summary;
    private String description;
    private Double amount;
    private List<String> timeSlots;
    private List<String> education;
    private List<String> languages;
}
