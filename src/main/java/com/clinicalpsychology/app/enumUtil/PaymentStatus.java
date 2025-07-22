package com.clinicalpsychology.app.enumUtil;

public enum PaymentStatus {

    COMPLETED,
    PENDING,
    EXPIRED,
    REFUNDED,
    HOLD,
    FAILURE;

    public boolean isCompleted(){
        return this==COMPLETED;
    }

    public boolean isHold(){
        return this==HOLD;
    }
}
