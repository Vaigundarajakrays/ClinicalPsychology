package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.dto.ClientDashboardDTO;
import com.clinicalpsychology.app.dto.ClientProfileDTO;
import com.clinicalpsychology.app.dto.RescheduleDTO;
import com.clinicalpsychology.app.enumUtil.PaymentStatus;
import com.clinicalpsychology.app.enumUtil.Role;
import com.clinicalpsychology.app.enumUtil.ZoomContextType;
import com.clinicalpsychology.app.exceptionHandling.InvalidFieldValueException;
import com.clinicalpsychology.app.exceptionHandling.ResourceAlreadyExistsException;
import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.*;
import com.clinicalpsychology.app.repository.*;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.zoom.ZoomMeetingResponse;
import com.clinicalpsychology.app.zoom.ZoomMeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
@RequiredArgsConstructor
public class ClientProfileService {

    private final ClientProfileRepository clientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final TherapistProfileRepository therapistNewRepository;
    private final UsersRepository usersRepository;
    private final BookingRepository bookingRepository;
    private final FixedTimeSlotNewRepository fixedTimeSlotNewRepository;
    private final ZoomMeetingService zoomMeetingService;
    private final JavaMailSender mailSender;
    private final EmailService emailService;
    // private final PaymentService paymentService;   Dont remove it, in future may need for refund


    @Value("${mail.from}")
    private String mailFrom;

    @Transactional
    public CommonResponse<ClientProfile> registerClient(ClientProfileDTO clientDto) throws UnexpectedServerException {
        try {
            // Check if email or phone already exists in client or users table
            if (clientProfileRepository.existsByEmailOrPhone(clientDto.getEmail(), clientDto.getPhone())) {
                throw new ResourceAlreadyExistsException(EMAIL_PHONE_EXISTS);
            }

            if (therapistNewRepository.existsByEmail(clientDto.getEmail())) {
                throw new ResourceAlreadyExistsException(ALREADY_REGISTERED_THERAPIST_EMAIL);
            }

            // Encrypt password
            String hashedPassword = passwordEncoder.encode(clientDto.getPassword());

            // Create and save ClientProfile entity
            ClientProfile client = ClientProfile.builder()
                    .name(clientDto.getName())
                    .email(clientDto.getEmail())
                    .phone(clientDto.getPhone())
                    .password(hashedPassword)
                    .description(clientDto.getDescription())
                    .goals(clientDto.getGoals())
                    .timeZone(clientDto.getTimezone())
                    .profileUrl(clientDto.getProfileUrl())
                    .address(clientDto.getAddress())
                    .dob(clientDto.getDob())
                    .gender(clientDto.getGender())
                    .status(ACTIVE)
                    .build();

            ClientProfile savedClient = clientProfileRepository.save(client);

            // Save entry to users table
            Users user = Users.builder()
                    .emailId(client.getEmail())
                    .password(hashedPassword)
                    .role(Role.CLIENT) // Assuming client is assigned USER role
                    .build();

            usersRepository.save(user);

            // You can add email sending here if required
            // mailService.sendWelcomeToClient(client);

            return CommonResponse.<ClientProfile>builder()
                    .status(true)
                    .message("You have registered successfully")
                    .statusCode(SUCCESS_CODE)
                    .data(savedClient)
                    .build();

        } catch (ResourceAlreadyExistsException | InvalidFieldValueException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedServerException(CLIENT_REGISTRATION_FAILED+ e.getMessage());
        }
    }


