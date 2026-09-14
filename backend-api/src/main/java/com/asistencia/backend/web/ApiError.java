package com.asistencia.backend.web;

public record ApiError(int status, String error, String message) {
}
