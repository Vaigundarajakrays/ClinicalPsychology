package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.enumUtil.OtpPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String mailFrom;

    @Async
    public void sendEmail(String to, String subject, String text ) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(mailFrom);
        mailSender.send(message);

    }

    // test
    @Async
    public void sendOTPUser(String email, String otp, OtpPurpose purpose) {
        String subject;
        String body;

        switch (purpose) {
            case THERAPIST_REGISTER -> {
                subject = "Therapist Registration - Verify Your Email";
                body = "Dear Therapist,\n\n" +
                        "Thank you for signing up as a therapist at Therapist Boosters! Please verify your email address using the One-Time Password (OTP) below:\n\n" +
                        "OTP: " + otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Warm regards,\nThe Therapist Boosters Team";
            }
            case CLIENT_REGISTER -> {
                subject = "Client Registration - Verify Your Email";
                body = "Dear Client,\n\n" +
                        "Thanks for joining Therapist Boosters! Please verify your email with the OTP below:\n\n" +
                        "OTP: " + otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Warm regards,\nThe Therapist Boosters Team";
            }
            case FORGOT_PASSWORD -> {
                subject = "Reset Your Password";
                body = "Hey there,\n\n" +
                        "You requested a password reset for your Therapist Boosters account. Use the OTP below to proceed:\n\n" +
                        "OTP: " + otp + "\n\n" +
                        "The OTP will expire in 5 minutes.\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Warm regards,\nThe Therapist Boosters Team";
            }
            default -> {
                subject = "Therapist Boosters OTP";
                body = "Your OTP is: " + otp;
            }
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(mailFrom);
        mailSender.send(message);
    }

}
