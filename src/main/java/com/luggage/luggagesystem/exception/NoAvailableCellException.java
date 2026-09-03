package com.luggage.luggagesystem.exception;

public class NoAvailableCellException
        extends RuntimeException {

    public NoAvailableCellException(String message) {
        super(message);
    }
}