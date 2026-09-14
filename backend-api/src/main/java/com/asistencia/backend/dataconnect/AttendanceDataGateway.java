package com.asistencia.backend.dataconnect;

import com.asistencia.backend.model.BackendAttendance;
import com.asistencia.backend.model.BackendSchedule;
import com.asistencia.backend.model.BackendUser;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceDataGateway {
    Optional<BackendUser> findUserByAuthUid(String authUid);

    Optional<BackendSchedule> findActiveSchedule(String userId, int chileDayOfWeek);

    Optional<BackendAttendance> findAttendance(String userId, LocalDate businessDate);

    BackendAttendance createServerEntry(String userId, LocalDate businessDate);

    BackendAttendance updateEntryResult(String userId, LocalDate businessDate, String state, int lateMinutes);

    BackendAttendance setServerExit(String userId, LocalDate businessDate);

    BackendAttendance updateExitResult(String userId, LocalDate businessDate, String state, int earlyMinutes);
}
