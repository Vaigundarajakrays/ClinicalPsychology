package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.enums.PaymentStatus;
import com.clinicalpsychology.app.payment.StripeWebhookHandlerService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final StripeWebhookHandlerService stripeWebhookHandlerService;

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    // Make sure you Immediately send a 200 OK as soon as you get the event
    // Then process the event and logics like Db update, email sending in the background (asynchronously)
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            logger.warn("⚠️ Invalid Stripe signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Stripe signature");
        } catch (Exception e) {
            logger.error("Webhook error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error: " + e.getMessage());
        }

        String eventType = event.getType();
        logger.info("Received Stripe event: {}", eventType);

        switch (eventType) {
            case "checkout.session.completed":
            case "checkout.session.async_payment_succeeded":
                stripeWebhookHandlerService.handleSessionEventAsync(event, PaymentStatus.COMPLETED);
                break;

            case "checkout.session.expired":
                stripeWebhookHandlerService.handleSessionEventAsync(event, PaymentStatus.EXPIRED);
                break;

            case "checkout.session.async_payment_failed":
                stripeWebhookHandlerService.handleSessionEventAsync(event, PaymentStatus.FAILURE);
                break;

            default:
                logger.warn("Unhandled Stripe event type: {}", eventType);
                break;
        }

        return ResponseEntity.ok("✅ Webhook processed: " + eventType);
    }

}



