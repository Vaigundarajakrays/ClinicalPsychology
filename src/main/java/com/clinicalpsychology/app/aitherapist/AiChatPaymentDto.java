package com.clinicalpsychology.app.aitherapist;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AiChatPaymentDto {

    @NotNull(message = "Client ID cannot be null")
    private Long clientId;

    @NotNull(message = "Amount cannot be null")
    @Min(value = 0, message = "Amount must be at least 0")
    private Double amount;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;

    @NotBlank(message = "Product name cannot be empty or null")
    private String productName;

    @NotBlank(message = "Currency cannot be empty or null")
    private String currency;

}
