package com.asistencia.service;

import com.asistencia.exception.MissingScheduleException;
import com.asistencia.model.AbsenceReportItem;
import com.asistencia.model.WorkSchedule;
import com.asistencia.model.WorkerReference;
import com.asistencia.repository.InMemoryAttendanceRepository;
import com.asistencia.repository.InMemoryScheduleRepository;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttendanceScheduleAndAbsenceTest {
    @Test
    void usuarioSinHorarioNoPuedeRegistrarEntrada() {
        InMemoryScheduleRepository schedules = new InMemoryScheduleRepository();
        AttendanceService service = new AttendanceService(
                new InMemoryAttendanceRepository(),
                new FakeOfficialTimeProvider(LocalDateTime.parse("2026-09-02T08:00:00")),
                schedules
        );
        WorkerReference worker = new WorkerReference(30, "Camila Vera", "10.000.000-1");

        assertThrows(MissingScheduleException.class, () -> service.registerEntry(worker));
    }

    @Test
    void trabajadorSinAsistenciaEnDiaLaboralApareceComoInasistente() {
        InMemoryScheduleRepository schedules = new InMemoryScheduleRepository();
        WorkerReference worker = new WorkerReference(30, "Camila Vera", "10.000.000-1");
        schedules.save(new WorkSchedule(
                worker.getWorkerId(),
                LocalTime.of(8, 0),
                LocalTime.of(17, 24),
                60,
                EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
        ));
        AttendanceService service = new AttendanceService(
                new InMemoryAttendanceRepository(),
                new FakeOfficialTimeProvider(LocalDateTime.parse("2026-09-02T08:00:00")),
                schedules
        );

        List<AbsenceReportItem> absences = service.findAbsences(List.of(worker), LocalDate.parse("2026-09-02"));

        assertEquals(1, absences.size());
        assertEquals("INASISTENCIA", absences.get(0).getStatus());
    }

    @Test
    void trabajadorConAsistenciaNoApareceComoInasistente() {
        InMemoryScheduleRepository schedules = new InMemoryScheduleRepository();
        WorkerReference worker = new WorkerReference(30, "Camila Vera", "10.000.000-1");
        schedules.save(WorkSchedule.standardMondayToFriday(worker.getWorkerId()));
        AttendanceService service = new AttendanceService(
                new InMemoryAttendanceRepository(),
                new FakeOfficialTimeProvider(LocalDateTime.parse("2026-09-02T08:00:00")),
                schedules
        );
        service.registerEntry(worker);

        List<AbsenceReportItem> absences = service.findAbsences(List.of(worker), LocalDate.parse("2026-09-02"));

        assertEquals(0, absences.size());
    }
}
