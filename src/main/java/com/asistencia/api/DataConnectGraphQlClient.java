package com.asistencia.api;

import com.asistencia.config.FirebaseDataConnectConfig;
import com.asistencia.config.FirebaseAuthConfig;
import com.asistencia.exception.BackendConnectionException;
import com.asistencia.exception.BackendUnavailableException;
import com.asistencia.exception.InvalidBackendResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataConnectGraphQlClient {
    private static final String BASE_URL = "https://firebasedataconnect.googleapis.com/v1/";
    private static final String FIREBASE_AUTH_HEADER = "X-Firebase-Auth-Token";

    private final FirebaseDataConnectConfig config;
    private final FirebaseAccessTokenProvider tokenProvider;
    private final HttpTransport httpTransport;
    private final ObjectMapper objectMapper;
    private final String webApiKey;

    public DataConnectGraphQlClient(FirebaseDataConnectConfig config, FirebaseAccessTokenProvider tokenProvider) {
        this(config, tokenProvider, (String) null, new JavaNetHttpTransport(), new ObjectMapper());
    }

    public DataConnectGraphQlClient(
            FirebaseDataConnectConfig config,
            FirebaseAccessTokenProvider tokenProvider,
            FirebaseAuthConfig authConfig
    ) {
        this(config, tokenProvider, authConfig.getWebApiKey(), new JavaNetHttpTransport(), new ObjectMapper());
    }

    public DataConnectGraphQlClient(
            FirebaseDataConnectConfig config,
            FirebaseAccessTokenProvider tokenProvider,
            HttpTransport httpTransport,
            ObjectMapper objectMapper
    ) {
        this(config, tokenProvider, null, httpTransport, objectMapper);
    }

    public DataConnectGraphQlClient(
            FirebaseDataConnectConfig config,
            FirebaseAccessTokenProvider tokenProvider,
            String webApiKey,
            HttpTransport httpTransport,
            ObjectMapper objectMapper
    ) {
        this.config = config;
        this.tokenProvider = tokenProvider;
        this.httpTransport = httpTransport;
        this.objectMapper = objectMapper;
        this.webApiKey = webApiKey == null || webApiKey.isBlank() ? null : webApiKey.trim();
    }

    public JsonNode executeQuery(String operationName, Map<String, Object> variables) {
        return execute(DataConnectOperationType.QUERY, operationName, variables);
    }

    public JsonNode executeMutation(String operationName, Map<String, Object> variables) {
        return execute(DataConnectOperationType.MUTATION, operationName, variables);
    }

    private JsonNode execute(DataConnectOperationType type, String operationName, Map<String, Object> variables) {
        try {
            String connectorName = connectorResourceName();
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "name", connectorName,
                    "operationName", operationName,
                    "variables", variables == null ? Map.of() : variables
            ));
            String idToken = firebaseIdToken();
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put(FIREBASE_AUTH_HEADER, idToken);
            headers.put("X-Goog-Api-Client", "gl-java/ fire/java-swing");
            headers.put("X-Client-Version", "java-swing/1.0.0");
            URI uri = buildUri(type, connectorName);
            HttpResponseData response = httpTransport.post(
                    uri,
                    headers,
                    requestBody
            );
            if (response.statusCode() == 502 || response.statusCode() == 503 || response.statusCode() == 504) {
                logFailedRequest(uri, operationName, headers, idToken, response);
                throw new BackendUnavailableException("Firebase Data Connect no esta disponible temporalmente");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logFailedRequest(uri, operationName, headers, idToken, response);
                throw new InvalidBackendResponseException("Firebase Data Connect respondio con estado HTTP " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode errors = root.get("errors");
            if (errors != null && errors.isArray() && !errors.isEmpty()) {
                throw new InvalidBackendResponseException("Firebase Data Connect devolvio errores GraphQL: " + errors);
            }
            JsonNode data = root.get("data");
            if (data == null || data.isNull()) {
                throw new InvalidBackendResponseException("Firebase Data Connect devolvio una respuesta sin data");
            }
            return data;
        } catch (IOException exception) {
            throw new BackendConnectionException("No se pudo conectar con Firebase Data Connect", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BackendConnectionException("La comunicacion con Firebase Data Connect fue interrumpida", exception);
        }
    }

    private URI buildUri(DataConnectOperationType type, String connectorName) {
        String suffix = type == DataConnectOperationType.QUERY ? ":executeQuery" : ":executeMutation";
        String endpoint = BASE_URL + connectorName + suffix;
        if (webApiKey != null) {
            endpoint += "?key=" + url(webApiKey);
        }
        return URI.create(endpoint);
    }

    private String connectorResourceName() {
        String[] connectorParts = config.getConnector().split("/");
        if (connectorParts.length != 3) {
            throw new IllegalArgumentException("El conector debe tener formato location/service/connector");
        }
        return "projects/" + url(config.getProjectId())
                + "/locations/" + url(connectorParts[0])
                + "/services/" + url(connectorParts[1])
                + "/connectors/" + url(connectorParts[2]);
    }

    private String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String firebaseIdToken() {
        String idToken = tokenProvider.getAccessToken();
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalStateException("No hay idToken Firebase valido para llamar Data Connect");
        }
        String trimmedToken = idToken.trim();
        if (trimmedToken.contains("\n") || trimmedToken.contains("\r")
                || trimmedToken.startsWith("\"") || trimmedToken.endsWith("\"")) {
            throw new IllegalStateException("El idToken Firebase tiene un formato invalido");
        }
        return trimmedToken;
    }

    private void logFailedRequest(
            URI uri,
            String operationName,
            Map<String, String> headers,
            String idToken,
            HttpResponseData response
    ) {
        System.err.println("Firebase Data Connect HTTP error"
                + " status=" + response.statusCode()
                + " endpoint=" + sanitizedEndpoint(uri)
                + " operation=" + operationName
                + " authHeaderPresent=" + headers.containsKey(FIREBASE_AUTH_HEADER)
                + " idTokenLength=" + idToken.length()
                + " response=" + safeResponseBody(response.body()));
    }

    private String sanitizedEndpoint(URI uri) {
        return uri.toString().replaceAll("([?&]key=)[^&]+", "$1<redacted>");
    }

    private String safeResponseBody(String body) {
        if (body == null || body.isBlank()) {
            return "<empty>";
        }
        String singleLine = body.replaceAll("\\s+", " ").trim();
        return singleLine.length() <= 1200 ? singleLine : singleLine.substring(0, 1200) + "...";
    }
}
