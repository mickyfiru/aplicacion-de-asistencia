package com.asistencia.repository;

import com.asistencia.model.AttendanceFilter;
import com.asistencia.model.AttendanceRecord;
import com.asistencia.model.AuditLog;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository {
    AttendanceRecord save(AttendanceRecord record);

    Optional<AttendanceRecord> findById(String recordId);

    Optional<AttendanceRecord> findByWorkerAndDate(int workerId, LocalDate date);

    boolean hasEntryForDate(int workerId, LocalDate date);

    boolean hasExitForDate(int workerId, LocalDate date);

    List<AttendanceRecord> findLateArrivals(AttendanceFilter filter);

    List<AttendanceRecord> findEarlyDepartures(AttendanceFilter filter);

    AuditLog saveAuditLog(AuditLog auditLog);

    List<AuditLog> findAuditLogsByRecord(String recordId);
}
