package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.dto.TimeSlotDTO;
import com.clinicalpsychology.app.enumUtil.PaymentStatus;
import com.clinicalpsychology.app.exceptionHandling.InvalidFieldValueException;
import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.Booking;
import com.clinicalpsychology.app.model.FixedTimeSlotNew;
import com.clinicalpsychology.app.repository.BookingRepository;
import com.clinicalpsychology.app.repository.ClientProfileRepository;
import com.clinicalpsychology.app.repository.FixedTimeSlotNewRepository;
import com.clinicalpsychology.app.repository.TherapistProfileRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.clinicalpsychology.app.util.Constant.*;
import static com.clinicalpsychology.app.util.Constant.ERROR_FETCHING_TIME_SLOTS_FOR_THERAPIST;

@Service
@RequiredArgsConstructor
public class FixedTimeSlotNewService {

    private final FixedTimeSlotNewRepository fixedTimeSlotNewRepository;
    private final BookingRepository bookingRepository;
    private final TherapistProfileRepository therapistProfileRepository;
    private final ClientProfileRepository clientProfileRepository;

    public CommonResponse<List<TimeSlotDTO>> getTimeSlotsOfTherapist(Long therapistId, Long clientId, String localDate) throws ResourceNotFoundException, UnexpectedServerException {

        if (!therapistProfileRepository.existsById(therapistId)) {
            throw new ResourceNotFoundException(NO_THERAPISTS_AVAILABLE);
        }

        String timezone = clientProfileRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException( CLIENT_NOT_FOUND_WITH_ID + clientId))
                .getTimeZone();

        // Convert string timezone to ZoneId
        final ZoneId clientTimezone;
        try {
            clientTimezone = ZoneId.of(timezone);
        } catch (DateTimeException e){
            throw new InvalidFieldValueException("Invalid Timezone");
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Converting string date to LocalDate
        LocalDate date;
        try {
            date = LocalDate.parse(localDate, dateFormatter);
        } catch (DateTimeParseException e){
            throw new InvalidFieldValueException("Date must be yyyy-mm-dd format");
        }


        try {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            // Get current time in client timezone
            ZonedDateTime now = ZonedDateTime.now(clientTimezone);

            // 1. Fetch all fixed time slots for the therapist (stored as UTC Instants)
            List<FixedTimeSlotNew> slots = fixedTimeSlotNewRepository.findByTherapistId(therapistId);

            if (slots.isEmpty()) {
                throw new ResourceNotFoundException(NO_TIME_SLOTS_AVAILABLE_FOR_THERAPIST);
            }

            // 2. Fetch bookings only for that date in user's timezone
            ZonedDateTime dayStartZoned = date.atStartOfDay(clientTimezone);
            ZonedDateTime dayEndZoned = dayStartZoned.plusDays(1);

            Instant utcStart = dayStartZoned.toInstant();
            Instant utcEnd = dayEndZoned.toInstant();

            List<Booking> bookings = bookingRepository.findByTherapistIdAndSessionStartTimeBetweenAndPaymentStatus(
                    therapistId, utcStart, utcEnd, PaymentStatus.COMPLETED
            );

            Set<Long> bookedSlotIds = bookings.stream()
                    .map(Booking::getTimeSlotId)
                    .collect(Collectors.toSet());

            List<TimeSlotDTO> timeSlotDTOS = slots.stream()
                    .map(slot -> {
                        // converting the therapist slot's start time to client local time
                        // Since therapist is available daily at that time, we cant compare with client and therapist Instant
                        LocalTime slotTime = slot.getTimeStart()
                                .atZone(ZoneOffset.UTC)
                                .withZoneSameInstant(clientTimezone)
                                .toLocalTime();

                        // Apply that time to the requested date
                        ZonedDateTime userZonedTime = date.atTime(slotTime).atZone(clientTimezone);

                        String status;
                        if (bookedSlotIds.contains(slot.getId())) {
                            status = OCCUPIED;
                        } else if (userZonedTime.isBefore(now)) {
                            status = NOT_AVAILABLE;
                        } else {
                            status =AVAILABLE;
                        }

                        return TimeSlotDTO.builder()
                                .id(slot.getId())
                                .timeStart(slotTime.format(formatter))
                                .timeEnd(slotTime.plusHours(1).format(formatter))
                                .status(status)
                                .build();
                    })
                    .toList();


            return CommonResponse.<List<TimeSlotDTO>>builder()
                    .message(LOADED_ALL_TIME_SLOTS_FOR_THERAPISTS)
                    .status(STATUS_TRUE)
                    .data(timeSlotDTOS)
                    .statusCode(SUCCESS_CODE)
                    .build();
        }
        catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_FETCHING_TIME_SLOTS_FOR_THERAPIST + e.getMessage());
        }
    }

}
