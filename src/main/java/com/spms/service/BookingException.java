package com.spms.service;

/** Thrown for any rule violation while booking or cancelling; the message is shown to the user. */
public class BookingException extends RuntimeException {
    public BookingException(String message) {
        super(message);
    }
}
