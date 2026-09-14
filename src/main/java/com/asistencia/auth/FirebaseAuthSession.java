package com.asistencia.auth;

import java.time.Instant;

public class FirebaseAuthSession {
    private final String idToken;
    private final String refreshToken;
    private final String localId;
    private final Instant expiresAt;

    public FirebaseAuthSession(String idToken, String refreshToken, String localId, Instant expiresAt) {
        this.idToken = require(idToken, "idToken");
        this.refreshToken = require(refreshToken, "refreshToken");
        this.localId = require(localId, "localId");
        this.expiresAt = expiresAt;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getLocalId() {
        return localId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean expiresWithinSeconds(long seconds) {
        return !expiresAt.isAfter(Instant.now().plusSeconds(seconds));
    }

    private String require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Sesion Firebase sin " + field);
        }
        return value;
    }
}
