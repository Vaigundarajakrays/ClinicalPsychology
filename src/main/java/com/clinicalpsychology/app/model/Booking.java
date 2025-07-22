package com.clinicalpsychology.app.model;

import com.clinicalpsychology.app.enumUtil.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "bookings")
public class Booking extends BaseEntity {

    // Create a dto include timezone and local date and session id?
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long therapistId;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Long timeSlotId;

    // This is the new field you'll use to store the exact booking time in UTC
//    @Column(nullable = false)
//    private Instant bookedDate;

    @Column(nullable = false)
    private String clientTimezone;

    private String stripeSessionId;

    private String stripePaymentIntentId;

    private String stripeRefundId;

    private Instant sessionStartTime;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String connectMethod;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Long quantity;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private Instant holdStartTime;

    @Column(columnDefinition = "TEXT")
    private String therapistMeetLink;

    @Column(columnDefinition = "TEXT")
    private String clientMeetLink;
}
