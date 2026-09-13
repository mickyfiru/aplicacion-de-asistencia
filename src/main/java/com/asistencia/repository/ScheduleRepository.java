package com.asistencia.repository;

import com.asistencia.model.WorkSchedule;

import java.time.LocalDate;
import java.util.Optional;

public interface ScheduleRepository {
    Optional<WorkSchedule> findByWorkerAndDate(int workerId, LocalDate date);
}
