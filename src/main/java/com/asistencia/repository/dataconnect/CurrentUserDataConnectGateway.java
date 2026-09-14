package com.asistencia.repository.dataconnect;

import com.asistencia.model.dataconnect.DataConnectAttendance;
import com.asistencia.model.dataconnect.DataConnectSchedule;
import com.asistencia.model.dataconnect.DataConnectUser;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CurrentUserDataConnectGateway {
    Optional<DataConnectUser> findCurrentUser();

    Optional<DataConnectSchedule> findCurrentSchedule(int diaSemana);

    Optional<DataConnectAttendance> findOpenAttendance();

    Optional<DataConnectAttendance> findLatestAttendance();

    List<DataConnectAttendance> findMyAttendances(OffsetDateTime desde, OffsetDateTime hasta);

    DataConnectAttendance markMyEntry();

    DataConnectAttendance markMyExit();
}
