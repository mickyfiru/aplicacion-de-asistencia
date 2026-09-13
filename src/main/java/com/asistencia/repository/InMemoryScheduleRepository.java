package com.asistencia.repository;

import com.asistencia.model.WorkSchedule;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryScheduleRepository implements ScheduleRepository {
    private final Map<Integer, WorkSchedule> schedulesByWorker = new HashMap<>();

    public void save(WorkSchedule schedule) {
        schedulesByWorker.put(schedule.getWorkerId(), schedule);
    }

    @Override
    public Optional<WorkSchedule> findByWorkerAndDate(int workerId, LocalDate date) {
        return Optional.ofNullable(schedulesByWorker.get(workerId))
                .filter(schedule -> schedule.worksOn(date.getDayOfWeek()));
    }
}
