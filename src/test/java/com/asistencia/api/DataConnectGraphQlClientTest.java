package com.asistencia.api;

import com.asistencia.config.FirebaseDataConnectConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataConnectGraphQlClientTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void enviaIdTokenFirebaseEnEncabezadoEsperadoPorDataConnect() throws Exception {
        FakeTransport transport = new FakeTransport(new HttpResponseData(200, """
                {"data":{"users":[]}}
                """));
        DataConnectGraphQlClient client = new DataConnectGraphQlClient(
                new FirebaseDataConnectConfig("app-asistencia-5e6fe", "southamerica-west1/app-asistencia-5e6fe-service/default"),
                () -> " id-token-firebase ",
                "web-api-key",
                transport,
                mapper
        );

        client.executeQuery("ObtenerMiUsuario", Map.of());

        Request request = transport.requests.get(0);
        assertEquals("id-token-firebase", request.headers().get("X-Firebase-Auth-Token"));
        assertFalse(request.headers().containsKey("Authorization"));
        assertEquals("application/json", request.headers().get("Content-Type"));
        assertEquals(
                "https://firebasedataconnect.googleapis.com/v1/projects/app-asistencia-5e6fe/locations/southamerica-west1/services/app-asistencia-5e6fe-service/connectors/default:executeQuery?key=web-api-key",
                request.uri().toString()
        );

        JsonNode body = mapper.readTree(request.body());
        assertEquals("ObtenerMiUsuario", body.get("operationName").asText());
        assertEquals(
                "projects/app-asistencia-5e6fe/locations/southamerica-west1/services/app-asistencia-5e6fe-service/connectors/default",
                body.get("name").asText()
        );
    }

    @Test
    void rechazaIdTokenConSaltosDeLineaOComillas() {
        DataConnectGraphQlClient client = new DataConnectGraphQlClient(
                new FirebaseDataConnectConfig("project", "southamerica-west1/service/default"),
                () -> "\"id-token\"",
                "web-api-key",
                new FakeTransport(new HttpResponseData(200, "{\"data\":{}}")),
                mapper
        );

        assertThrows(IllegalStateException.class, () -> client.executeQuery("ObtenerMiUsuario", Map.of()));
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
