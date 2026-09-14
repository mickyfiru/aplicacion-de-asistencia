package com.asistencia.service;

import com.asistencia.model.dataconnect.DataConnectAttendance;
import com.asistencia.model.dataconnect.DataConnectSchedule;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class AttendancePresentationCalculator {
    public static final ZoneId CHILE_ZONE = ZoneId.of("America/Santiago");
    public static final String ENTRY_ON_TIME = "A_TIEMPO";
    public static final String ENTRY_LATE = "ATRASO";
    public static final String EXIT_NORMAL = "NORMAL";
    public static final String EXIT_EARLY = "ANTICIPADA";

    public LocalDate chileDate(OffsetDateTime timestamp) {
        return timestamp.toInstant().atZone(CHILE_ZONE).toLocalDate();
    }

    public LocalTime chileTime(OffsetDateTime timestamp) {
        return timestamp.toInstant().atZone(CHILE_ZONE).toLocalTime().truncatedTo(ChronoUnit.SECONDS);
    }

    public ZonedDateTime chileDateTime(OffsetDateTime timestamp) {
        return timestamp.toInstant().atZone(CHILE_ZONE).truncatedTo(ChronoUnit.SECONDS);
    }

    public String entryState(DataConnectAttendance attendance, DataConnectSchedule schedule) {
        return lateMinutes(attendance, schedule) == 0 ? ENTRY_ON_TIME : ENTRY_LATE;
    }

    public int lateMinutes(DataConnectAttendance attendance, DataConnectSchedule schedule) {
        ZonedDateTime entryDateTime = chileDateTime(attendance.getHoraEntrada());
        ZonedDateTime scheduledEntryDateTime = scheduledDateTime(entryDateTime, schedule.getHoraEntrada());
        return positiveMinutesBetween(scheduledEntryDateTime, entryDateTime);
    }

    public String exitState(DataConnectAttendance attendance, DataConnectSchedule schedule) {
        return earlyExitMinutes(attendance, schedule) == 0 ? EXIT_NORMAL : EXIT_EARLY;
    }

    public int earlyExitMinutes(DataConnectAttendance attendance, DataConnectSchedule schedule) {
        if (attendance.getHoraSalida() == null) {
            return 0;
        }
        ZonedDateTime exitDateTime = chileDateTime(attendance.getHoraSalida());
        ZonedDateTime scheduledExitDateTime = scheduledDateTime(exitDateTime, schedule.getHoraSalida());
        return positiveMinutesBetween(exitDateTime, scheduledExitDateTime);
    }

    private ZonedDateTime scheduledDateTime(ZonedDateTime serverDateTimeInChile, LocalTime scheduledTime) {
        return serverDateTimeInChile.toLocalDate().atTime(scheduledTime).atZone(CHILE_ZONE);
    }

    private int positiveMinutesBetween(ZonedDateTime start, ZonedDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        return Math.max(0, Math.toIntExact(minutes));
    }
}
