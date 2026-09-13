package com.asistencia.api;

import com.asistencia.exception.BackendConnectionException;
import com.asistencia.exception.BackendUnavailableException;
import com.asistencia.exception.InvalidBackendResponseException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpBackendApiClient implements BackendApiClient {
    private final HttpClient httpClient;

    public HttpBackendApiClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    public HttpBackendApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public BackendResponse execute(BackendRequest request) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(request.getUri())
                    .timeout(request.getTimeout());
            request.getHeaders().forEach(builder::header);
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                builder.GET();
            } else {
                builder.method(request.getMethod(), HttpRequest.BodyPublishers.ofString(request.getBody() == null ? "" : request.getBody()));
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 503 || response.statusCode() == 502 || response.statusCode() == 504) {
                throw new BackendUnavailableException("El servidor no esta disponible temporalmente");
            }
            if (response.body() == null) {
                throw new InvalidBackendResponseException("Respuesta invalida del servidor");
            }
            return new BackendResponse(response.statusCode(), response.body());
        } catch (ConnectException exception) {
            throw new BackendConnectionException("No hay conexion con el backend de asistencia", exception);
        } catch (IOException exception) {
            throw new BackendConnectionException("Error de conexion con el backend de asistencia", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BackendConnectionException("La comunicacion con el backend fue interrumpida", exception);
        }
    }
}
