package com.asistencia.exception;

public class DuplicateAttendanceException extends IllegalStateException {
    public DuplicateAttendanceException(String message) {
        super(message);
    }
}
