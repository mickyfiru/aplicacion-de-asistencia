package com.asistencia.repository.dataconnect;

import com.asistencia.exception.InvalidBackendResponseException;
import com.asistencia.model.dataconnect.DataConnectAttendance;
import com.asistencia.model.dataconnect.DataConnectSchedule;
import com.asistencia.model.dataconnect.DataConnectUser;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class DataConnectJsonMapper {
    private DataConnectJsonMapper() {
    }

    static Optional<DataConnectUser> optionalUser(JsonNode node) {
        if (isEmpty(node)) {
            return Optional.empty();
        }
        return Optional.of(user(node));
    }

    static DataConnectUser user(JsonNode node) {
        return new DataConnectUser(
                requiredText(node, "id"),
                requiredText(node, "nombre"),
                requiredText(node, "apellido"),
                requiredText(node, "rut"),
                requiredText(node, "correo"),
                optionalText(node, "authUid"),
                optionalText(node, "passwordHash"),
                requiredText(node, "rol"),
                requiredBoolean(node, "activo")
        );
    }

    static Optional<DataConnectSchedule> optionalSchedule(JsonNode node) {
        if (isEmpty(node)) {
            return Optional.empty();
        }
        return Optional.of(schedule(node));
    }

    static DataConnectSchedule schedule(JsonNode node) {
        return new DataConnectSchedule(
                requiredText(node, "id"),
                requiredText(node.get("usuario"), "id"),
                intList(node.get("diasSemana")),
                LocalTime.parse(requiredText(node, "horaEntrada")),
                LocalTime.parse(requiredText(node, "horaSalida")),
                requiredBoolean(node, "activo")
        );
    }

    static Optional<DataConnectAttendance> optionalAttendance(JsonNode node) {
        if (isEmpty(node)) {
            return Optional.empty();
        }
        return Optional.of(attendance(node));
    }

    static DataConnectAttendance attendance(JsonNode node) {
        return new DataConnectAttendance(
                optionalText(node, "id"),
                requiredText(node.get("usuario"), "id"),
                LocalDate.parse(requiredText(node, "fecha")),
                OffsetDateTime.parse(requiredText(node, "horaEntrada")),
                requiredText(node, "estadoEntrada"),
                requiredInt(node, "minutosAtraso"),
                optionalOffsetDateTime(node, "horaSalida"),
                optionalText(node, "estadoSalida"),
                optionalInteger(node, "minutosSalidaAnticipada")
        );
    }

    static List<DataConnectUser> users(JsonNode nodes) {
        List<DataConnectUser> users = new ArrayList<>();
        if (nodes != null && nodes.isArray()) {
            nodes.forEach(node -> users.add(user(node)));
        }
        return users;
    }

    static List<DataConnectAttendance> attendances(JsonNode nodes) {
        List<DataConnectAttendance> attendances = new ArrayList<>();
        if (nodes != null && nodes.isArray()) {
            nodes.forEach(node -> attendances.add(attendance(node)));
        }
        return attendances;
    }

    private static boolean isEmpty(JsonNode node) {
        return node == null || node.isNull() || node.isMissingNode();
    }

    private static String requiredText(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            throw new InvalidBackendResponseException("Falta el campo requerido en Data Connect: " + field);
        }
        return node.get(field).asText();
    }

    private static String optionalText(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }

    private static boolean requiredBoolean(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            throw new InvalidBackendResponseException("Falta el campo requerido en Data Connect: " + field);
        }
        return node.get(field).asBoolean();
    }

    private static int requiredInt(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            throw new InvalidBackendResponseException("Falta el campo requerido en Data Connect: " + field);
        }
        return node.get(field).asInt();
    }

    private static Integer optionalInteger(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asInt();
    }

    private static OffsetDateTime optionalOffsetDateTime(JsonNode node, String field) {
        String value = optionalText(node, field);
        return value == null ? null : OffsetDateTime.parse(value);
    }

    private static List<Integer> intList(JsonNode node) {
        if (node == null || !node.isArray()) {
            throw new InvalidBackendResponseException("diasSemana debe venir como lista desde Data Connect");
        }
        List<Integer> values = new ArrayList<>();
        node.forEach(value -> values.add(value.asInt()));
        return values;
    }
}
