package com.asistencia.repository;

import com.asistencia.model.WorkSchedule;

import java.time.LocalDate;
import java.util.Optional;

public class DefaultScheduleRepository implements ScheduleRepository {
    @Override
    public Optional<WorkSchedule> findByWorkerAndDate(int workerId, LocalDate date) {
        WorkSchedule schedule = WorkSchedule.standardMondayToFriday(workerId);
        return schedule.worksOn(date.getDayOfWeek()) ? Optional.of(schedule) : Optional.empty();
    }
}
