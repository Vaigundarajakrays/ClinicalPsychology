package com.clinicalpsychology.app.enums;

import com.clinicalpsychology.app.exception.InvalidFieldValueException;

public enum OtpPurpose {

    THERAPIST_REGISTER,
    CLIENT_REGISTER,
    FORGOT_PASSWORD;

    public static OtpPurpose from(String value){
        return switch (value){
            case "therapist-register" -> THERAPIST_REGISTER;
            case "forgot-password" -> FORGOT_PASSWORD;
            case "client-register" -> CLIENT_REGISTER;
            default -> throw new InvalidFieldValueException("Purpose should be either therapist-register or client-register or forgot-password");
        };
    }

    public boolean isTherapistRegister(){
        return this==THERAPIST_REGISTER;
    }

    public boolean isClientRegister(){
        return this==CLIENT_REGISTER;
    }

    public boolean isForgotPassword(){
        return this==FORGOT_PASSWORD;
    }
}
