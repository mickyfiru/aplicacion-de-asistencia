package com.asistencia.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class WorkSchedule {
    private final int workerId;
    private final LocalTime entryTime;
    private final LocalTime exitTime;
    private final int lunchMinutes;
    private final Set<DayOfWeek> workDays;

    public WorkSchedule(
            int workerId,
            LocalTime entryTime,
            LocalTime exitTime,
            int lunchMinutes,
            Set<DayOfWeek> workDays
    ) {
        if (workerId <= 0) {
            throw new IllegalArgumentException("El ID del trabajador debe ser valido");
        }
        if (entryTime == null || exitTime == null) {
            throw new IllegalArgumentException("El horario debe tener entrada y salida");
        }
        if (!exitTime.isAfter(entryTime)) {
            throw new IllegalArgumentException("La hora de salida debe ser posterior a la entrada");
        }
        if (lunchMinutes < 0) {
            throw new IllegalArgumentException("Los minutos de colacion no pueden ser negativos");
        }
        if (workDays == null || workDays.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar dias laborales");
        }
        this.workerId = workerId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.lunchMinutes = lunchMinutes;
        this.workDays = Collections.unmodifiableSet(EnumSet.copyOf(workDays));
    }

    public static WorkSchedule standardMondayToFriday(int workerId) {
        return new WorkSchedule(
                workerId,
                LocalTime.of(8, 0),
                LocalTime.of(17, 24),
                60,
                EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
        );
    }

    public int getWorkerId() {
        return workerId;
    }

    public LocalTime getEntryTime() {
        return entryTime;
    }

    public LocalTime getExitTime() {
        return exitTime;
    }

    public int getLunchMinutes() {
        return lunchMinutes;
    }

    public Set<DayOfWeek> getWorkDays() {
        return workDays;
    }

    public boolean worksOn(DayOfWeek dayOfWeek) {
        return workDays.contains(dayOfWeek);
    }
}
