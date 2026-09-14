package com.asistencia.backend.security;

import java.util.Map;

public record AuthenticatedUser(String uid, String email, Map<String, Object> claims, boolean admin) {
    public AuthenticatedUser {
        claims = claims == null ? Map.of() : Map.copyOf(claims);
    }
}
