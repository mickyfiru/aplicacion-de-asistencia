package com.asistencia.backend.dataconnect;

import com.asistencia.backend.model.BackendAttendance;
import com.asistencia.backend.model.BackendSchedule;
import com.asistencia.backend.model.BackendUser;
import com.asistencia.backend.web.BackendDependencyException;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DataConnectAttendanceDataGateway implements AttendanceDataGateway {
    private final DataConnectRestClient client;

    public DataConnectAttendanceDataGateway(DataConnectRestClient client) {
        this.client = client;
    }

    @Override
    public Optional<BackendUser> findUserByAuthUid(String authUid) {
        JsonNode data = client.executeQuery("BuscarUsuarioPorAuthUid", Map.of("authUid", authUid));
        JsonNode users = data.get("users");
        if (users == null || !users.isArray() || users.isEmpty()) {
            return Optional.empty();
        }
        JsonNode user = users.get(0);
        return Optional.of(new BackendUser(
                text(user, "id"),
                text(user, "authUid"),
                text(user, "nombre"),
                text(user, "apellido"),
                text(user, "rol"),
                bool(user, "activo")
        ));
    }

    @Override
    public Optional<BackendSchedule> findActiveSchedule(String userId, int chileDayOfWeek) {
        JsonNode data = client.executeQuery("ObtenerHorarioBackend", Map.of(
                "usuarioId", userId,
                "diaSemana", chileDayOfWeek
        ));
        JsonNode horarios = data.get("horarios");
        if (horarios == null || !horarios.isArray() || horarios.isEmpty()) {
            return Optional.empty();
        }
        JsonNode schedule = horarios.get(0);
        return Optional.of(new BackendSchedule(
                text(schedule, "id"),
                text(schedule.get("usuario"), "id"),
                intList(schedule.get("diasSemana")),
                LocalTime.parse(text(schedule, "horaEntrada")),
                LocalTime.parse(text(schedule, "horaSalida"))
        ));
    }

    @Override
    public Optional<BackendAttendance> findAttendance(String userId, LocalDate businessDate) {
        JsonNode data = client.executeQuery("ObtenerAsistenciaBackend", keyVariables(userId, businessDate));
        JsonNode asistencia = data.get("asistencia");
        return asistencia == null || asistencia.isNull() ? Optional.empty() : Optional.of(attendance(asistencia));
    }

    @Override
    public BackendAttendance createServerEntry(String userId, LocalDate businessDate) {
        client.executeMutation("CrearEntradaAsistenciaBackend", keyVariables(userId, businessDate));
        return findAttendance(userId, businessDate)
                .orElseThrow(() -> new BackendDependencyException("No se pudo recuperar la entrada creada"));
    }

    @Override
    public BackendAttendance updateEntryResult(String userId, LocalDate businessDate, String state, int lateMinutes) {
        client.executeMutation("ActualizarResultadoEntrada", Map.of(
                "usuarioId", userId,
                "fecha", businessDate.toString(),
                "estadoEntrada", state,
                "minutosAtraso", lateMinutes
        ));
        return findAttendance(userId, businessDate)
                .orElseThrow(() -> new BackendDependencyException("No se pudo recuperar la entrada actualizada"));
    }

    @Override
    public BackendAttendance setServerExit(String userId, LocalDate businessDate) {
        client.executeMutation("MarcarSalidaAsistenciaBackend", keyVariables(userId, businessDate));
        return findAttendance(userId, businessDate)
                .orElseThrow(() -> new BackendDependencyException("No se pudo recuperar la salida registrada"));
    }

    @Override
    public BackendAttendance updateExitResult(String userId, LocalDate businessDate, String state, int earlyMinutes) {
        client.executeMutation("ActualizarResultadoSalida", Map.of(
                "usuarioId", userId,
                "fecha", businessDate.toString(),
                "estadoSalida", state,
                "minutosSalidaAnticipada", earlyMinutes
        ));
        return findAttendance(userId, businessDate)
                .orElseThrow(() -> new BackendDependencyException("No se pudo recuperar la salida actualizada"));
    }

    private Map<String, Object> keyVariables(String userId, LocalDate businessDate) {
        return Map.of("usuarioId", userId, "fecha", businessDate.toString());
    }

    private BackendAttendance attendance(JsonNode node) {
        return new BackendAttendance(
                text(node.get("usuario"), "id"),
                LocalDate.parse(text(node, "fecha")),
                OffsetDateTime.parse(text(node, "horaEntrada")),
                text(node, "estadoEntrada"),
                integer(node, "minutosAtraso"),
                optionalOffset(node, "horaSalida"),
                optionalText(node, "estadoSalida"),
                optionalInteger(node, "minutosSalidaAnticipada")
        );
    }

    private String text(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            throw new BackendDependencyException("Respuesta Data Connect sin campo: " + field);
        }
        return node.get(field).asText();
    }

    private String optionalText(JsonNode node, String field) {
        return node == null || node.get(field) == null || node.get(field).isNull() ? null : node.get(field).asText();
    }

    private boolean bool(JsonNode node, String field) {
        return node.get(field).asBoolean();
    }

    private int integer(JsonNode node, String field) {
        return node.get(field).asInt();
    }

    private Integer optionalInteger(JsonNode node, String field) {
        return node == null || node.get(field) == null || node.get(field).isNull() ? null : node.get(field).asInt();
    }

    private OffsetDateTime optionalOffset(JsonNode node, String field) {
        String value = optionalText(node, field);
        return value == null ? null : OffsetDateTime.parse(value);
    }

    private List<Integer> intList(JsonNode node) {
        List<Integer> values = new ArrayList<>();
        node.forEach(value -> values.add(value.asInt()));
        return values;
    }
}
