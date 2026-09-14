package com.asistencia.repository.dataconnect;

import com.asistencia.api.DataConnectGraphQlClient;
import com.asistencia.api.FirebaseAccessTokenProvider;
import com.asistencia.config.FirebaseDataConnectConfig;
import com.asistencia.model.dataconnect.DataConnectAttendance;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrentUserDataConnectRepositoryTest {
    @Test
    void obtenerMiUsuarioUsaOperacionDelPropietario() {
        FakeClient client = new FakeClient();
        CurrentUserDataConnectRepository repository = new CurrentUserDataConnectRepository(client);

        repository.findCurrentUser();

        assertEquals("ObtenerMiUsuario", client.calls.get(0).operationName());
        assertTrue(client.calls.get(0).variables().isEmpty());
    }

    @Test
    void obtenerMiHorarioNoEnviaUsuarioId() {
        FakeClient client = new FakeClient();
        CurrentUserDataConnectRepository repository = new CurrentUserDataConnectRepository(client);

        repository.findCurrentSchedule(1);

        assertEquals("ObtenerMiHorario", client.calls.get(0).operationName());
        assertEquals(Map.of("diaSemana", 1), client.calls.get(0).variables());
    }

    @Test
    void entradaNoEnviaUsuarioIdNiHoraEntrada() {
        FakeClient client = new FakeClient();
        CurrentUserDataConnectRepository repository = new CurrentUserDataConnectRepository(client);

        DataConnectAttendance attendance = repository.markMyEntry();

        assertEquals("MarcarMiEntrada", client.calls.get(0).operationName());
        assertTrue(client.calls.get(0).variables().isEmpty());
        assertEquals(OffsetDateTime.parse("2026-09-13T12:00:00Z"), attendance.getHoraEntrada());
    }

    @Test
    void salidaNoEnviaHoraSalida() {
        FakeClient client = new FakeClient();
        CurrentUserDataConnectRepository repository = new CurrentUserDataConnectRepository(client);

        DataConnectAttendance attendance = repository.markMyExit();

        assertEquals("MarcarMiSalida", client.calls.get(0).operationName());
        assertTrue(client.calls.get(0).variables().isEmpty());
        assertEquals(OffsetDateTime.parse("2026-09-13T21:30:00Z"), attendance.getHoraSalida());
    }

    private static class FakeClient extends DataConnectGraphQlClient {
        private final ObjectMapper mapper = new ObjectMapper();
        private final List<Call> calls = new ArrayList<>();

        FakeClient() {
            super(new FirebaseDataConnectConfig("project", "southamerica-west1/service/default"), (FirebaseAccessTokenProvider) () -> "id-token");
        }

        @Override
        public JsonNode executeQuery(String operationName, Map<String, Object> variables) {
            calls.add(new Call(operationName, variables));
            try {
                return switch (operationName) {
                    case "ObtenerMiUsuario" -> mapper.readTree("""
                            {"users":[{"id":"user-1","nombre":"Ana","apellido":"Perez","rut":"11.111.111-1","correo":"ana@example.com","authUid":"uid","rol":"TRABAJADOR","activo":true}]}
                            """);
                    case "ObtenerMiHorario" -> mapper.readTree("""
                            {"horarios":[{"id":"schedule-1","usuario":{"id":"user-1"},"diasSemana":[1,2,3,4,5],"horaEntrada":"08:00","horaSalida":"17:30","activo":true}]}
                            """);
                    case "ObtenerMiAsistenciaActual" -> mapper.readTree("""
                            {"asistencias":[{"id":"attendance-1","usuario":{"id":"user-1"},"fecha":"2026-09-13","horaEntrada":"2026-09-13T12:00:00Z","estadoEntrada":"PENDIENTE","minutosAtraso":0,"horaSalida":null,"estadoSalida":null,"minutosSalidaAnticipada":null}]}
                            """);
                    case "ObtenerMiUltimaAsistencia" -> mapper.readTree("""
                            {"asistencias":[{"id":"attendance-1","usuario":{"id":"user-1"},"fecha":"2026-09-13","horaEntrada":"2026-09-13T12:00:00Z","estadoEntrada":"PENDIENTE","minutosAtraso":0,"horaSalida":"2026-09-13T21:30:00Z","estadoSalida":null,"minutosSalidaAnticipada":null}]}
                            """);
                    default -> mapper.readTree("{}");
                };
            } catch (Exception exception) {
                throw new AssertionError(exception);
            }
        }

        @Override
        public JsonNode executeMutation(String operationName, Map<String, Object> variables) {
            calls.add(new Call(operationName, variables));
            return mapper.createObjectNode();
        }
    }

    private record Call(String operationName, Map<String, Object> variables) {
    }
}
