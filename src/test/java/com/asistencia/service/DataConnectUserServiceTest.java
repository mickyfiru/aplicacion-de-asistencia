package com.asistencia.service;

import com.asistencia.exception.DuplicateUserException;
import com.asistencia.model.dataconnect.CreateDataConnectUserRequest;
import com.asistencia.model.dataconnect.DataConnectUser;
import com.asistencia.repository.dataconnect.DataConnectUserGateway;
import com.asistencia.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataConnectUserServiceTest {
    @Test
    void usuarioDuplicadoPorCorreoYRutEsRechazado() {
        FakeDataConnectUserGateway gateway = new FakeDataConnectUserGateway();
        gateway.users.add(user("1", "11.111.111-1", "ana@example.com"));
        DataConnectUserService service = new DataConnectUserService(gateway);

        DuplicateUserException exception = assertThrows(DuplicateUserException.class, () -> service.createUser(
                request("11.111.111-1", "ana@example.com")
        ));

        assertEquals("Ya existe un usuario con ese correo", exception.getMessage());
    }

    @Test
    void correoDuplicadoEsRechazado() {
        FakeDataConnectUserGateway gateway = new FakeDataConnectUserGateway();
        gateway.users.add(user("1", "11.111.111-1", "ana@example.com"));
        DataConnectUserService service = new DataConnectUserService(gateway);

        DuplicateUserException exception = assertThrows(DuplicateUserException.class, () -> service.createUser(
                request("22.222.222-2", "ana@example.com")
        ));

        assertEquals("Ya existe un usuario con ese correo", exception.getMessage());
    }

    @Test
    void rutDuplicadoEsRechazado() {
        FakeDataConnectUserGateway gateway = new FakeDataConnectUserGateway();
        gateway.users.add(user("1", "11.111.111-1", "ana@example.com"));
        DataConnectUserService service = new DataConnectUserService(gateway);

        DuplicateUserException exception = assertThrows(DuplicateUserException.class, () -> service.createUser(
                request("11.111.111-1", "otra@example.com")
        ));

        assertEquals("Ya existe un usuario con ese RUT", exception.getMessage());
    }

    @Test
    void crearUsuarioGuardaPasswordHasheada() {
        FakeDataConnectUserGateway gateway = new FakeDataConnectUserGateway();
        DataConnectUserService service = new DataConnectUserService(gateway);

        DataConnectUser created = service.createUser(request("22.222.222-2", "ana@example.com"));

        assertNotEquals("secreto123", gateway.lastPasswordHash);
        assertTrue(PasswordUtil.verifyPassword("secreto123", gateway.lastPasswordHash));
        assertEquals(created.getPasswordHash(), gateway.lastPasswordHash);
    }

    private CreateDataConnectUserRequest request(String rut, String correo) {
        return new CreateDataConnectUserRequest("Ana", "Perez", rut, correo, "firebase-uid-1", "secreto123", "TRABAJADOR");
    }

    private DataConnectUser user(String id, String rut, String correo) {
        return new DataConnectUser(id, "Ana", "Perez", rut, correo, "firebase-uid-" + id, "hash", "TRABAJADOR", true);
    }

    private static class FakeDataConnectUserGateway implements DataConnectUserGateway {
        private final List<DataConnectUser> users = new ArrayList<>();
        private String lastPasswordHash;

        @Override
        public DataConnectUser create(CreateDataConnectUserRequest request, String passwordHash) {
            lastPasswordHash = passwordHash;
            DataConnectUser user = new DataConnectUser(
                    String.valueOf(users.size() + 1),
                    request.getNombre(),
                    request.getApellido(),
                    request.getRut(),
                    request.getCorreo(),
                    request.getAuthUid(),
                    passwordHash,
                    request.getRol(),
                    true
            );
            users.add(user);
            return user;
        }

        @Override
        public Optional<DataConnectUser> findById(String id) {
            return users.stream().filter(user -> user.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<DataConnectUser> findByEmail(String correo) {
            return users.stream().filter(user -> user.getCorreo().equalsIgnoreCase(correo)).findFirst();
        }

        @Override
        public Optional<DataConnectUser> findByRut(String rut) {
            return users.stream().filter(user -> user.getRut().equals(rut)).findFirst();
        }

        @Override
        public Optional<DataConnectUser> findByAuthUid(String authUid) {
            return users.stream().filter(user -> user.getAuthUid().equals(authUid)).findFirst();
        }

        @Override
        public List<DataConnectUser> findActive() {
            return users.stream().filter(DataConnectUser::isActivo).toList();
        }
    }
}
