package com.clinicalpsychology.app.repository;

import com.clinicalpsychology.app.enumUtil.PaymentStatus;
import com.clinicalpsychology.app.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
//    List<Booking> findByTherapistIdAndBookingDate(Long therapistId, LocalDate date);
//
//    List<Booking> findByUserIdAndBookingDate(Long userId, LocalDate bookingDate);
//
//    List<Booking> findByUserIdAndTherapistIdAndBookingDateGreaterThanEqual(Long userId, Long therapistId, LocalDate today);
//
//    List<Booking> findByUserIdAndBookingDateAndPaymentStatus(Long userId, LocalDate bookingDate, String complete);

    Booking findByStripeSessionId(String sessionId);

//    List<Booking> findByTherapistIdAndBookingDateAndPaymentStatus(Long therapistId, LocalDate date, String completed);

//    List<Booking> findByTherapistIdAndBookedDateBetweenAndPaymentStatus(Long therapistId, Instant utcStart, Instant utcEnd, PaymentStatus paymentStatus);
//
//    List<Booking> findByClientIdAndBookedDateBetweenAndPaymentStatus(Long clientId, Instant utcStart, Instant utcEnd, PaymentStatus paymentStatus);

    List<Booking> findByClientIdAndPaymentStatus(Long clientId, PaymentStatus paymentStatus);

    List<Booking> findByTherapistIdAndPaymentStatus(Long therapistId, PaymentStatus paymentStatus);

    List<Booking> findByTherapistIdAndSessionStartTimeBetweenAndPaymentStatus(Long therapistId, Instant utcStart, Instant utcEnd, PaymentStatus paymentStatus);

    List<Booking> findByClientIdAndSessionStartTimeBetweenAndPaymentStatus(Long clientId, Instant utcStart, Instant utcEnd, PaymentStatus paymentStatus);

//    Optional<Booking> findByTimeSlotIdAndSessionStartTimeBetweenAndPaymentStatus(Long timeSlotId, Instant utcStart, Instant utcEnd, PaymentStatus paymentStatus);

//    Optional<Booking> findByTimeSlotIdAndSessionStartTimeAndPaymentStatus(Long timeSlotId, Instant sessionStart, PaymentStatus paymentStatus);

//    Optional<Booking> findByTimeSlotIdAndSessionStartTimeAndPaymentStatusIn(Long timeSlotId, Instant sessionStart, List<PaymentStatus> completed);

    List<Booking> findByPaymentStatusAndHoldStartTimeBefore(PaymentStatus paymentStatus, Instant cutoffTime);

    List<Booking> findByTimeSlotIdAndSessionStartTimeAndPaymentStatusIn(Long timeSlotId, Instant sessionStart, List<PaymentStatus> completed);
}
