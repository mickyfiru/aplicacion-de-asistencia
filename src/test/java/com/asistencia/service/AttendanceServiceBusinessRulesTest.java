package com.asistencia.service;

import com.asistencia.controller.AttendanceController;
import com.asistencia.model.AttendanceFilter;
import com.asistencia.model.AttendanceRecord;
import com.asistencia.model.AttendanceStatus;
import com.asistencia.model.EarlyDepartureReportItem;
import com.asistencia.model.ExitStatus;
import com.asistencia.model.LateArrivalReportItem;
import com.asistencia.model.WorkerReference;
import com.asistencia.repository.InMemoryAttendanceRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttendanceServiceBusinessRulesTest {
    private final WorkerReference worker = new WorkerReference(10, "Ana Perez", "12.345.678-9");

    @Test
    void entrada0759EsATiempo() {
        AttendanceRecord record = serviceAt("2026-09-02T07:59:00").registerEntry(worker);

        assertEquals(AttendanceStatus.A_TIEMPO, record.getAttendanceStatus());
        assertEquals(0, record.getLateMinutes());
    }

    @Test
    void entrada0800EsATiempo() {
        AttendanceRecord record = serviceAt("2026-09-02T08:00:00").registerEntry(worker);

        assertEquals(AttendanceStatus.A_TIEMPO, record.getAttendanceStatus());
        assertEquals(0, record.getLateMinutes());
    }

    @Test
    void entrada0801EsAtrasoDeUnMinuto() {
        AttendanceRecord record = serviceAt("2026-09-02T08:01:00").registerEntry(worker);

        assertEquals(AttendanceStatus.ATRASO, record.getAttendanceStatus());
        assertEquals(1, record.getLateMinutes());
    }

    @Test
    void entrada0830EsAtrasoDeTreintaMinutos() {
        AttendanceRecord record = serviceAt("2026-09-02T08:30:00").registerEntry(worker);

        assertEquals(AttendanceStatus.ATRASO, record.getAttendanceStatus());
        assertEquals(30, record.getLateMinutes());
    }

    @Test
    void salida1723EsSalidaAnticipadaDeUnMinuto() {
        InMemoryAttendanceRepository repository = new InMemoryAttendanceRepository();
        FakeOfficialTimeProvider timeProvider = new FakeOfficialTimeProvider(LocalDateTime.parse("2026-09-02T08:00:00"));
        AttendanceService service = new AttendanceService(repository, timeProvider);
        service.registerEntry(worker);
        timeProvider.setCurrentDateTime(LocalDateTime.parse("2026-09-02T17:23:00"));

        AttendanceRecord record = service.registerExit(worker);

        assertEquals(ExitStatus.SALIDA_ANTICIPADA, record.getExitStatus());
        assertEquals(1, record.getMissingMinutes());
    }

    @Test
    void salida1724EsNormal() {
        AttendanceRecord record = registerEntryAndExit("2026-09-02T17:24:00");

        assertEquals(ExitStatus.SALIDA_NORMAL, record.getExitStatus());
        assertEquals(0, record.getMissingMinutes());
    }

    @Test
    void salida1730EsNormal() {
        AttendanceRecord record = registerEntryAndExit("2026-09-02T17:30:00");

        assertEquals(ExitStatus.SALIDA_NORMAL, record.getExitStatus());
        assertEquals(0, record.getMissingMinutes());
    }

    @Test
    void dosEntradasMismoDiaSonRechazadas() {
        AttendanceService service = serviceAt("2026-09-02T08:00:00");
        service.registerEntry(worker);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.registerEntry(worker));
        assertEquals("Ya existe un registro de entrada para este trabajador en la fecha actual.", exception.getMessage());
    }

    @Test
    void dosSalidasMismoDiaSonRechazadas() {
        InMemoryAttendanceRepository repository = new InMemoryAttendanceRepository();
        FakeOfficialTimeProvider timeProvider = new FakeOfficialTimeProvider(LocalDateTime.parse("2026-09-02T08:00:00"));
        AttendanceService service = new AttendanceService(repository, timeProvider);
        service.registerEntry(worker);
        timeProvider.setCurrentDateTime(LocalDateTime.parse("2026-09-02T17:24:00"));
        service.registerExit(worker);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.registerExit(worker));
        assertEquals("Ya existe un registro de salida para este trabajador en la fecha actual.", exception.getMessage());
    }

    @Test
    void horaFalsaDelClienteNoSeUsaParaRegistrarEntrada() {
        LocalDateTime fakeClientDateTime = LocalDateTime.parse("2026-09-02T07:30:00");
        FakeOfficialTimeProvider officialTime = new FakeOfficialTimeProvider(LocalDateTime.parse("2026-09-02T08:30:00"));
        AttendanceController controller = new AttendanceController(
                new AttendanceService(new InMemoryAttendanceRepository(), officialTime)
        );

        AttendanceRecord record = controller.registerEntry(worker);

        assertEquals(LocalTime.of(7, 30), fakeClientDateTime.toLocalTime());
        assertEquals(LocalTime.of(8, 30), record.getEntryTime());
        assertEquals(AttendanceStatus.ATRASO, record.getAttendanceStatus());
        assertEquals(30, record.getLateMinutes());
    }

    @Test
    void controladorNoExponeParametrosDeHoraParaEntradaOSalida() {
        List<Method> methods = Arrays.stream(AttendanceController.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("registerEntry") || method.getName().equals("registerExit"))
                .toList();

        assertFalse(methods.isEmpty());
        for (Method method : methods) {
            boolean receivesClientTime = Arrays.stream(method.getParameterTypes())
                    .anyMatch(type -> type.equals(LocalDateTime.class)
                            || type.equals(LocalTime.class)
                            || type.equals(LocalDate.class));
            assertFalse(receivesClientTime);
        }
    }

    @Test
    void consultaAtrasosPermiteFiltroPorFecha() {
        InMemoryAttendanceRepository repository = new InMemoryAttendanceRepository();
        FakeOfficialTimeProvider timeProvider = new FakeOfficialTimeProvider(LocalDateTime.parse("2026-09-02T08:30:00"));
        AttendanceService service = new AttendanceService(repository, timeProvider);
        service.registerEntry(worker);

        List<LateArrivalReportItem> lateArrivals = service.findLateArrivals(AttendanceFilter.byDate(LocalDate.parse("2026-09-02")));

        assertEquals(1, lateArrivals.size());
        assertEquals(30, lateArrivals.get(0).getLateMinutes());
    }

    @Test
    void consultaSalidasAnticipadasPermiteFiltroPorTrabajador() {
        InMemoryAttendanceRepository repository = new InMemoryAttendanceRepository();
        FakeOfficialTimeProvider timeProvider = new FakeOfficialTimeProvider(LocalDateTime.parse("2026-09-02T08:00:00"));
        AttendanceService service = new AttendanceService(repository, timeProvider);
        service.registerEntry(worker);
        timeProvider.setCurrentDateTime(LocalDateTime.parse("2026-09-02T17:05:00"));
        service.registerExit(worker);

        List<EarlyDepartureReportItem> earlyDepartures = service.findEarlyDepartures(AttendanceFilter.byWorker(10));

        assertEquals(1, earlyDepartures.size());
        assertEquals(19, earlyDepartures.get(0).getMissingMinutes());
    }

    private AttendanceService serviceAt(String dateTime) {
        return new AttendanceService(
                new InMemoryAttendanceRepository(),
                new FakeOfficialTimeProvider(LocalDateTime.parse(dateTime))
        );
    }

    private AttendanceRecord registerEntryAndExit(String exitDateTime) {
        InMemoryAttendanceRepository repository = new InMemoryAttendanceRepository();
        FakeOfficialTimeProvider timeProvider = new FakeOfficialTimeProvider(LocalDateTime.parse("2026-09-02T08:00:00"));
        AttendanceService service = new AttendanceService(repository, timeProvider);
        service.registerEntry(worker);
        timeProvider.setCurrentDateTime(LocalDateTime.parse(exitDateTime));
        return service.registerExit(worker);
    }
}
