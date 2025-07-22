package com.clinicalpsychology.app.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentResponse {

    private String status;
    private String message;
    private String sessionId;
    private String sessionUrl;

}
