package com.asistencia.service;

import com.asistencia.api.BackendApiClient;
import com.asistencia.api.BackendRequest;
import com.asistencia.api.BackendResponse;
import com.asistencia.exception.BackendConnectionException;
import com.asistencia.exception.BackendUnavailableException;
import com.asistencia.exception.InvalidBackendResponseException;

import java.io.IOException;

public class BackendOperationService {
    private final BackendApiClient backendApiClient;

    public BackendOperationService(BackendApiClient backendApiClient) {
        this.backendApiClient = backendApiClient;
    }

    public BackendResponse executeRequired(BackendRequest request) {
        try {
            BackendResponse response = backendApiClient.execute(request);
            if (response.getStatusCode() == 404) {
                throw new InvalidBackendResponseException("El recurso solicitado no existe en el backend");
            }
            if (response.getStatusCode() == 409) {
                throw new InvalidBackendResponseException("El backend informo un registro duplicado");
            }
            if (response.getStatusCode() >= 500) {
                throw new BackendUnavailableException("El servidor no esta disponible temporalmente");
            }
            if (!response.isSuccessful()) {
                throw new InvalidBackendResponseException("El backend respondio con un estado no esperado: " + response.getStatusCode());
            }
            if (response.getBody() == null || response.getBody().isBlank()) {
                throw new InvalidBackendResponseException("El backend devolvio una respuesta vacia");
            }
            return response;
        } catch (BackendConnectionException | BackendUnavailableException | InvalidBackendResponseException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BackendConnectionException("No se pudo conectar con el backend de asistencia", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BackendConnectionException("La comunicacion con el backend fue interrumpida", exception);
        }
    }
}
