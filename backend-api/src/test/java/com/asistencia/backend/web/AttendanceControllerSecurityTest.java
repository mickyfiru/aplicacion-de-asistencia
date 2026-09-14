package com.asistencia.backend.web;

import com.asistencia.backend.dataconnect.AttendanceDataGateway;
import com.asistencia.backend.model.BackendAttendance;
import com.asistencia.backend.model.BackendSchedule;
import com.asistencia.backend.model.BackendUser;
import com.asistencia.backend.security.AuthenticatedUser;
import com.asistencia.backend.security.TokenVerifier;
import com.asistencia.backend.service.AttendanceWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AttendanceControllerSecurityTest {
    @Test
    void healthNoRequiereToken() throws Exception {
        mvc(token -> {
            throw new AssertionError("Health no debe verificar token");
        }, new EmptyGateway())
                .perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void endpointProtegidoSinTokenResponde401() throws Exception {
        mvc(token -> new AuthenticatedUser("uid", "worker@example.com", Map.of(), false), new EmptyGateway())
                .perform(post("/api/asistencia/entrada"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void endpointProtegidoConTokenInvalidoResponde401() throws Exception {
        mvc(token -> {
            throw new UnauthorizedException("Token Firebase invalido");
        }, new EmptyGateway())
                .perform(post("/api/asistencia/entrada").header("Authorization", "Bearer token-malo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void tokenValidoSinUsuarioRegistradoRespondeControlado() throws Exception {
        mvc(token -> new AuthenticatedUser("uid-no-registrado", "worker@example.com", Map.of(), false), new EmptyGateway())
                .perform(post("/api/asistencia/entrada").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("no está registrado")));
    }

    private MockMvc mvc(TokenVerifier tokenVerifier, AttendanceDataGateway gateway) {
        AttendanceWorkflowService service = new AttendanceWorkflowService(gateway, Clock.systemUTC());
        return MockMvcBuilders.standaloneSetup(
                        new HealthController(),
                        new AttendanceController(tokenVerifier, service)
                )
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    private static class EmptyGateway implements AttendanceDataGateway {
        @Override
        public Optional<BackendUser> findUserByAuthUid(String authUid) {
            return Optional.empty();
        }

        @Override
        public Optional<BackendSchedule> findActiveSchedule(String userId, int chileDayOfWeek) {
            return Optional.empty();
        }

        @Override
        public Optional<BackendAttendance> findAttendance(String userId, LocalDate businessDate) {
            return Optional.empty();
        }

        @Override
        public BackendAttendance createServerEntry(String userId, LocalDate businessDate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BackendAttendance updateEntryResult(String userId, LocalDate businessDate, String state, int lateMinutes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BackendAttendance setServerExit(String userId, LocalDate businessDate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BackendAttendance updateExitResult(String userId, LocalDate businessDate, String state, int earlyMinutes) {
            throw new UnsupportedOperationException();
        }
    }
}
