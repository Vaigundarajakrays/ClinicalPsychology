package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.dto.AllTherapistsResponseDTO;
import com.clinicalpsychology.app.dto.TherapistDashboardDTO;
import com.clinicalpsychology.app.dto.TherapistProfileDTO;
import com.clinicalpsychology.app.enumUtil.AccountStatus;
import com.clinicalpsychology.app.enumUtil.ApprovalStatus;
import com.clinicalpsychology.app.enumUtil.PaymentStatus;
import com.clinicalpsychology.app.enumUtil.Role;
import com.clinicalpsychology.app.exceptionHandling.InvalidFieldValueException;
import com.clinicalpsychology.app.exceptionHandling.ResourceAlreadyExistsException;
import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.*;
import com.clinicalpsychology.app.repository.BookingRepository;
import com.clinicalpsychology.app.repository.ClientProfileRepository;
import com.clinicalpsychology.app.repository.TherapistProfileRepository;
import com.clinicalpsychology.app.repository.UsersRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
@RequiredArgsConstructor
public class TherapistProfileService {

    private final TherapistProfileRepository therapistProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    //@Transactional
    //Spring only rolls back for unchecked exceptions (aka runtime exceptions) unless you explicitly tell it otherwise.
    @Transactional
    public CommonResponse<String> registerTherapist(TherapistProfileDTO therapistProfileDTO) throws UnexpectedServerException {
        try {
            if (therapistProfileRepository.existsByEmailOrPhone(therapistProfileDTO.getEmail(), therapistProfileDTO.getPhone())) {
                throw new ResourceAlreadyExistsException(EMAIL_PHONE_EXISTS);
            }

            if (clientProfileRepository.existsByEmail(therapistProfileDTO.getEmail())){
                throw new ResourceAlreadyExistsException(ALREADY_REGISTERED_EMAIL);
            }

            String timezone = therapistProfileDTO.getTimezone();
            if (timezone == null || timezone.isBlank()) {
                throw new InvalidFieldValueException(TIMEZONE_REQUIRED);
            }

            // Convert DTO to entity
            TherapistProfile therapist = TherapistProfile.builder()
                    .name(therapistProfileDTO.getName())
                    .phone(therapistProfileDTO.getPhone())
                    .email(therapistProfileDTO.getEmail())
                    .linkedinUrl(therapistProfileDTO.getLinkedinUrl())
                    .profileUrl(therapistProfileDTO.getProfileUrl())
                    .resumeUrl(therapistProfileDTO.getResumeUrl())
                    .yearsOfExperience(therapistProfileDTO.getYearsOfExperience())
                    .password(therapistProfileDTO.getPassword()) // Will hash below
                    .categories(therapistProfileDTO.getCategories())
                    .education(therapistProfileDTO.getEducation())
                    .languages(therapistProfileDTO.getLanguages())
                    .summary(therapistProfileDTO.getSummary())
                    .amount(therapistProfileDTO.getAmount())
                    .terms(therapistProfileDTO.getTerms())
                    .termsAndConditions(therapistProfileDTO.getTermsAndConditions())
                    .location(therapistProfileDTO.getLocation())
                    .timezone(timezone)
                    .build();

            List<FixedTimeSlotNew> timeSlots = therapistProfileDTO.getTimeSlots().stream().map(slotStr -> {
                try {
                    String trimmed = slotStr.trim(); // Clean up extra spaces
                    LocalTime localTime = LocalTime.parse(trimmed); // Parse "HH:mm"
                    LocalDate localDate = LocalDate.now(ZoneId.of(timezone)); // Use today in therapist's timezone
                    ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate, localTime, ZoneId.of(timezone));
                    Instant utcInstant = zonedDateTime.toInstant();

                    return FixedTimeSlotNew.builder()
                            .timeStart(utcInstant)
                            .therapist(therapist)
                            .build();
                } catch (DateTimeParseException e) {
                    throw new InvalidFieldValueException(INVALID_TIME_TIMESLOTS + slotStr);
                }
            }).toList();

            therapist.setTimeSlots(timeSlots);

            // Encrypt password
            String hashedPassword = passwordEncoder.encode(therapistProfileDTO.getPassword());
            therapist.setPassword(hashedPassword);

            // Save therapist
            TherapistProfile savedTherapist = therapistProfileRepository.save(therapist);

            // Create and save user
            Users user = Users.builder()
                    .emailId(therapist.getEmail())
                    .role(Role.THERAPIST)
                    .password(hashedPassword)
                    .build();
            usersRepository.save(user);

            // Send confirmation email
            String subject = "Therapist Registration Received - Awaiting Approval";
            String body = String.format("""
            Hi %s,
            
            Thank you for registering as a therapist on TherapistBooster. 🎉
            
            Your registration was successful, and our team is currently reviewing your application.
            
            🕒 What’s next?
            - Our admin team will verify your profile details.
            - You will receive an email once your account is approved or rejected.
            
            We appreciate your willingness to guide and empower clients.
            
            Warm regards,  
            Team TherapistBooster  
            """, therapist.getName());

            emailService.sendEmail(therapist.getEmail(), subject, body);


            return CommonResponse.<String>builder()
                    .message("You have registered successfully")
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .data("Role: " + user.getRole().toString())
                    .build();

        } catch (ResourceAlreadyExistsException | InvalidFieldValueException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedServerException(REGISTRATION_FAILED + e.getMessage());
        }
    }

    public CommonResponse<TherapistProfileDTO> getProfileDetails(Long id) throws ResourceNotFoundException, UnexpectedServerException {

        try {

            TherapistProfile therapistNew = therapistProfileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(THERAPIST_NOT_FOUND_ID + id));

            List<String> timeSlots = therapistNew.getTimeSlots().stream()
                    .map(slot -> slot.getTimeStart()
                            .atZone(ZoneId.of(therapistNew.getTimezone()))
                            .toLocalTime()
                            .toString())
                    .toList();


            var therapistDto = TherapistProfileDTO.builder()
                    .therapistId(therapistNew.getId())
                    .name(therapistNew.getName())
                    .email(therapistNew.getEmail())
                    .amount(therapistNew.getAmount())
                    .profileUrl(therapistNew.getProfileUrl())
                    .categories(therapistNew.getCategories())
                    .linkedinUrl(therapistNew.getLinkedinUrl())
                    .terms(therapistNew.getTerms())
                    .education(therapistNew.getEducation())
                    .languages(therapistNew.getLanguages())
                    .summary(therapistNew.getSummary())
                    .description(therapistNew.getDescription())
                    .resumeUrl(therapistNew.getResumeUrl())
                    .yearsOfExperience(therapistNew.getYearsOfExperience())
                    .termsAndConditions(therapistNew.getTermsAndConditions())
                    .phone(therapistNew.getPhone())
                    .profileUrl(therapistNew.getProfileUrl())
                    .timezone(therapistNew.getTimezone())
                    .timeSlots(timeSlots)
                    .location(therapistNew.getLocation())
                    .accountStatus(therapistNew.getAccountStatus())
                    .build();


            return CommonResponse.<TherapistProfileDTO>builder()
                    .status(STATUS_TRUE)
                    .message(DETAILS_LOADED_SUCCESSFULLY)
                    .data(therapistDto)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (ResourceNotFoundException e){
            throw e;
        } catch (Exception e){
            throw new UnexpectedServerException(LOADING_THERAPIST_DETAILS + e.getMessage());
        }
    }

    //We are getting only partial details not the whole obj, that's why we used patch
    @Transactional
    public CommonResponse<TherapistProfileDTO> updateTherapistProfile(Long id, TherapistProfileDTO therapistDto)
            throws ResourceNotFoundException, UnexpectedServerException {
        try {
            TherapistProfile therapist = therapistProfileRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(THERAPIST_NOT_FOUND_ID+ id));

            // Update only non-null fields (Partial update / PATCH style)
            if (therapistDto.getName() != null) therapist.setName(therapistDto.getName());
            if (therapistDto.getPhone() != null) therapist.setPhone(therapistDto.getPhone());
            if (therapistDto.getEmail() != null) therapist.setEmail(therapistDto.getEmail());
            if (therapistDto.getLinkedinUrl() != null) therapist.setLinkedinUrl(therapistDto.getLinkedinUrl());
            if (therapistDto.getProfileUrl() != null) therapist.setProfileUrl(therapistDto.getProfileUrl());
            if (therapistDto.getResumeUrl() != null) therapist.setResumeUrl(therapistDto.getResumeUrl());
            if (therapistDto.getYearsOfExperience() != null) therapist.setYearsOfExperience(therapistDto.getYearsOfExperience());
            if (therapistDto.getCategories() != null) therapist.setCategories(therapistDto.getCategories());
            if(therapistDto.getEducation() != null) therapist.setEducation(therapistDto.getEducation());
            if(therapistDto.getLanguages() != null) therapist.setLanguages(therapistDto.getLanguages());
            if (therapistDto.getSummary() != null) therapist.setSummary(therapistDto.getSummary());
            if (therapistDto.getAmount() != null) therapist.setAmount(therapistDto.getAmount());
            if (therapistDto.getTerms() != null) therapist.setTerms(therapistDto.getTerms());
            if (therapistDto.getLocation() != null) therapist.setLocation(therapistDto.getLocation());
            if (therapistDto.getTermsAndConditions() != null) therapist.setTermsAndConditions(therapistDto.getTermsAndConditions());
            if (therapistDto.getTimezone() != null) therapist.setTimezone(therapistDto.getTimezone());

            String effectiveTimezone = therapistDto.getTimezone() != null ? therapistDto.getTimezone() : therapist.getTimezone();

            // When updating it, we need time zone that's why we get effective timezone
            if (therapistDto.getTimeSlots() != null) {
                List<FixedTimeSlotNew> updatedTimeSlots = therapistDto.getTimeSlots().stream()
                        .map(timeStr -> {
                            try {
                                LocalTime localTime = LocalTime.parse(timeStr.trim());
                                LocalDate today = LocalDate.now(ZoneId.of(effectiveTimezone));
                                ZonedDateTime zoned = ZonedDateTime.of(today, localTime, ZoneId.of(effectiveTimezone));
                                return FixedTimeSlotNew.builder()
                                        .therapist(therapist)
                                        .timeStart(zoned.toInstant())
                                        .build();
                            } catch (DateTimeParseException e) {
                                throw new InvalidFieldValueException(INVALID_TIME_FORMAT + timeStr);
                            }
                        }).toList();

                therapist.getTimeSlots().clear();
                therapist.getTimeSlots().addAll(updatedTimeSlots);
            }

            TherapistProfile updatedTherapist = therapistProfileRepository.save(therapist);

            // Build timeSlots back to String list for response
            List<String> updatedTimeSlotsStr = updatedTherapist.getTimeSlots().stream()
                    .map(slot -> slot.getTimeStart()
                            .atZone(ZoneId.of(updatedTherapist.getTimezone()))
                            .toLocalTime()
                            .toString())
                    .toList();

            // Prepare response DTO for therapist
            TherapistProfileDTO responseDto = TherapistProfileDTO.builder()
                    .name(updatedTherapist.getName())
                    .phone(updatedTherapist.getPhone())
                    .email(updatedTherapist.getEmail())
                    .linkedinUrl(updatedTherapist.getLinkedinUrl())
                    .profileUrl(updatedTherapist.getProfileUrl())
                    .resumeUrl(updatedTherapist.getResumeUrl())
                    .yearsOfExperience(updatedTherapist.getYearsOfExperience())
                    .categories(updatedTherapist.getCategories())
                    .summary(updatedTherapist.getSummary())
                    .amount(updatedTherapist.getAmount())
                    .terms(updatedTherapist.getTerms())
                    .termsAndConditions(updatedTherapist.getTermsAndConditions())
                    .timezone(updatedTherapist.getTimezone())
                    .location(updatedTherapist.getLocation())
                    .timeSlots(updatedTimeSlotsStr)
                    .accountStatus(updatedTherapist.getAccountStatus())
                    .build();

            return CommonResponse.<TherapistProfileDTO>builder()
                    .status(true)
                    .message("You have successfully updated your profile")
                    .statusCode(SUCCESS_CODE)
                    .data(responseDto)
                    .build();

        } catch (ResourceNotFoundException | InvalidFieldValueException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedServerException(FAILED_UPDATE_PROFILE + e.getMessage());
        }
    }


    public CommonResponse<List<TherapistDashboardDTO>> getAppointments(Long therapistId) throws ResourceNotFoundException, UnexpectedServerException {

        try {

            TherapistProfile therapist = therapistProfileRepository.findById(therapistId).orElseThrow(() -> new ResourceNotFoundException(THERAPIST_NOT_FOUND_ID + therapistId));

            List<Booking> bookings = bookingRepository.findByTherapistIdAndPaymentStatus(therapistId, PaymentStatus.COMPLETED);

            if (bookings.isEmpty()) {
                return CommonResponse.<List<TherapistDashboardDTO>>builder()
                        .status(STATUS_TRUE)
                        .statusCode(SUCCESS_CODE)
                        .message(DO_NOT_HAVE_ANY_APPOINTMENTS)
                        .data(List.of())
                        .build();
            }

            var formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

            Instant timeNow = Instant.now();

            // I didn't used stream because
            // The issue is that you're using orElseThrow(...) inside a lambda in a stream() — and that method throws a checked exception (ResourceNotFoundException).
            // Java doesn't allow checked exceptions to be thrown from inside lambda expressions unless you handle them.

            List<TherapistDashboardDTO> appointments = new ArrayList<>();
            for (Booking booking : bookings) {

                // To find client name
                ClientProfile client = clientProfileRepository.findById(booking.getClientId())
                        .orElseThrow(() -> new ResourceNotFoundException(CLIENT_NOT_FOUND_ID + booking.getClientId() + BOOKING_ID + booking.getId()));

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


                // To find session time in therapist time zone
                ZonedDateTime sessionTime = sessionStartTime.atZone(ZoneId.of(therapist.getTimezone()));
                String session = sessionTime.format(formatter);

                appointments.add(TherapistDashboardDTO.builder()
                        .clientName(client.getName())
                        .sessionTime(session)
                        .sessionName(booking.getCategory())
                        .sessionDuration("1 Hr")
                        .meetType(booking.getConnectMethod())
                        .status(status)
                        .therapistMeetLink(booking.getTherapistMeetLink())
                        .build());
            }

            return CommonResponse.<List<TherapistDashboardDTO>>builder()
                    .statusCode(SUCCESS_CODE)
                    .message(LOADED_THERAPIST_APPOINTMENTS)
                    .data(appointments)
                    .status(STATUS_TRUE)
                    .build();



        } catch (ResourceNotFoundException e){
            throw e;
        } catch (Exception e){
            throw new UnexpectedServerException(ERROR_LOADING_APPOINTMENTS + e.getMessage());
        }
    }

