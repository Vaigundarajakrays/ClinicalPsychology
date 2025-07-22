package com.clinicalpsychology.app.exceptionHandling;

public class OtpException extends RuntimeException {
    private final String errorCode;

    public OtpException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
