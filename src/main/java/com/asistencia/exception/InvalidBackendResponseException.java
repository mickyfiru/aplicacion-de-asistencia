package com.asistencia.exception;

public class InvalidBackendResponseException extends AttendanceException {
    public InvalidBackendResponseException(String message) {
        super(message);
    }
}
