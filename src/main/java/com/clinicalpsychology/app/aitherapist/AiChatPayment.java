package com.clinicalpsychology.app.aitherapist;

import com.clinicalpsychology.app.enums.PaymentStatus;
import com.clinicalpsychology.app.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class AiChatPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the client
    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    // Stripe related fields
    private String stripeSessionId;

    private String stripePaymentIntentId;

    private String stripeRefundId;

}

