package com.asistencia.service;

import com.asistencia.api.BackendRequest;
import com.asistencia.api.BackendResponse;
import com.asistencia.exception.BackendConnectionException;
import com.asistencia.exception.BackendUnavailableException;
import com.asistencia.exception.InvalidBackendResponseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BackendOperationServiceTest {
    @Test
    void errorDeConexionSeEntregaComoExcepcionControlada() {
        BackendOperationService service = new BackendOperationService(request -> {
            throw new IOException("sin internet");
        });

        assertThrows(BackendConnectionException.class, () -> service.executeRequired(request()));
    }

    @Test
    void servidorNoDisponibleSeEntregaComoExcepcionControlada() {
        BackendOperationService service = new BackendOperationService(request -> new BackendResponse(503, "unavailable"));

        assertThrows(BackendUnavailableException.class, () -> service.executeRequired(request()));
    }

    @Test
    void respuestaVaciaSeEntregaComoRespuestaInvalida() {
        BackendOperationService service = new BackendOperationService(request -> new BackendResponse(200, ""));

        assertThrows(InvalidBackendResponseException.class, () -> service.executeRequired(request()));
    }

    @Test
    void respuestaValidaSeRetornaSinAlterar() {
        BackendOperationService service = new BackendOperationService(request -> new BackendResponse(200, "{\"ok\":true}"));

        BackendResponse response = service.executeRequired(request());

        assertEquals(200, response.getStatusCode());
        assertEquals("{\"ok\":true}", response.getBody());
    }

    private BackendRequest request() {
        return new BackendRequest(
                "health",
                URI.create("https://backend.example.invalid/health"),
                "GET",
                null,
                Map.of(),
                Duration.ofSeconds(1)
        );
    }
}
