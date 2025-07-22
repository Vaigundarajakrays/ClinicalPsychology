package com.clinicalpsychology.app.exceptionHandling;

public class ResourceNotFoundException extends Exception {

    public ResourceNotFoundException(String message){
        super(message);
    }
}
