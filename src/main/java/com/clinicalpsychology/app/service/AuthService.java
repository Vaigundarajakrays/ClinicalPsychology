package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.dto.ChangePasswordRequest;
import com.clinicalpsychology.app.dto.LoginRequest;
import com.clinicalpsychology.app.dto.LoginResponse;
import com.clinicalpsychology.app.enumUtil.OtpPurpose;
import com.clinicalpsychology.app.enumUtil.Role;
import com.clinicalpsychology.app.exceptionHandling.OtpException;
import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.*;
import com.clinicalpsychology.app.repository.*;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.security.JwtService;
import com.clinicalpsychology.app.util.CommonFiles;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UsersRepository usersRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CommonFiles commonFiles;
    private final PasswordEncoder passwordEncoder;
    private final OtpRepository otpRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final TherapistProfileRepository therapistProfileRepository;
    private final SubscribeRepository subscribeRepository;
    private final EmailService emailService;

    public CommonResponse<LoginResponse> authenticate(LoginRequest request) throws UnexpectedServerException, ResourceNotFoundException {

        Users user = usersRepository.findByEmailId(request.getEmailId());

        if (user == null) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + request.getEmailId());
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmailId(), request.getPassword())
            );

            String token = jwtService.generateToken(user);

            String name = null;
            Long id = null;
            String timezone = null;
            String profileUrl = null;
            boolean isSubscribed = false;

            var formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

            if (user.getRole() == Role.CLIENT) {
                ClientProfile client = clientProfileRepository.findByEmail(user.getEmailId())
                        .orElseThrow(() -> new ResourceNotFoundException(CLIENT_NOT_FOUND_EMAIL + user.getEmailId()));

                Subscribe subscribe = subscribeRepository.findByEmail(client.getEmail());
                if(!(subscribe ==null)){
                    isSubscribed=true;
                }

                name = client.getName();
                id = client.getId();
                timezone = client.getTimeZone();
                profileUrl = client.getProfileUrl();
            }

            if (user.getRole() == Role.THERAPIST) {
                TherapistProfile therapist = therapistProfileRepository.findByEmail(user.getEmailId())
                        .orElseThrow(() -> new ResourceNotFoundException(CLIENT_NOT_FOUND_EMAIL + user.getEmailId()));

                Subscribe subscribe = subscribeRepository.findByEmail(therapist.getEmail());
                if(!(subscribe ==null)){
                    isSubscribed=true;
                }

                name = therapist.getName();
                id = therapist.getId();
                timezone = therapist.getTimezone();
                profileUrl = therapist.getProfileUrl();
            }

            LoginResponse loginResponse = LoginResponse.builder()
                    .token(token)
                    .role(user.getRole())
                    .name(name)
                    .id(id)
                    .timezone(timezone)
                    .profileUrl(profileUrl)
                    .isSubscribed(isSubscribed)
                    .build();

            return CommonResponse.<LoginResponse>builder()
                    .message(LOGIN_SUCCESS)
                    .status(STATUS_TRUE)
                    .data(loginResponse)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_LOGGING_IN + e.getMessage());
        }
    }


    public CommonResponse<String> sendOtp(String email, OtpPurpose otpPurpose) throws UnexpectedServerException {
        try {
            if (otpPurpose.isTherapistRegister()) {
                if (therapistProfileRepository.existsByEmail(email)) {
                    throw new OtpException("You are already registered as a therapist", "THERAPIST_EXISTS");
                }

                if (clientProfileRepository.existsByEmail(email)) {
                    throw new OtpException("You are already registered as a client with this account. Please proceed with another account.", "CLIENT_EXISTS");
                }

                if (usersRepository.existsByEmailId(email)) {
                    throw new OtpException("Your email already exists in our system", "USER_EXISTS");
                }
            }

            if (otpPurpose.isClientRegister()) {
                if (clientProfileRepository.existsByEmail(email)) {
                    throw new OtpException("You are already registered as a client", "CLIENT_EXISTS");
                }

                if (therapistProfileRepository.existsByEmail(email)) {
                    throw new OtpException("You are already registered as a therapist with this account. Please proceed with another account.", "THERAPIST_EXISTS");
                }

                if (usersRepository.existsByEmailId(email)) {
                    throw new OtpException("Your email already exists in our system", "USER_EXISTS");
                }
            }

            String otp = commonFiles.generateOTP(6);
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

            Otp otpEntity = otpRepository.findByEmail(email)
                    .map(existingOtp -> {
                        existingOtp.setOtp(otp);
                        existingOtp.setExpiryTime(expiryTime);
                        return existingOtp;
                    })
                    .orElse(new Otp(email, otp, expiryTime));

            otpRepository.save(otpEntity);

            // Send email
            emailService.sendOTPUser(email, otp, otpPurpose);

            return CommonResponse.<String>builder()
                    .message(OTP_SENT_SUCCESS)
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (MailAuthenticationException | MailSendException | OtpException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_SENDING_OTP + e.getMessage());
        }
    }


//    public CommonResponse<String> sendOtp(String email) throws UnexpectedServerException {
//
//        try {
//
//            String otp = commonFiles.generateOTP(6);
//            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);
//
//            Otp otpEntity = otpRepository.findByEmail(email)
//                    .map(existingOtp -> {
//                        existingOtp.setOtp(otp);
//                        existingOtp.setExpiryTime(expiryTime);
//                        return existingOtp;
//                    })
//                    .orElse(new Otp(email, otp, expiryTime));
//
//            otpRepository.save(otpEntity);
//
//            commonFiles.sendOTPUser(email, otp);
//
//            return CommonResponse.<String>builder()
//                    .message(OTP_SENT_SUCCESS)
//                    .status(STATUS_TRUE)
//                    .statusCode(SUCCESS_CODE)
//                    .build();
//
//        } catch (MailAuthenticationException | MailSendException | OtpException e){
//            throw e;
//        } catch (Exception e) {
//            throw new UnexpectedServerException(ERROR_SENDING_OTP + e.getMessage());
//        }
//    }

    public CommonResponse<String> verifyOtp(String email, String otp) throws UnexpectedServerException {

        try {

            Otp otpEntity = otpRepository.findByEmailAndOtp(email, otp)
                    .orElseThrow(() -> new OtpException(INVALID_OTP, IN_VALID_OTP));

            if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
                throw new OtpException(OTP_EXPIRED, OTP_EXPIRE);
            }

            return CommonResponse.<String>builder()
                    .status(true)
                    .message(OTP_VERIFIED_SUCCESSFULLY)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (OtpException e){
            throw e;
        } catch (Exception e){
            throw new UnexpectedServerException(ERROR_VERIFYING_OTP + e.getMessage());
        }

    }

    public CommonResponse<String> changePassword(ChangePasswordRequest changePasswordRequest) throws ResourceNotFoundException, UnexpectedServerException {

        Users user = usersRepository.findByEmailId(changePasswordRequest.getEmail());

        if(user == null){
            throw new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + changePasswordRequest.getEmail());
        }

        try {

            user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
            usersRepository.save(user);

            return CommonResponse.<String>builder()
                    .message(PASSWORD_CHANGE_SUCCESS)
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .data(PASSWORD_UPDATED)
                    .build();

        }

        catch (Exception e){
            throw new UnexpectedServerException(ERROR_UPDATING_NEW_PASSWORD + e.getMessage());
        }
    }


}
