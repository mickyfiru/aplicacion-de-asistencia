package com.asistencia.repository.dataconnect;

import com.asistencia.model.dataconnect.DataConnectAttendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DataConnectAttendanceGateway {
    DataConnectAttendance createEntry(String usuarioId, LocalDate fecha, String estadoEntrada, int minutosAtraso);

    Optional<DataConnectAttendance> findByUserAndDate(String usuarioId, LocalDate fecha);

    DataConnectAttendance markExit(String usuarioId, LocalDate fecha, String estadoSalida, int minutosSalidaAnticipada);

    List<DataConnectAttendance> findByUserAndDateRange(String usuarioId, LocalDate fechaInicio, LocalDate fechaFin);

    List<DataConnectAttendance> findLateArrivals(LocalDate fechaInicio, LocalDate fechaFin, String usuarioId);

    List<DataConnectAttendance> findEarlyDepartures(LocalDate fechaInicio, LocalDate fechaFin, String usuarioId);
}
