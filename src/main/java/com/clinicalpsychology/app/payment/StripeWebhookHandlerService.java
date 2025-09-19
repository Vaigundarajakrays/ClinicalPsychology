package com.clinicalpsychology.app.payment;

import com.clinicalpsychology.app.enums.PaymentStatus;
import com.clinicalpsychology.app.enums.ZoomContextType;
import com.clinicalpsychology.app.model.Booking;
import com.clinicalpsychology.app.repository.BookingRepository;
import com.clinicalpsychology.app.zoom.ZoomMeetingResponse;
import com.clinicalpsychology.app.zoom.ZoomMeetingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookHandlerService {

    private final ObjectMapper objectMapper;
    private final BookingRepository bookingRepository;
    private final ZoomMeetingService zoomMeetingService;

    @Async
    public void handleSessionEventAsync(Event event, PaymentStatus statusToSet) {
        try {
            JsonNode data = objectMapper.readTree(event.getData().getObject().toJson());
            String sessionId = data.get("id").asText();
            JsonNode metadata = data.get("metadata");

            if(metadata==null){
                log.warn("Metadata should not be null");
                return;
            }

            String bookingIdStr = metadata.has("bookingId")
                    ? metadata.get("bookingId").asText()
                    : null;

            Booking booking = bookingRepository.findByStripeSessionId(sessionId);

            // Don't throw custom exception in this controller because this api called by stripe not FRONTEND
            if (booking == null) {
                log.warn("Booking not found for sessionId: {}", sessionId);
                return;
            }

            // Set status
            booking.setPaymentStatus(statusToSet);

            if (statusToSet.isCompleted()) {

                String therapistEmail = metadata.get("therapistEmail").asText();
                String clientEmail = metadata.get("clientEmail").asText();

                String therapistName = metadata.get("therapistName").asText();
                String clientName = metadata.get("clientName").asText();

                Instant sessionStart = Instant.parse(metadata.get("sessionStart").asText());
                Instant sessionEnd = Instant.parse(metadata.get("sessionEnd").asText());

                String therapistTimezone = metadata.get("therapistTimezone").asText();
                String clientTimezone = metadata.get("clientTimezone").asText();

                // Payment intent id is used for refund,
                String paymentIntentId = data.has("payment_intent") && !data.get("payment_intent").isNull() ? data.get("payment_intent").asText() : null;
                booking.setStripePaymentIntentId(paymentIntentId);

                // Create Zoom meeting and get links
                ZoomMeetingResponse zoomLinks = zoomMeetingService
                        .createZoomMeetingAndNotify(therapistEmail, clientEmail,therapistName, clientName, sessionStart, sessionEnd, null, ZoomContextType.NEW, therapistTimezone, clientTimezone);

                booking.setTherapistMeetLink(zoomLinks.getStartUrl());
                booking.setClientMeetLink(zoomLinks.getJoinUrl());
                log.info("Zoom meeting created for booking ID {} - therapist: {}, user: {}", bookingIdStr, therapistEmail, clientEmail);

            }

            bookingRepository.save(booking);
            log.info("Booking updated with payment status: {}", statusToSet);

        } catch (Exception e) {
            log.error("Error updating booking payment status: {}", e.getMessage(), e);
        }
    }
}
