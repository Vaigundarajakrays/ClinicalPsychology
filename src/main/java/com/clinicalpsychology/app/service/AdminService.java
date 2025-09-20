package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.dto.*;
import com.clinicalpsychology.app.enums.AccountStatus;
import com.clinicalpsychology.app.enums.ApprovalStatus;
import com.clinicalpsychology.app.enums.PaymentStatus;
import com.clinicalpsychology.app.exception.InvalidFieldValueException;
import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.model.Booking;
import com.clinicalpsychology.app.model.ClientProfile;
import com.clinicalpsychology.app.model.TherapistProfile;
import com.clinicalpsychology.app.repository.BookingRepository;
import com.clinicalpsychology.app.repository.ClientProfileRepository;
import com.clinicalpsychology.app.repository.TherapistProfileRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private  final TherapistProfileRepository therapistProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;


    // wanna add no of therapists active? inactive?
    public CommonResponse<AdminDashboardDTO> getAdminDashboardDetails() throws UnexpectedServerException {

        try {

            Long therapistApprovedCount = therapistProfileRepository.countByApprovalStatus(ApprovalStatus.ACCEPTED);
            Long therapistNotApprovedCount = therapistProfileRepository.countByApprovalStatus(ApprovalStatus.PENDING);

            Long noOfClient = clientProfileRepository.count();

            // Can use findAllByApprovalStatus too, both works
            List<TherapistProfile> therapistProfiles = therapistProfileRepository.findByApprovalStatus(ApprovalStatus.PENDING);

            if (therapistProfiles.isEmpty()) {

                var adminDashboard = AdminDashboardDTO.builder()
                        .noOfTherapistApproved(therapistApprovedCount)
                        .noOfTherapistNotApproved(therapistNotApprovedCount)
                        .noOfClients(noOfClient)
                        .therapistProfileDTOS(List.of())
                        .build();

                return CommonResponse.<AdminDashboardDTO>builder()
                        .status(STATUS_FALSE)
                        .statusCode(SUCCESS_CODE)
                        .message(THERAPISTS_NOT_FOUND)
                        .data(adminDashboard)
                        .build();
            }

            List<TherapistProfileDTO> therapistProfileDTOS = therapistProfiles.stream()
                    .map(therapistProfile -> {

                        List<String> timeslots = therapistProfile.getTimeSlots().stream()
                                .map(timeSlot -> timeSlot.getTimeStart().atZone(ZoneId.of(therapistProfile.getTimezone())).toLocalTime().toString())
                                .toList();

                        return TherapistProfileDTO.builder()
                                .therapistId(therapistProfile.getId())
                                .name(therapistProfile.getName())
                                .phone(therapistProfile.getPhone())
                                .email(therapistProfile.getEmail())
                                .linkedinUrl(therapistProfile.getLinkedinUrl())
                                .profileUrl(therapistProfile.getProfileUrl())
                                .resumeUrl(therapistProfile.getResumeUrl())
                                .yearsOfExperience(therapistProfile.getYearsOfExperience())
                                .categories(therapistProfile.getCategories())
                                .summary(therapistProfile.getSummary())
                                .amount(therapistProfile.getAmount())
                                .terms(therapistProfile.getTerms())
                                .termsAndConditions(therapistProfile.getTermsAndConditions())
                                .timezone(therapistProfile.getTimezone())
                                .accountStatus(therapistProfile.getAccountStatus())
                                .approvalStatus(therapistProfile.getApprovalStatus())
                                .timeSlots(timeslots)
                                .build();
                    })
                    .toList();

            var adminDashboard = AdminDashboardDTO.builder()
                    .noOfTherapistApproved(therapistApprovedCount)
                    .noOfTherapistNotApproved(therapistNotApprovedCount)
                    .noOfClients(noOfClient)
                    .therapistProfileDTOS(therapistProfileDTOS)
                    .build();

            return CommonResponse.<AdminDashboardDTO>builder()
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message(LOADED_ADMIN_DASHBOARD_DETAILS)
                    .data(adminDashboard)
                    .build();

        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_LOADING_ADMIN_DASHBOARD_DETAILS + e.getMessage());
        }

    }

    public CommonResponse<List<TherapistAppointmentsDTO>> getAllTherapistSessions() throws ResourceNotFoundException, UnexpectedServerException {

        try {

            List<TherapistProfile> therapistProfiles = therapistProfileRepository.findAll();

            if (therapistProfiles.isEmpty()) {

                return CommonResponse.<List<TherapistAppointmentsDTO>>builder()
                        .status(STATUS_FALSE)
                        .statusCode(SUCCESS_CODE)
                        .message(NO_THERAPIST_AVAILABLE)
                        .data(List.of())
                        .build();
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

            Instant timeNow = Instant.now();

            List<TherapistAppointmentsDTO> therapistAppointmentsDTOS = new ArrayList<>();

            for (TherapistProfile therapistProfile : therapistProfiles) {


                List<Booking> bookings = bookingRepository.findByTherapistIdAndPaymentStatus(therapistProfile.getId(), PaymentStatus.COMPLETED);

                List<TherapistDashboardDTO> therapistDashboardDTOS = new ArrayList<>();

                for (Booking booking : bookings) {

                    String sessionTime = booking.getSessionStartTime().atZone(ZoneId.of(therapistProfile.getTimezone())).format(formatter);

                    ClientProfile clientProfile = clientProfileRepository.findById(booking.getClientId()).orElseThrow(() -> new ResourceNotFoundException(CLIENT_NOT_FOUND_ID + booking.getClientId()));

                    Instant sessionStartTime = booking.getSessionStartTime();
                    Instant sessionEndTime = sessionStartTime.plus(Duration.ofMinutes(60));

                    String status;
                    if (timeNow.isBefore(sessionStartTime)) {
                        status = "upcoming";
                    } else if (timeNow.isAfter(sessionEndTime)) {
                        status = "completed";
                    } else {
                        status = "ongoing";
                    }

                    var therapistDashboardDTO = TherapistDashboardDTO.builder()
                            .sessionTime(sessionTime)
                            .sessionName(booking.getCategory())
                            .sessionDuration("1 Hr")
                            .clientName(clientProfile.getName())
                            .meetType(booking.getConnectMethod())
                            .status(status)
                            .therapistMeetLink(booking.getTherapistMeetLink())
                            .bookingId(booking.getId())
                            .build();

                    therapistDashboardDTOS.add(therapistDashboardDTO);


                }

                var therapistAppointmentsDto = TherapistAppointmentsDTO.builder()
                        .therapistId(therapistProfile.getId())
                        .therapistName(therapistProfile.getName())
                        .therapistDashboardDTOS(therapistDashboardDTOS)
                        .build();

                therapistAppointmentsDTOS.add(therapistAppointmentsDto);

            }

            return CommonResponse.<List<TherapistAppointmentsDTO>>builder()
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message(LOADED_ALL_THERAPISTS_SESSIONS)
                    .data(therapistAppointmentsDTOS)
                    .build();

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_LOADING_THERAPISTS_APPOINTMENTS + e.getMessage());
        }
    }

    public CommonResponse<List<ClientAppointmentsDTO>> getAllClientSessions() throws ResourceNotFoundException, UnexpectedServerException {

        try {

            List<ClientProfile> clientProfiles = clientProfileRepository.findAll();

            if (clientProfiles.isEmpty()) {

                return CommonResponse.<List<ClientAppointmentsDTO>>builder()
                        .status(STATUS_FALSE)
                        .statusCode(SUCCESS_CODE)
                        .message(NO_CLIENTS_AVAILABLE)
                        .data(List.of())
                        .build();
            }

            Instant timeNow = Instant.now();

            var formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

            List<ClientAppointmentsDTO> clientAppointmentsDTOS = new ArrayList<>();

            for (ClientProfile clientProfile : clientProfiles) {

                List<Booking> bookings = bookingRepository.findByClientIdAndPaymentStatus(clientProfile.getId(), PaymentStatus.COMPLETED);

                List<ClientDashboardDTO> clientDashboardDTOS = new ArrayList<>();

                for (Booking booking : bookings) {

                    String sessionTime = booking.getSessionStartTime().atZone(ZoneId.of(clientProfile.getTimeZone())).format(formatter);

                    TherapistProfile therapistProfile = therapistProfileRepository.findById(booking.getTherapistId()).orElseThrow(() -> new ResourceNotFoundException(THERAPIST_NOT_FOUND_ID+ booking.getTherapistId()));

                    Instant sessionStartTime = booking.getSessionStartTime();
                    Instant sessionEndTime = sessionStartTime.plus(Duration.ofMinutes(60));

                    String status;
                    if (timeNow.isBefore(sessionStartTime)) {
                        status = "upcoming";
                    } else if (timeNow.isAfter(sessionEndTime)) {
                        status = "completed";
                    } else {
                        status = "ongoing";
                    }

                    var clientDashboardDto = ClientDashboardDTO.builder()
                            .sessionTime(sessionTime)
                            .status(status)
                            .bookingId(booking.getId())
                            .therapistName(therapistProfile.getName())
                            .meetType(booking.getConnectMethod())
                            .bookingId(booking.getId())
                            .build();

                    clientDashboardDTOS.add(clientDashboardDto);
                }

                var clientAppointmentsDto = ClientAppointmentsDTO.builder()
                        .clientId(clientProfile.getId())
                        .clientName(clientProfile.getName())
                        .clientDashboardDTOS(clientDashboardDTOS)
                        .build();

                clientAppointmentsDTOS.add(clientAppointmentsDto);

            }

            return CommonResponse.<List<ClientAppointmentsDTO>>builder()
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message(LOADED_ALL_CLIENTS_SESSIONS)
                    .data(clientAppointmentsDTOS)
                    .build();

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_LOADING_ALL_CLIENTS_SESSIONS + e.getMessage());
        }


    }

    public CommonResponse<List<TherapistOverviewDTO>> getTherapistsOverview() throws UnexpectedServerException {

        try {

            List<TherapistProfile> therapistProfiles = therapistProfileRepository.findAll();

            if (therapistProfiles.isEmpty()) {

                return CommonResponse.<List<TherapistOverviewDTO>>builder()
                        .status(STATUS_FALSE)
                        .statusCode(SUCCESS_CODE)
                        .message(N0_THERAPIST_AVAILABLE)
                        .data(List.of())
                        .build();
            }

            Instant timeNow = Instant.now();

            var formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

            List<TherapistOverviewDTO> therapistOverviewDTOS = therapistProfiles.stream()
                    .map(therapistProfile -> {

                        List<Booking> bookings = bookingRepository.findByTherapistIdAndPaymentStatus(therapistProfile.getId(), PaymentStatus.COMPLETED);

                        // filter must return a boolean true or false
                        Long futureSessions = bookings.stream()
                                .filter(booking -> timeNow.isBefore(booking.getSessionStartTime()))
                                .count();

                        Long completedSessions = bookings.stream()
                                .filter(booking -> {

                                    Instant sessionStartTime = booking.getSessionStartTime();
                                    Instant sessionEndTime = sessionStartTime.plus(Duration.ofMinutes(60));

                                    return timeNow.isAfter(sessionEndTime);
                                })
                                .count();

                        return TherapistOverviewDTO.builder()
                                .therapistId(therapistProfile.getId())
                                .therapistName(therapistProfile.getName())
                                .joinDate(therapistProfile.getCreatedAt().atZone(ZoneId.of(therapistProfile.getTimezone())).format(formatter))
                                .futureSessions(futureSessions)
                                .completedSessions(completedSessions)
                                .accountStatus(therapistProfile.getAccountStatus())
                                .profileUrl(therapistProfile.getProfileUrl())
                                .build();


                    })
                    .toList();

            return CommonResponse.<List<TherapistOverviewDTO>>builder()
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message( LOADED_ALL_THERAPISTS_DETAILS)
                    .data(therapistOverviewDTOS)
                    .build();

        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_LOADING_THERAPISTS_DETAILS+ e.getMessage());
        }


    }

    public CommonResponse<List<ClientOverviewDTO>> getClientsOverview() throws UnexpectedServerException {

        try {

            List<ClientProfile> clientProfiles = clientProfileRepository.findAll();

            if (clientProfiles.isEmpty()) {

                return CommonResponse.<List<ClientOverviewDTO>>builder()
                        .status(STATUS_FALSE)
                        .statusCode(SUCCESS_CODE)
                        .message(NO_CLIENT_AVAILABLE)
                        .data(List.of())
                        .build();
            }

            Instant timeNow = Instant.now();

            var formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

            List<ClientOverviewDTO> clientOverviewDTOS = clientProfiles.stream()
                    .map(clientProfile -> {

                        List<Booking> bookings = bookingRepository.findByClientIdAndPaymentStatus(clientProfile.getId(), PaymentStatus.COMPLETED);

                        // filter must return a boolean true or false
                        Long futureSessions = bookings.stream()
                                .filter(booking -> timeNow.isBefore(booking.getSessionStartTime()))
                                .count();

                        Long completedSessions = bookings.stream()
                                .filter(booking -> {

                                    Instant sessionStartTime = booking.getSessionStartTime();
                                    Instant sessionEndTime = sessionStartTime.plus(Duration.ofMinutes(60));

                                    return timeNow.isAfter(sessionEndTime);
                                })
                                .count();

                        return ClientOverviewDTO.builder()
                                .clientId(clientProfile.getId())
                                .clientName(clientProfile.getName())
                                .joinDate(clientProfile.getCreatedAt().atZone(ZoneId.of(clientProfile.getTimeZone())).format(formatter))
                                .futureSessions(futureSessions)
                                .completedSessions(completedSessions)
                                .accountStatus(AccountStatus.ACTIVE)
                                .profileUrl(clientProfile.getProfileUrl())
                                .build();


                    })
                    .toList();

            return CommonResponse.<List<ClientOverviewDTO>>builder()
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message(LOADED_ALL_CLIENTS_DETAILS)
                    .data(clientOverviewDTOS)
                    .build();

        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_LOADING_CLIENTS_DETAILS + e.getMessage());
        }

    }

    public CommonResponse<AdminDashboardDTO> updateTherapistApprovalStatus(Long therapistId, ApprovalRequestDTO request) throws ResourceNotFoundException, UnexpectedServerException {


        try {

            TherapistProfile therapist = therapistProfileRepository.findById(therapistId).orElseThrow(() -> new ResourceNotFoundException(THERAPIST_NOT_FOUND_ID + therapistId));

            String action = request.getStatus();

            if ("APPROVED".equalsIgnoreCase(action)) {

                therapist.setApprovalStatus(ApprovalStatus.ACCEPTED);
                therapist.setAccountStatus(AccountStatus.ACTIVE);

            } else if ("REJECTED".equalsIgnoreCase(action)) {

                therapist.setApprovalStatus(ApprovalStatus.REJECTED);
                therapist.setAccountStatus(AccountStatus.INACTIVE);

            } else {
                throw new InvalidFieldValueException(ACTION_APPROVED_OR_REJECTED);
            }

            therapistProfileRepository.save(therapist);

            String subject;
            String body;

            if (ApprovalStatus.ACCEPTED.equals(therapist.getApprovalStatus())) {
                subject = "Your Therapist Application is Approved ✅";
                body = String.format("""
                Hi %s,
                
                Congratulations! 🎉
                
                Your application to become a therapist on TherapistBooster has been reviewed and approved by our team.
                
                You can now:
                ✔ Log in to your dashboard  
                ✔ Manage your availability  
                ✔ Start receiving therapistship bookings
                
                Thank you for being part of our mission to empower learners!
                
                Warm regards,  
                Team TherapistBooster  
                """, therapist.getName());
            } else {
                subject = "Your Therapist Application is Not Approved ❌";
                body = String.format("""
                Hi %s,
                
                Thank you for applying to be a therapist on TherapistBooster.
                
                After carefully reviewing your application, we regret to inform you that it has not been approved at this time.
                
                This may be due to:
                - Incomplete or unclear information  
                - A mismatch with our current therapist requirements
                
                We truly appreciate your interest, and you're welcome to re-apply in the future with updated details.
                
                Warm wishes,  
                Team TherapistBooster  
                """, therapist.getName());
            }

            emailService.sendEmail(therapist.getEmail(), subject, body);

            return CommonResponse.<AdminDashboardDTO>builder()
                    .status(STATUS_TRUE)
                    .message("Therapist " + action + " successfully")
                    .statusCode(200)
                    .data(getAdminDashboardDetails().getData())
                    .build();

        }catch (ResourceNotFoundException | InvalidFieldValueException e){
            throw e;
        }catch (Exception e){
            throw new UnexpectedServerException( ERROR_APPROVING_OR_REJECTING_THERAPIST + e.getMessage());
        }
    }

}