package com.asistencia.service;

import com.asistencia.model.dataconnect.DataConnectAttendance;
import com.asistencia.model.dataconnect.DataConnectSchedule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttendancePresentationCalculatorTest {
    private static final ZoneId CHILE_ZONE = ZoneId.of("America/Santiago");
    private final AttendancePresentationCalculator calculator = new AttendancePresentationCalculator();

    @Test
    void entrada0730ChileEsATiempo() {
        DataConnectAttendance attendance = attendanceWithEntry(chileTimestamp("2026-09-14T07:30:00"));

        assertEquals(AttendancePresentationCalculator.ENTRY_ON_TIME, calculator.entryState(attendance, schedule()));
        assertEquals(0, calculator.lateMinutes(attendance, schedule()));
    }

    @Test
    void entrada0800ChileEsATiempo() {
        DataConnectAttendance attendance = attendanceWithEntry(chileTimestamp("2026-09-14T08:00:00"));

        assertEquals(AttendancePresentationCalculator.ENTRY_ON_TIME, calculator.entryState(attendance, schedule()));
        assertEquals(0, calculator.lateMinutes(attendance, schedule()));
    }

    @Test
    void entrada0815ChileEsAtrasoDe15Minutos() {
        DataConnectAttendance attendance = attendanceWithEntry(chileTimestamp("2026-09-14T08:15:00"));

        assertEquals(AttendancePresentationCalculator.ENTRY_LATE, calculator.entryState(attendance, schedule()));
        assertEquals(15, calculator.lateMinutes(attendance, schedule()));
    }

    @Test
    void salida1700ChileEsAnticipadaDe30Minutos() {
        DataConnectAttendance attendance = attendanceWithExit(chileTimestamp("2026-09-14T17:00:00"));

        assertEquals(AttendancePresentationCalculator.EXIT_EARLY, calculator.exitState(attendance, schedule()));
        assertEquals(30, calculator.earlyExitMinutes(attendance, schedule()));
    }

    @Test
    void salida1730ChileEsNormal() {
        DataConnectAttendance attendance = attendanceWithExit(chileTimestamp("2026-09-14T17:30:00"));

        assertEquals(AttendancePresentationCalculator.EXIT_NORMAL, calculator.exitState(attendance, schedule()));
        assertEquals(0, calculator.earlyExitMinutes(attendance, schedule()));
    }

    @Test
    void salida1800ChileEsNormal() {
        DataConnectAttendance attendance = attendanceWithExit(chileTimestamp("2026-09-14T18:00:00"));

        assertEquals(AttendancePresentationCalculator.EXIT_NORMAL, calculator.exitState(attendance, schedule()));
        assertEquals(0, calculator.earlyExitMinutes(attendance, schedule()));
    }

    @Test
    void timestampUtcRealSeInterpretaEnAmericaSantiago() {
        DataConnectAttendance attendance = attendanceWithEntry(OffsetDateTime.parse("2026-09-14T03:51:46.086149Z"));

        assertEquals(LocalDate.of(2026, 9, 14), calculator.chileDate(attendance.getHoraEntrada()));
        assertEquals(LocalTime.of(0, 51, 46), calculator.chileTime(attendance.getHoraEntrada()));
        assertEquals(AttendancePresentationCalculator.ENTRY_ON_TIME, calculator.entryState(attendance, schedule()));
        assertEquals(0, calculator.lateMinutes(attendance, schedule()));
    }

    @Test
    void zonaHorariaDelPcNoAlteraResultadoConInstantServidorFijo() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
            DataConnectAttendance attendance = attendanceWithEntry(chileTimestamp("2026-09-14T08:15:00"));

            assertEquals(AttendancePresentationCalculator.ENTRY_LATE, calculator.entryState(attendance, schedule()));
            assertEquals(15, calculator.lateMinutes(attendance, schedule()));
        } finally {
            TimeZone.setDefault(original);
        }
    }

    private DataConnectSchedule schedule() {
        return new DataConnectSchedule(
                "schedule-1",
                "user-1",
                List.of(1, 2, 3, 4, 5),
                LocalTime.of(8, 0),
                LocalTime.of(17, 30),
                true
        );
    }

    private DataConnectAttendance attendanceWithEntry(OffsetDateTime entry) {
        return new DataConnectAttendance(
                "user-1",
                null,
                entry,
                "PENDIENTE",
                0,
                null,
                null,
                null
        );
    }

    private DataConnectAttendance attendanceWithExit(OffsetDateTime exit) {
        return new DataConnectAttendance(
                "user-1",
                null,
                chileTimestamp("2026-09-14T08:00:00"),
                "PENDIENTE",
                0,
                exit,
                null,
                null
        );
    }

    private OffsetDateTime chileTimestamp(String localDateTime) {
        return LocalDateTime.parse(localDateTime).atZone(CHILE_ZONE).toOffsetDateTime();
    }
}
