package com.asistencia.auth;

import com.asistencia.api.HttpResponseData;
import com.asistencia.api.HttpTransport;
import com.asistencia.config.FirebaseAuthConfig;
import com.asistencia.exception.InvalidCredentialsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebaseAuthRestClientTest {
    @Test
    void loginCorrectoGuardaTokensDeFirebase() {
        FakeTransport transport = new FakeTransport(new HttpResponseData(200, """
                {
                  "idToken": "id-token",
                  "refreshToken": "refresh-token",
                  "localId": "firebase-uid",
                  "expiresIn": "3600"
                }
                """));
        FirebaseAuthRestClient client = client(transport);

        FirebaseAuthSession session = client.signInWithPassword("ana@example.com", "secreto");

        assertEquals("id-token", session.getIdToken());
        assertEquals("refresh-token", session.getRefreshToken());
        assertEquals("firebase-uid", session.getLocalId());
        assertTrue(transport.requests.get(0).body().contains("\"returnSecureToken\":true"));
        assertTrue(transport.requests.get(0).body().contains("\"email\":\"ana@example.com\""));
    }

    @Test
    void loginIncorrectoEsRechazado() {
        FirebaseAuthRestClient client = client(new FakeTransport(new HttpResponseData(400, "{}")));

        assertThrows(InvalidCredentialsException.class, () -> client.signInWithPassword("ana@example.com", "malo"));
    }

    @Test
    void renovacionUsaSecureTokenApi() {
        FakeTransport transport = new FakeTransport(new HttpResponseData(200, """
                {
                  "id_token": "id-token-nuevo",
                  "refresh_token": "refresh-token-nuevo",
                  "user_id": "firebase-uid",
                  "expires_in": "3600"
                }
                """));
        FirebaseAuthRestClient client = client(transport);

        FirebaseAuthSession session = client.refresh("refresh-token");

        assertEquals("id-token-nuevo", session.getIdToken());
        assertTrue(transport.requests.get(0).uri().toString().startsWith("https://securetoken.googleapis.com/v1/token"));
        assertEquals("application/x-www-form-urlencoded", transport.requests.get(0).headers().get("Content-Type"));
        assertTrue(transport.requests.get(0).body().contains("grant_type=refresh_token"));
    }

    private FirebaseAuthRestClient client(HttpTransport transport) {
        return new FirebaseAuthRestClient(new FirebaseAuthConfig("api-key"), transport, new ObjectMapper());
    }

    private static class FakeTransport implements HttpTransport {
        private final HttpResponseData response;
        private final List<Request> requests = new ArrayList<>();

        FakeTransport(HttpResponseData response) {
            this.response = response;
        }

        @Override
        public HttpResponseData post(URI uri, Map<String, String> headers, String body) throws IOException, InterruptedException {
            requests.add(new Request(uri, headers, body));
            return response;
        }
    }

    private record Request(URI uri, Map<String, String> headers, String body) {
    }
}
