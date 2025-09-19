package com.clinicalpsychology.app.dto;

import com.clinicalpsychology.app.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LoginResponse {

    private String token;
    private Role role;
    private String name;
    private Long id;
    private String timezone;
    private String profileUrl;
    @JsonProperty("isSubscribed")
    private boolean isSubscribed;

}
