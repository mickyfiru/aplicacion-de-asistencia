package com.asistencia.repository.dataconnect;

import com.asistencia.model.dataconnect.DataConnectSchedule;

import java.util.Optional;

public interface DataConnectScheduleGateway {
    Optional<DataConnectSchedule> findActiveByUserAndDay(String usuarioId, int diaSemana);
}
