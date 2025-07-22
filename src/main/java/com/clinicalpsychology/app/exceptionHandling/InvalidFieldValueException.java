package com.clinicalpsychology.app.exceptionHandling;

public class InvalidFieldValueException extends RuntimeException {
    public InvalidFieldValueException(String message) {
        super(message);
    }
}

