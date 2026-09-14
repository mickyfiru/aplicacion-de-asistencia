package com.asistencia.repository.dataconnect;

import com.asistencia.api.DataConnectGraphQlClient;
import com.asistencia.exception.InvalidBackendResponseException;
import com.asistencia.model.dataconnect.DataConnectAttendance;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DataConnectAttendanceRepository implements DataConnectAttendanceGateway {
    private final DataConnectGraphQlClient client;

    public DataConnectAttendanceRepository(DataConnectGraphQlClient client) {
        this.client = client;
    }

    @Override
    public DataConnectAttendance createEntry(String usuarioId, LocalDate fecha, String estadoEntrada, int minutosAtraso) {
        client.executeMutation("CrearEntradaAsistenciaBackend", Map.of(
                "usuarioId", usuarioId,
                "fecha", fecha.toString()
        ));
        client.executeMutation("ActualizarResultadoEntrada", Map.of(
                "usuarioId", usuarioId,
                "fecha", fecha.toString(),
                "estadoEntrada", estadoEntrada,
                "minutosAtraso", minutosAtraso
        ));
        return findByUserAndDate(usuarioId, fecha)
                .orElseThrow(() -> new InvalidBackendResponseException("Data Connect no devolvio la asistencia creada"));
    }

    @Override
    public Optional<DataConnectAttendance> findByUserAndDate(String usuarioId, LocalDate fecha) {
        JsonNode data = client.executeQuery("ObtenerAsistenciaPorUsuarioYFecha", Map.of(
                "usuarioId", usuarioId,
                "fecha", fecha.toString()
        ));
        return DataConnectJsonMapper.optionalAttendance(data.get("asistencia"));
    }

    @Override
    public DataConnectAttendance markExit(String usuarioId, LocalDate fecha, String estadoSalida, int minutosSalidaAnticipada) {
        client.executeMutation("MarcarSalidaAsistenciaBackend", Map.of(
                "usuarioId", usuarioId,
                "fecha", fecha.toString()
        ));
        client.executeMutation("ActualizarResultadoSalida", Map.of(
                "usuarioId", usuarioId,
                "fecha", fecha.toString(),
                "estadoSalida", estadoSalida,
                "minutosSalidaAnticipada", minutosSalidaAnticipada
        ));
        return findByUserAndDate(usuarioId, fecha)
                .orElseThrow(() -> new InvalidBackendResponseException("Data Connect no devolvio la asistencia actualizada"));
    }

    @Override
    public List<DataConnectAttendance> findByUserAndDateRange(String usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        JsonNode data = client.executeQuery("ListarAsistenciasUsuarioPorRango", Map.of(
                "usuarioId", usuarioId,
                "fechaInicio", fechaInicio.toString(),
                "fechaFin", fechaFin.toString()
        ));
        return DataConnectJsonMapper.attendances(data.get("asistencias"));
    }

    @Override
    public List<DataConnectAttendance> findLateArrivals(LocalDate fechaInicio, LocalDate fechaFin, String usuarioId) {
        JsonNode data = client.executeQuery("ListarAtrasos", nullableRangeVariables(fechaInicio, fechaFin, usuarioId));
        return DataConnectJsonMapper.attendances(data.get("asistencias"));
    }

    @Override
    public List<DataConnectAttendance> findEarlyDepartures(LocalDate fechaInicio, LocalDate fechaFin, String usuarioId) {
        JsonNode data = client.executeQuery("ListarSalidasAnticipadas", nullableRangeVariables(fechaInicio, fechaFin, usuarioId));
        return DataConnectJsonMapper.attendances(data.get("asistencias"));
    }

    private Map<String, Object> nullableRangeVariables(LocalDate fechaInicio, LocalDate fechaFin, String usuarioId) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("fechaInicio", fechaInicio == null ? null : fechaInicio.toString());
        variables.put("fechaFin", fechaFin == null ? null : fechaFin.toString());
        variables.put("usuarioId", usuarioId);
        return variables;
    }
}
