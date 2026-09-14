package com.asistencia.repository.dataconnect;

import com.asistencia.api.DataConnectGraphQlClient;
import com.asistencia.model.dataconnect.DataConnectSchedule;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Optional;

public class DataConnectScheduleRepository implements DataConnectScheduleGateway {
    private final DataConnectGraphQlClient client;

    public DataConnectScheduleRepository(DataConnectGraphQlClient client) {
        this.client = client;
    }

    @Override
    public Optional<DataConnectSchedule> findActiveByUserAndDay(String usuarioId, int diaSemana) {
        JsonNode data = client.executeQuery("ObtenerHorarioActivoUsuario", Map.of(
                "usuarioId", usuarioId,
                "diaSemana", diaSemana
        ));
        JsonNode horarios = data.get("horarios");
        if (horarios == null || !horarios.isArray() || horarios.isEmpty()) {
            return Optional.empty();
        }
        return DataConnectJsonMapper.optionalSchedule(horarios.get(0));
    }
}
