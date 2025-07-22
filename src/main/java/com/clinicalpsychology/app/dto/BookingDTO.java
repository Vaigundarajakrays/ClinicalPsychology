package com.clinicalpsychology.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BookingDTO {

    private Long therapistId;

    private Long clientId;

    private Long timeSlotId;

    private String bookingDate;

    private String stripeSessionId;

    private String category;

    private String connectMethod;

    private Double amount;

    private String currency;

    private String productName;

    private Long quantity;

    private String paymentStatus;

    private String therapistMeetLink;

    private String userMeetLink;
}
