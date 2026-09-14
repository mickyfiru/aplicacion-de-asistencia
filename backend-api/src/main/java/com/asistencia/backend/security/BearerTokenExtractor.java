package com.asistencia.backend.security;

import com.asistencia.backend.web.UnauthorizedException;

public final class BearerTokenExtractor {
    private static final String PREFIX = "Bearer ";

    private BearerTokenExtractor() {
    }

    public static String extract(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(PREFIX)) {
            throw new UnauthorizedException("Debe enviar Authorization: Bearer <Firebase ID Token>");
        }
        String token = authorizationHeader.substring(PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new UnauthorizedException("Token Firebase vacio");
        }
        return token;
    }
}
