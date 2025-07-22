package com.clinicalpsychology.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubscribeResponseDTO {

    // If not given this, in response, field name will be "subscribed" as "is" will omit by jackson.
    @JsonProperty("isSubscribed")
    private boolean isSubscribed;

    private String email;
}
