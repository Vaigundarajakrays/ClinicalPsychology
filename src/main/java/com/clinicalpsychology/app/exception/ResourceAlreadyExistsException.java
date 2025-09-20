package com.clinicalpsychology.app.exception;

public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message); // Explicit call to the superclass constructor
    }
}
