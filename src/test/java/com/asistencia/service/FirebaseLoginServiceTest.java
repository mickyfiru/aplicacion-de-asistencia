package com.asistencia.service;

import com.asistencia.auth.AuthSessionManager;
import com.asistencia.auth.FirebaseAuthRestClient;
import com.asistencia.auth.FirebaseAuthSession;
import com.asistencia.config.FirebaseAuthConfig;
import com.asistencia.exception.AuthenticatedUserNotRegisteredException;
import com.asistencia.exception.InvalidCredentialsException;
import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;
import com.asistencia.model.dataconnect.DataConnectAttendance;
import com.asistencia.model.dataconnect.DataConnectSchedule;
import com.asistencia.model.dataconnect.DataConnectUser;
import com.asistencia.repository.dataconnect.CurrentUserDataConnectGateway;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebaseLoginServiceTest {
    @Test
    void loginCorrectoObtieneUsuarioPostgresql() {
        AuthSessionManager sessionManager = new AuthSessionManager(authClient(false));
        FirebaseLoginService service = new FirebaseLoginService(authClient(false), sessionManager, new FakeGateway(Optional.of(user())));

        Optional<Usuario> result = service.login("ana@example.com", "secreto");

        assertTrue(result.isPresent());
        assertEquals("Ana", result.get().getNombre());
        assertEquals(Rol.USUARIO, result.get().getRol());
        assertTrue(sessionManager.current().isPresent());
    }

    @Test
    void loginIncorrectoNoDejaSesion() {
        AuthSessionManager sessionManager = new AuthSessionManager(authClient(true));
        FirebaseLoginService service = new FirebaseLoginService(authClient(true), sessionManager, new FakeGateway(Optional.of(user())));

        assertTrue(service.login("ana@example.com", "malo").isEmpty());
        assertTrue(sessionManager.current().isEmpty());
    }

    @Test
    void usuarioAutenticadoSinUserPostgresqlEsRechazado() {
        AuthSessionManager sessionManager = new AuthSessionManager(authClient(false));
        FirebaseLoginService service = new FirebaseLoginService(authClient(false), sessionManager, new FakeGateway(Optional.empty()));

        assertThrows(AuthenticatedUserNotRegisteredException.class, () -> service.login("ana@example.com", "secreto"));
        assertTrue(sessionManager.current().isEmpty());
    }

    @Test
    void logoutLimpiaSesion() {
        AuthSessionManager sessionManager = new AuthSessionManager(authClient(false));
        FirebaseLoginService service = new FirebaseLoginService(authClient(false), sessionManager, new FakeGateway(Optional.of(user())));
        service.login("ana@example.com", "secreto");

        service.logout();

        assertTrue(sessionManager.current().isEmpty());
    }

    private FirebaseAuthRestClient authClient(boolean fail) {
        return new FirebaseAuthRestClient(new FirebaseAuthConfig("api-key")) {
            @Override
            public FirebaseAuthSession signInWithPassword(String email, String password) {
                if (fail) {
                    throw new InvalidCredentialsException("Credenciales Firebase incorrectas");
                }
                return new FirebaseAuthSession("id-token", "refresh-token", "firebase-uid", Instant.now().plusSeconds(3600));
            }
        };
    }

    private DataConnectUser user() {
        return new DataConnectUser("user-1", "Ana", "Perez", "11.111.111-1", "ana@example.com", "firebase-uid", null, "TRABAJADOR", true);
    }

    private static class FakeGateway implements CurrentUserDataConnectGateway {
        private final Optional<DataConnectUser> user;

        FakeGateway(Optional<DataConnectUser> user) {
            this.user = user;
        }

        @Override
        public Optional<DataConnectUser> findCurrentUser() {
            return user;
        }

        @Override
        public Optional<DataConnectSchedule> findCurrentSchedule(int diaSemana) {
            return Optional.empty();
        }

        @Override
        public Optional<DataConnectAttendance> findOpenAttendance() {
            return Optional.empty();
        }

        @Override
        public Optional<DataConnectAttendance> findLatestAttendance() {
            return Optional.empty();
        }

        @Override
        public List<DataConnectAttendance> findMyAttendances(OffsetDateTime desde, OffsetDateTime hasta) {
            return List.of();
        }

        @Override
        public DataConnectAttendance markMyEntry() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataConnectAttendance markMyExit() {
            throw new UnsupportedOperationException();
        }
    }
}
