package com.asistencia.auth;

import com.asistencia.config.FirebaseAuthConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthSessionManagerTest {
    @Test
    void renuevaTokenCuandoEstaPorExpirar() {
        FirebaseAuthRestClient authClient = new FirebaseAuthRestClient(new FirebaseAuthConfig("api-key"), (uri, headers, body) ->
                new com.asistencia.api.HttpResponseData(200, """
                        {
                          "id_token": "id-token-renovado",
                          "refresh_token": "refresh-renovado",
                          "user_id": "firebase-uid",
                          "expires_in": "3600"
                        }
                        """), new ObjectMapper());
        AuthSessionManager manager = new AuthSessionManager(authClient);
        manager.start(new FirebaseAuthSession("id-token-viejo", "refresh-viejo", "firebase-uid", Instant.now().plusSeconds(10)));

        assertEquals("id-token-renovado", manager.requireValidSession().getIdToken());
    }

    @Test
    void logoutLimpiaSesion() {
        AuthSessionManager manager = new AuthSessionManager(new FirebaseAuthRestClient(new FirebaseAuthConfig("api-key")));
        manager.start(new FirebaseAuthSession("id-token", "refresh-token", "firebase-uid", Instant.now().plusSeconds(3600)));

        manager.clear();

        assertTrue(manager.current().isEmpty());
    }
}
