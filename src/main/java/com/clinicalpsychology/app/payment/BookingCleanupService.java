package com.clinicalpsychology.app.payment;

import com.clinicalpsychology.app.enums.PaymentStatus;
import com.clinicalpsychology.app.model.Booking;
import com.clinicalpsychology.app.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCleanupService {

    private final BookingRepository bookingRepository;

    // In main file enable scheduling
    // 5 minutes
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void expireStaleHeldBookings() {

        log.info("🕵️ Checking for stale HOLD bookings...");

        Instant cutoffTime = Instant.now().minus(Duration.ofMinutes(15));
        List<Booking> staleBookings = bookingRepository.findByPaymentStatusAndHoldStartTimeBefore(PaymentStatus.HOLD, cutoffTime);

        for (Booking booking : staleBookings) {
            booking.setPaymentStatus(PaymentStatus.EXPIRED);
        }

        if (!staleBookings.isEmpty()) {
            bookingRepository.saveAll(staleBookings);
            log.info("⏰ Expired {} stale HOLD booking(s).", staleBookings.size());
        } else {
            log.info("✅ No stale HOLD bookings found.");
        }
    }
}
