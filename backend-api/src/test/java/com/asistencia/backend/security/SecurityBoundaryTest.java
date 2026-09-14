package com.asistencia.backend.security;

import com.asistencia.backend.web.ForbiddenException;
import com.asistencia.backend.web.UnauthorizedException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityBoundaryTest {
    @Test
    void extraeTokenBearerValido() {
        assertEquals("token-valido", BearerTokenExtractor.extract("Bearer token-valido"));
    }

    @Test
    void rechazaTokenInvalido() {
        assertThrows(UnauthorizedException.class, () -> BearerTokenExtractor.extract("Basic token"));
        assertThrows(UnauthorizedException.class, () -> BearerTokenExtractor.extract("Bearer   "));
    }

    @Test
    void trabajadorNoPuedeEjecutarOperacionAdmin() {
        AdminAuthorizationService service = new AdminAuthorizationService();

        assertThrows(ForbiddenException.class, () -> service.requireAdmin(new AuthenticatedUser("worker-uid", "worker@example.com", Map.of(), false)));
    }

    @Test
    void adminPuedeEjecutarOperacionAdmin() {
        AdminAuthorizationService service = new AdminAuthorizationService();

        service.requireAdmin(new AuthenticatedUser("admin-uid", "admin@example.com", Map.of("admin", true), true));
    }
}
