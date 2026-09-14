package com.asistencia.repository.dataconnect;

import com.asistencia.api.DataConnectGraphQlClient;
import com.asistencia.exception.InvalidBackendResponseException;
import com.asistencia.model.dataconnect.DataConnectAttendance;
import com.asistencia.model.dataconnect.DataConnectSchedule;
import com.asistencia.model.dataconnect.DataConnectUser;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CurrentUserDataConnectRepository implements CurrentUserDataConnectGateway {
    private final DataConnectGraphQlClient client;

    public CurrentUserDataConnectRepository(DataConnectGraphQlClient client) {
        this.client = client;
    }

    @Override
    public Optional<DataConnectUser> findCurrentUser() {
        JsonNode data = client.executeQuery("ObtenerMiUsuario", Map.of());
        return firstUser(data.get("users"));
    }

    @Override
    public Optional<DataConnectSchedule> findCurrentSchedule(int diaSemana) {
        JsonNode data = client.executeQuery("ObtenerMiHorario", Map.of("diaSemana", diaSemana));
        return firstSchedule(data.get("horarios"));
    }

    @Override
    public Optional<DataConnectAttendance> findOpenAttendance() {
        JsonNode data = client.executeQuery("ObtenerMiAsistenciaActual", Map.of());
        return firstAttendance(data.get("asistencias"));
    }

    @Override
    public Optional<DataConnectAttendance> findLatestAttendance() {
        JsonNode data = client.executeQuery("ObtenerMiUltimaAsistencia", Map.of());
        return firstAttendance(data.get("asistencias"));
    }

    @Override
    public List<DataConnectAttendance> findMyAttendances(OffsetDateTime desde, OffsetDateTime hasta) {
        JsonNode data = client.executeQuery("ObtenerMisAsistencias", Map.of(
                "desde", desde.toString(),
                "hasta", hasta.toString()
        ));
        return DataConnectJsonMapper.attendances(data.get("asistencias"));
    }

    @Override
    public DataConnectAttendance markMyEntry() {
        client.executeMutation("MarcarMiEntrada", Map.of());
        return findOpenAttendance()
                .orElseThrow(() -> new InvalidBackendResponseException("Data Connect no devolvio la entrada creada"));
    }

    @Override
    public DataConnectAttendance markMyExit() {
        client.executeMutation("MarcarMiSalida", Map.of());
        return findLatestAttendance()
                .orElseThrow(() -> new InvalidBackendResponseException("Data Connect no devolvio la salida registrada"));
    }

    private Optional<DataConnectUser> firstUser(JsonNode users) {
        if (users == null || !users.isArray() || users.isEmpty()) {
            return Optional.empty();
        }
        return DataConnectJsonMapper.optionalUser(users.get(0));
    }

    private Optional<DataConnectSchedule> firstSchedule(JsonNode schedules) {
        if (schedules == null || !schedules.isArray() || schedules.isEmpty()) {
            return Optional.empty();
        }
        return DataConnectJsonMapper.optionalSchedule(schedules.get(0));
    }

    private Optional<DataConnectAttendance> firstAttendance(JsonNode attendances) {
        if (attendances == null || !attendances.isArray() || attendances.isEmpty()) {
            return Optional.empty();
        }
        return DataConnectJsonMapper.optionalAttendance(attendances.get(0));
    }
}