//    public CommonResponse<List<TherapistProfileDTO>> getTherapistsByCategoryName(String categoryName) {
//
//        // 1. Fetch all therapists
//        List<TherapistProfile> allTherapists = therapistProfileRepository.findAll();
//
//        // 2. Filter therapists manually based on category string (case-insensitive)
//        List<TherapistProfile> filtered = allTherapists.stream()
//                .filter(m -> m.getCategories() != null &&
//                        m.getCategories().stream()
//                                .map(String::trim)
//                                .anyMatch(c -> c.equalsIgnoreCase(categoryName.trim())))
//                .toList();
//
//        // 3. Map to DTOs
//        List<TherapistProfileDTO> dtos = filtered.stream()
//                .map(this::mapToDTO)
//                .toList();
//
//        return CommonResponse.<List<TherapistProfileDTO>>builder()
//                .message("Therapists filtered by category: " + categoryName)
//                .status(true)
//                .statusCode(200)
//                .data(dtos)
//                .build();
//    }

    // It is returning timeslot as null
//    private TherapistProfileDTO mapToDTO(TherapistProfile therapist) {
//
//        var formatter = DateTimeFormatter.ofPattern("HH:mm");
//
//        return TherapistProfileDTO.builder()
//                .therapistId(therapist.getId())
//                .name(therapist.getName())
////                .phone(therapist.getPhone())
////                .email(therapist.getEmail())
////                .linkedinUrl(therapist.getLinkedinUrl())
//                .profileUrl(therapist.getProfileUrl())
////                .resumeUrl(therapist.getResumeUrl())
////                .yearsOfExperience(therapist.getYearsOfExperience())
//                .categories(therapist.getCategories())
//                .summary(therapist.getSummary())
//                .description(therapist.getDescription())
////                .amount(therapist.getAmount())
////                .terms(therapist.getTerms())
////                .termsAndConditions(therapist.getTermsAndConditions())
////                .timezone(therapist.getTimezone())
////                .accountStatus(therapist.getAccountStatus())
////                .approvalStatus(therapist.getApprovalStatus())
////                .timeSlots(null)
//                .build();
//    }

    // it is returning timeslot as null
    // It returns only approved therapists, other apis may not, clarify
    public CommonResponse<List<AllTherapistsResponseDTO>> getAllTherapists() throws UnexpectedServerException {

        try {

            List<TherapistProfile> therapists = therapistProfileRepository.findAllByApprovalStatusAndAccountStatus(ApprovalStatus.ACCEPTED, AccountStatus.ACTIVE);

            if (therapists.isEmpty()) {
                return CommonResponse.<List<AllTherapistsResponseDTO>>builder()
                        .status(STATUS_FALSE)
                        .statusCode(SUCCESS_CODE)
                        .message("No therapists found")
                        .data(List.of())
                        .build();
            }

            List<AllTherapistsResponseDTO> dtos = therapists.stream()
                    .map(this::toAllTherapistsResponseDTO)
                    .toList();

            return CommonResponse.<List<AllTherapistsResponseDTO>>builder()
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message("Therapists fetched successfully")
                    .data(dtos)
                    .build();

        } catch (Exception e){
            throw new UnexpectedServerException("Error while loading therapists:" + e.getMessage());
        }

    }

    private AllTherapistsResponseDTO toAllTherapistsResponseDTO(TherapistProfile therapistProfile){

        return AllTherapistsResponseDTO.builder()
                .therapistId(therapistProfile.getId())
                .name(therapistProfile.getName())
                .profileUrl(therapistProfile.getProfileUrl())
                .categories(therapistProfile.getCategories())
                .summary(therapistProfile.getSummary())
                .build();
    }

    public CommonResponse<List<TherapistProfile>> search(String name, String location, String category, String priceRange) throws UnexpectedServerException {

        try {

            if (name == null || name.trim().isEmpty() || "null".equalsIgnoreCase(name)) name = null;
            if (location == null || location.trim().isEmpty() || "null".equalsIgnoreCase(location)) location = null;
            if (category == null || category.trim().isEmpty() || "null".equalsIgnoreCase(category)) category = null;
            if (priceRange == null || priceRange.trim().isEmpty() || "null".equalsIgnoreCase(priceRange)) priceRange = null;

            Double minPrice = null;
            Double maxPrice = null;

            // Parse price=1-20 into minPrice and maxPrice
            if (priceRange != null) {
                try {
                    if (priceRange.contains("-")) {
                        // Case: price = 100-300
                        String[] parts = priceRange.split("-");
                        minPrice = Double.parseDouble(parts[0]);
                        maxPrice = Double.parseDouble(parts[1]);
                    } else if (priceRange.endsWith("+")){
                        // Case: price = 500+
                        String value = priceRange.substring(0, priceRange.length() - 1);
                        minPrice = Double.parseDouble(value);
                    }else {
                        // Case: price = 200 (single value)
                        double price = Double.parseDouble(priceRange);
                        minPrice = price;
                        maxPrice = price;
                    }
                } catch (NumberFormatException e) {
                    throw new InvalidFieldValueException("Invalid price format. Use price=1-20 or price=200");
                }
            }

            // Call DB filter
            List<TherapistProfile> result = therapistProfileRepository.searchTherapists(name, location, minPrice, maxPrice, category);

            if (result.isEmpty()) {
                return CommonResponse.<List<TherapistProfile>>builder()
                        .status(STATUS_FALSE)
                        .statusCode(SUCCESS_CODE)
                        .message("No therapists found")
                        .data(List.of())
                        .build();
            }

            return CommonResponse.<List<TherapistProfile>>builder()
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message("Therapists fetched successfully")
                    .data(result)
                    .build();

        } catch (InvalidFieldValueException e){
            throw e;
        } catch (Exception e){
            throw new UnexpectedServerException("Error while loading therapists: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<String> deleteTherapist(Long id) throws ResourceNotFoundException {

        TherapistProfile therapistProfile = therapistProfileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Therapist not found with id: " + id));

        try {

            usersRepository.deleteByEmailId(therapistProfile.getEmail());
            therapistProfileRepository.deleteById(id);

            return ResponseEntity.ok("Successfully deleted the therapist with id: " + id);

        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while deleting therapist");
        }
    }
}
