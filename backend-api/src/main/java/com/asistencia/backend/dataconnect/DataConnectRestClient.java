package com.asistencia.backend.dataconnect;

import com.asistencia.backend.config.DataConnectSettings;
import com.asistencia.backend.web.BackendDependencyException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class DataConnectRestClient {
    private static final String BASE_URL = "https://firebasedataconnect.googleapis.com/v1/";

    private final DataConnectSettings settings;
    private final DataConnectAccessTokenProvider tokenProvider;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataConnectRestClient(DataConnectSettings settings, DataConnectAccessTokenProvider tokenProvider, HttpClient httpClient) {
        this.settings = settings;
        this.tokenProvider = tokenProvider;
        this.httpClient = httpClient;
    }

    public JsonNode executeQuery(String operationName, Map<String, Object> variables) {
        return execute(operationName, variables, ":executeQuery");
    }

    public JsonNode executeMutation(String operationName, Map<String, Object> variables) {
        return execute(operationName, variables, ":executeMutation");
    }

    private JsonNode execute(String operationName, Map<String, Object> variables, String suffix) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "operationName", operationName,
                    "variables", variables == null ? Map.of() : variables
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + settings.connectorResourceName() + suffix))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + tokenProvider.getAccessToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BackendDependencyException("Data Connect respondio con HTTP " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode errors = root.get("errors");
            if (errors != null && errors.isArray() && !errors.isEmpty()) {
                throw new BackendDependencyException("Data Connect devolvio errores: " + errors);
            }
            return root.get("data");
        } catch (IOException exception) {
            throw new BackendDependencyException("No se pudo llamar a Data Connect", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BackendDependencyException("La llamada a Data Connect fue interrumpida", exception);
        }
    }
}
