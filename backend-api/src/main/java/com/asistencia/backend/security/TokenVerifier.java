package com.asistencia.backend.security;

public interface TokenVerifier {
    AuthenticatedUser verify(String idToken);
}
