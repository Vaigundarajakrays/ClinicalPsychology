package com.clinicalpsychology.app.exceptionHandling;

public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message); // Explicit call to the superclass constructor
    }
}
