package com.asistencia.exception;

public class AuthenticatedUserNotRegisteredException extends AttendanceException {
    public AuthenticatedUserNotRegisteredException(String message) {
        super(message);
    }
}
