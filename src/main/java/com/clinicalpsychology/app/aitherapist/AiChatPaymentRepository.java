package com.clinicalpsychology.app.aitherapist;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AiChatPaymentRepository extends JpaRepository<AiChatPayment, Long> {
    AiChatPayment findByStripeSessionId(String stripeSessionId);
}
