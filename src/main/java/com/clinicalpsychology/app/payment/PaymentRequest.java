package com.clinicalpsychology.app.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentRequest {

    private Double amount;
    private Long quantity;
    private String name;
    private String currency;
    private Long userId;

}
