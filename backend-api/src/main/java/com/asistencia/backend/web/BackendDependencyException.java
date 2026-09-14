package com.asistencia.backend.web;

public class BackendDependencyException extends RuntimeException {
    public BackendDependencyException(String message) {
        super(message);
    }

    public BackendDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
