package com.asistencia.backend.web;

import com.asistencia.backend.model.BackendSchedule;
import com.asistencia.backend.model.EntryResult;
import com.asistencia.backend.model.ExitResult;
import com.asistencia.backend.model.TodayAttendanceResult;
import com.asistencia.backend.security.AuthenticatedUser;
import com.asistencia.backend.security.BearerTokenExtractor;
import com.asistencia.backend.security.TokenVerifier;
import com.asistencia.backend.service.AttendanceWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AttendanceController {
    private final TokenVerifier tokenVerifier;
    private final AttendanceWorkflowService attendanceService;

    public AttendanceController(TokenVerifier tokenVerifier, AttendanceWorkflowService attendanceService) {
        this.tokenVerifier = tokenVerifier;
        this.attendanceService = attendanceService;
    }

    @PostMapping("/asistencia/entrada")
    public EntryResult entry(@RequestHeader("Authorization") String authorizationHeader) {
        return attendanceService.markEntry(authenticatedUser(authorizationHeader));
    }

    @PostMapping("/asistencia/salida")
    public ExitResult exit(@RequestHeader("Authorization") String authorizationHeader) {
        return attendanceService.markExit(authenticatedUser(authorizationHeader));
    }

    @GetMapping("/asistencia/hoy")
    public ResponseEntity<TodayAttendanceResult> today(@RequestHeader("Authorization") String authorizationHeader) {
        TodayAttendanceResult result = attendanceService.today(authenticatedUser(authorizationHeader));
        return result == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }

    @GetMapping("/horario/mio")
    public ScheduleResponse mySchedule(@RequestHeader("Authorization") String authorizationHeader) {
        BackendSchedule schedule = attendanceService.mySchedule(authenticatedUser(authorizationHeader));
        return new ScheduleResponse(schedule.diasSemana(), schedule.horaEntrada(), schedule.horaSalida());
    }

    private AuthenticatedUser authenticatedUser(String authorizationHeader) {
        return tokenVerifier.verify(BearerTokenExtractor.extract(authorizationHeader));
    }
}
