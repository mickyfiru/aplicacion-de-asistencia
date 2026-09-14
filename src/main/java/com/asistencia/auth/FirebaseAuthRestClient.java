package com.asistencia.auth;

import com.asistencia.api.HttpResponseData;
import com.asistencia.api.HttpTransport;
import com.asistencia.api.JavaNetHttpTransport;
import com.asistencia.config.FirebaseAuthConfig;
import com.asistencia.exception.BackendConnectionException;
import com.asistencia.exception.InvalidBackendResponseException;
import com.asistencia.exception.InvalidCredentialsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public class FirebaseAuthRestClient {
    private static final String SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=";
    private static final String REFRESH_URL = "https://securetoken.googleapis.com/v1/token?key=";

    private final FirebaseAuthConfig config;
    private final HttpTransport httpTransport;
    private final ObjectMapper objectMapper;

    public FirebaseAuthRestClient(FirebaseAuthConfig config) {
        this(config, new JavaNetHttpTransport(), new ObjectMapper());
    }

    public FirebaseAuthRestClient(FirebaseAuthConfig config, HttpTransport httpTransport, ObjectMapper objectMapper) {
        this.config = config;
        this.httpTransport = httpTransport;
        this.objectMapper = objectMapper;
    }

    public FirebaseAuthSession signInWithPassword(String email, String password) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "email", email.trim(),
                    "password", password,
                    "returnSecureToken", true
            ));
            HttpResponseData response = httpTransport.post(
                    URI.create(SIGN_IN_URL + url(config.getWebApiKey())),
                    Map.of("Content-Type", "application/json"),
                    body
            );
            if (response.statusCode() == 400 || response.statusCode() == 401) {
                throw new InvalidCredentialsException("Credenciales Firebase incorrectas");
            }
            return parseSession(response);
        } catch (InvalidCredentialsException | InvalidBackendResponseException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BackendConnectionException("No se pudo conectar con Firebase Authentication", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BackendConnectionException("La comunicacion con Firebase Authentication fue interrumpida", exception);
        }
    }

    public FirebaseAuthSession refresh(String refreshToken) {
        try {
            String body = "grant_type=refresh_token&refresh_token=" + url(refreshToken);
            HttpResponseData response = httpTransport.post(
                    URI.create(REFRESH_URL + url(config.getWebApiKey())),
                    Map.of("Content-Type", "application/x-www-form-urlencoded"),
                    body
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new InvalidCredentialsException("La sesion Firebase expiro; vuelve a iniciar sesion");
            }
            JsonNode root = objectMapper.readTree(response.body());
            return new FirebaseAuthSession(
                    requiredText(root, "id_token"),
                    requiredText(root, "refresh_token"),
                    requiredText(root, "user_id"),
                    Instant.now().plusSeconds(Long.parseLong(requiredText(root, "expires_in")))
            );
        } catch (InvalidCredentialsException | InvalidBackendResponseException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BackendConnectionException("No se pudo renovar la sesion Firebase", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BackendConnectionException("La renovacion Firebase fue interrumpida", exception);
        }
    }

    private FirebaseAuthSession parseSession(HttpResponseData response) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new InvalidBackendResponseException("Firebase Authentication respondio con estado HTTP " + response.statusCode());
        }
        JsonNode root = objectMapper.readTree(response.body());
        return new FirebaseAuthSession(
                requiredText(root, "idToken"),
                requiredText(root, "refreshToken"),
                requiredText(root, "localId"),
                Instant.now().plusSeconds(Long.parseLong(requiredText(root, "expiresIn")))
        );
    }

    private String requiredText(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            throw new InvalidBackendResponseException("Firebase Authentication no devolvio " + field);
        }
        return value.asText();
    }

    private String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
