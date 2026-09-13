package com.asistencia.exception;

public class UserNotFoundException extends AttendanceException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
