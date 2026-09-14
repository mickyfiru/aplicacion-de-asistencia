package com.asistencia.backend.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ApiError> unauthorized(RuntimeException exception) {
        return error(HttpStatus.UNAUTHORIZED, exception);
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ApiError> forbidden(RuntimeException exception) {
        return error(HttpStatus.FORBIDDEN, exception);
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(RuntimeException exception) {
        return error(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> conflict(ConflictException exception) {
        return error(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler({BackendDependencyException.class, MissingRequestHeaderException.class})
    ResponseEntity<ApiError> dependency(Exception exception) {
        HttpStatus status = exception instanceof MissingRequestHeaderException ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_GATEWAY;
        return error(status, exception);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, Exception exception) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), errorCode(status, exception), exception.getMessage()));
    }

    private String errorCode(HttpStatus status, Exception exception) {
        if (exception instanceof ConflictException conflictException) {
            return conflictException.getErrorCode();
        }
        return switch (status) {
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "FORBIDDEN";
            case NOT_FOUND -> "NOT_FOUND";
            case BAD_GATEWAY -> "BACKEND_DEPENDENCY_ERROR";
            default -> status.name();
        };
    }
}