    public CommonResponse<ClientProfileDTO> getClientProfile(Long id) throws ResourceNotFoundException, UnexpectedServerException {

        try {

            ClientProfile client = clientProfileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(CLIENT_FOUND_THE_ID + id));

            var formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

            var clientProfileDTO = ClientProfileDTO.builder()
                    .clientId(client.getId())
                    .name(client.getName())
                    .email(client.getEmail())
                    .phone(client.getPhone())
                    .description(client.getDescription())
                    .languages(client.getLanguages())
                    .summary(client.getSummary())
                    .timezone(client.getTimeZone())
                    .profileUrl(client.getProfileUrl())
                    .address(client.getAddress())
                    .dob(client.getDob())
                    .gender(client.getGender())
                    .subscriptionPlan(client.getSubscriptionPlan())
                    .customerId(client.getId())
                    .joinDate(client.getCreatedAt().atZone(ZoneId.of(client.getTimeZone())).format(formatter))
                    .industry(client.getIndustry())
                    .location(client.getLocation())
                    .goals(client.getGoals())
                    .status(client.getStatus())
                    .build();

            return CommonResponse.<ClientProfileDTO>builder()
                    .status(STATUS_TRUE)
                    .message(LOADED_PROFILE_DETAILS)
                    .statusCode(SUCCESS_CODE)
                    .data(clientProfileDTO)
                    .build();

        } catch (ResourceNotFoundException e){
            throw e;
        } catch (Exception e){
            throw new UnexpectedServerException(ERROR_LOADING_THERAPIST_PROFILE_DETAILS + e.getMessage());
        }
    }

    @Transactional
    public CommonResponse<ClientProfileDTO> updateClientProfile(Long id, ClientProfileDTO dto) throws ResourceNotFoundException, UnexpectedServerException {

        try {

            ClientProfile client = clientProfileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(CLIENT_NOT_FOUND_ID + id));

            var formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

            // Only update non-null fields
            if (dto.getName() != null) client.setName(dto.getName());
            if (dto.getEmail() != null) client.setEmail(dto.getEmail());
            if (dto.getPhone() != null) client.setPhone(dto.getPhone());
            if (dto.getDescription() != null) client.setDescription(dto.getDescription());
            if (dto.getProfileUrl() != null) client.setProfileUrl(dto.getProfileUrl());
            if (dto.getLanguages() != null) client.setLanguages(dto.getLanguages());
            if (dto.getAddress() != null) client.setAddress(dto.getAddress());
            if (dto.getDob() != null) client.setDob(dto.getDob());
            if (dto.getGender() != null) client.setGender(dto.getGender());
            if (dto.getSummary() != null) client.setSummary(dto.getSummary());
            if (dto.getTimezone() != null) client.setTimeZone(dto.getTimezone());
            if (dto.getSubscriptionPlan() != null) client.setSubscriptionPlan(dto.getSubscriptionPlan());
            if (dto.getIndustry() != null) client.setIndustry(dto.getIndustry());
            if (dto.getLocation() != null) client.setLocation(dto.getLocation());
            if (dto.getGoals() != null) client.setGoals(dto.getGoals());
            if (dto.getStatus() != null) client.setStatus(dto.getStatus());

            ClientProfile updated = clientProfileRepository.save(client);

            // Convert createdAt to date string (e.g. "2025-05-01")
            String joinDate = updated.getCreatedAt().atZone(ZoneId.of(client.getTimeZone())).format(formatter);

            ClientProfileDTO responseDto = ClientProfileDTO.builder()
                    .name(updated.getName())
                    .email(updated.getEmail())
                    .phone(updated.getPhone())
                    .languages(updated.getLanguages())
                    .timezone(updated.getTimeZone())
                    .profileUrl(updated.getProfileUrl())
                    .subscriptionPlan(updated.getSubscriptionPlan())
                    .customerId(updated.getId()) // Assuming customerId = id
                    .joinDate(joinDate)
                    .industry(updated.getIndustry())
                    .summary(updated.getSummary())
                    .location(updated.getLocation())
                    .address(updated.getAddress())
                    .dob(updated.getDob())
                    .gender(updated.getGender())
                    .description(updated.getDescription())
                    .goals(updated.getGoals())
                    .status(updated.getStatus())
                    .build();

            return CommonResponse.<ClientProfileDTO>builder()
                    .status(true)
                    .statusCode(200)
                    .message("You have successfully updated your profile")
                    .data(responseDto)
                    .build();

        } catch (ResourceNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new UnexpectedServerException(FAILED_UPDATE_CLIENT_PROFILE + e.getMessage());
        }
    }

    public CommonResponse<List<ClientDashboardDTO>> getAppointments(Long clientId) throws ResourceNotFoundException, UnexpectedServerException {

        try {

            ClientProfile client = clientProfileRepository.findById(clientId).orElseThrow(() -> new ResourceNotFoundException(CLIENT_NOT_FOUND_ID + clientId));

            List<Booking> bookings = bookingRepository.findByClientIdAndPaymentStatus(clientId, PaymentStatus.COMPLETED);

            if (bookings.isEmpty()) {
                return CommonResponse.<List<ClientDashboardDTO>>builder()
                        .status(STATUS_TRUE)
                        .statusCode(SUCCESS_CODE)
                        .message(CLIENT_NOT_HAVE_ANY_APPOINTMENTS)
                        .data(List.of())
                        .build();
            }

            var formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

            Instant timeNow = Instant.now();

            // I didn't used stream because
            // The issue is that you're using orElseThrow(...) inside a lambda in a stream() — and that method throws a checked exception (ResourceNotFoundException).
            // Java doesn't allow checked exceptions to be thrown from inside lambda expressions unless you handle them.

            List<ClientDashboardDTO> appointments = new ArrayList<>();
            for (Booking booking : bookings) {

                // To find therapist name
                TherapistProfile therapist = therapistNewRepository.findById(booking.getTherapistId())
                        .orElseThrow(() -> new ResourceNotFoundException(THERAPIST_NOT_FOUND_ID + booking.getTherapistId() + BOOKING_ID + booking.getId()));

                // To find the status
                Instant sessionStartTime = booking.getSessionStartTime();
                Instant sessionEndTime = sessionStartTime.plus(Duration.ofMinutes(60));
                String status;
                if (timeNow.isBefore(sessionStartTime)) {
                    status = UPCOMING;
                } else if (timeNow.isAfter(sessionEndTime)) {
                    status = COMPLETE;
                } else {
                    status = ONGOING;
                }


                // To find session time in client time zone
                ZonedDateTime sessionTime = sessionStartTime.atZone(ZoneId.of(client.getTimeZone()));
                String session = sessionTime.format(formatter);

                appointments.add(ClientDashboardDTO.builder()
                        .therapistName(therapist.getName())
                        .sessionTime(session)
                        .therapistId(booking.getTherapistId())
                        .bookingId(booking.getId())
                        .meetType(booking.getConnectMethod())
                        .clientMeetLink(booking.getClientMeetLink())
                        .status(status)
                        .build());
            }

            return CommonResponse.<List<ClientDashboardDTO>>builder()
                    .statusCode(SUCCESS_CODE)
                    .message(LOADED_CLIENT_APPOINTMENTS)
                    .data(appointments)
                    .status(STATUS_TRUE)
                    .build();

        } catch (ResourceNotFoundException e){
            throw e;
        } catch (Exception e){
            throw new UnexpectedServerException(ERROR_LOADING_APPOINTMENTS_CLIENT + e.getMessage());
        }

    }

    public CommonResponse<ClientDashboardDTO> rescheduleBooking(Long bookingId, RescheduleDTO rescheduleDTO) throws ResourceNotFoundException, UnexpectedServerException {

        try {

            Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

            FixedTimeSlotNew currentSlot = fixedTimeSlotNewRepository.findById(rescheduleDTO.getTimeSlotId()).orElseThrow(() -> new ResourceNotFoundException("Time slot not found for this id: " + rescheduleDTO.getTimeSlotId()));

            TherapistProfile therapistProfile = therapistNewRepository.findById(booking.getTherapistId()).orElseThrow(() -> new ResourceNotFoundException("Booked therapist not found for the id: " + booking.getTherapistId()));

            ClientProfile clientProfile = clientProfileRepository.findById(booking.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found for the id: " + booking.getClientId()));

            var dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate date;
            try {
                date = LocalDate.parse(rescheduleDTO.getDate(), dateFormatter);
            } catch (DateTimeParseException e) {
                throw new InvalidFieldValueException("Invalid booking date format. Expected yyyy-MM-dd.");
            }

            ZoneId zoneId;
            try {
                zoneId = ZoneId.of(clientProfile.getTimeZone());
            } catch (DateTimeException e) {
                throw new InvalidFieldValueException("Invalid time zone");
            }

            Instant bookingDate = date.atStartOfDay(zoneId).toInstant();

            LocalTime localTimeSlotInClientTimezone = currentSlot.getTimeStart().atZone(zoneId).toLocalTime();

            Instant sessionStartTime = date.atTime(localTimeSlotInClientTimezone).atZone(zoneId).toInstant();
            Instant sessionEndTime = sessionStartTime.plus(Duration.ofMinutes(60));

            String sessionStartTimeStr = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a").withZone(ZoneId.of(clientProfile.getTimeZone())).format(sessionStartTime);


            Instant oldStartTime = booking.getSessionStartTime();

            ZoomMeetingResponse zoomLinks = zoomMeetingService
                    .createZoomMeetingAndNotify(therapistProfile.getEmail(), clientProfile.getEmail(), therapistProfile.getName(), clientProfile.getName(), sessionStartTime, sessionEndTime, oldStartTime, ZoomContextType.RESCHEDULE, therapistProfile.getTimezone(), clientProfile.getTimeZone());

            booking.setTimeSlotId(rescheduleDTO.getTimeSlotId());
            booking.setSessionStartTime(sessionStartTime);
            booking.setTherapistMeetLink(zoomLinks.getStartUrl());
            booking.setClientMeetLink(zoomLinks.getJoinUrl());

            Booking savedBooking = bookingRepository.save(booking);

            Instant timeNow = Instant.now();
            String status;
            if (timeNow.isBefore(sessionStartTime)) {
                status = UPCOMING;
            } else if (timeNow.isAfter(sessionEndTime)) {
                status = COMPLETE;
            } else {
                status = ONGOING;
            }

            var clientDashboard = ClientDashboardDTO.builder()
                    .bookingId(booking.getId())
                    .sessionTime(sessionStartTimeStr)
                    .therapistName(therapistProfile.getName())
                    .meetType(booking.getConnectMethod())
                    .status(status)
                    .build();

            return CommonResponse.<ClientDashboardDTO>builder()
                    .statusCode(SUCCESS_CODE)
                    .message("Successfully rescheduled.")
                    .data(clientDashboard)
                    .status(STATUS_TRUE)
                    .build();



        } catch (ResourceNotFoundException | InvalidFieldValueException e){
            throw e;
        } catch (Exception e){
            throw new UnexpectedServerException("Error while rescheduling the therapist: " + e.getMessage());
        }



    }

    public CommonResponse<String> cancelBooking(Long bookingId) throws ResourceNotFoundException, UnexpectedServerException {

        try {

            Booking booking = bookingRepository.findById(bookingId).orElseThrow(()-> new ResourceNotFoundException("Booking not found with id: " + bookingId));

            FixedTimeSlotNew currentSlot = fixedTimeSlotNewRepository.findById(booking.getTimeSlotId()).orElseThrow(() -> new ResourceNotFoundException("Time slot not found for this id: " + booking.getTimeSlotId()));

            TherapistProfile therapistProfile = therapistNewRepository.findById(booking.getTherapistId()).orElseThrow(() -> new ResourceNotFoundException("Booked therapist not found for the id: " + booking.getTherapistId()));

            ClientProfile clientProfile = clientProfileRepository.findById(booking.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found for the id: " + booking.getClientId()));

            var formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a z");
            String sessionStartForClient = formatter.withZone(ZoneId.of(clientProfile.getTimeZone())).format(booking.getSessionStartTime());
            String sessionStartForTherapist = formatter.withZone(ZoneId.of(therapistProfile.getTimezone())).format(booking.getSessionStartTime());

            String clientEmailBody = String.format("""
                Hi %s,

                Your session with %s, originally scheduled on **%s**, has been successfully cancelled.

                If this was a mistake or you’d like to reschedule, feel free to book a new session from your dashboard.

                Thank you,
                The TherapistBooster Team
                """, clientProfile.getName(), therapistProfile.getName(), sessionStartForClient);

            String therapistEmailBody = String.format("""
                Hi %s,

                The session with %s, scheduled on **%s**, has been cancelled by the client.

                You can view your updated schedule in the TherapistBooster dashboard.

                Best regards,
                The TherapistBooster Team
                """, therapistProfile.getName(), clientProfile.getName(), sessionStartForTherapist);

            // Send emails
            emailService.sendEmail(clientProfile.getEmail(), "Your TherapistBooster Session Has Been Cancelled", clientEmailBody);
            emailService.sendEmail(therapistProfile.getEmail(), "A Session Has Been Cancelled", therapistEmailBody);

            bookingRepository.deleteById(bookingId);



            return CommonResponse.<String>builder()
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message("Booking cancelled successfully")
                    .build();

        } catch (Exception e){
            throw new UnexpectedServerException("Error while cancelling the booking: " + e.getMessage());
        }

        // In future if needed to refund

        // Save refund ID & update status in DB

//        String refundId = paymentService.refundBooking(booking.getStripePaymentIntentId());
//        booking.setStripeRefundId(refundId);
//        booking.setRefundStatus("refunded");
//        booking.setPaymentStatus("refunded");
//        bookingRepository.save(booking);
    }

//    private void sendEmail(String to, String subject, String text) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(text);
//        message.setFrom(mailFrom);
//        mailSender.send(message);
//  }
}
