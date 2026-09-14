package com.asistencia.repository.dataconnect;

import com.asistencia.api.DataConnectGraphQlClient;
import com.asistencia.exception.InvalidBackendResponseException;
import com.asistencia.model.dataconnect.CreateDataConnectUserRequest;
import com.asistencia.model.dataconnect.DataConnectUser;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DataConnectUserRepository implements DataConnectUserGateway {
    private final DataConnectGraphQlClient client;

    public DataConnectUserRepository(DataConnectGraphQlClient client) {
        this.client = client;
    }

    @Override
    public DataConnectUser create(CreateDataConnectUserRequest request, String passwordHash) {
        client.executeMutation("CrearUsuario", Map.of(
                "nombre", request.getNombre(),
                "apellido", request.getApellido(),
                "rut", request.getRut(),
                "correo", request.getCorreo(),
                "authUid", request.getAuthUid(),
                "passwordHash", passwordHash,
                "rol", request.getRol()
        ));
        return findByEmail(request.getCorreo())
                .orElseThrow(() -> new InvalidBackendResponseException("Data Connect no devolvio el usuario creado"));
    }

    @Override
    public Optional<DataConnectUser> findById(String id) {
        JsonNode data = client.executeQuery("BuscarUsuarioPorId", Map.of("id", id));
        return DataConnectJsonMapper.optionalUser(data.get("user"));
    }

    @Override
    public Optional<DataConnectUser> findByEmail(String correo) {
        JsonNode data = client.executeQuery("BuscarUsuarioPorCorreo", Map.of("correo", correo.trim().toLowerCase()));
        return firstUser(data.get("users"));
    }

    @Override
    public Optional<DataConnectUser> findByRut(String rut) {
        JsonNode data = client.executeQuery("BuscarUsuarioPorRut", Map.of("rut", rut.trim()));
        return firstUser(data.get("users"));
    }

    @Override
    public Optional<DataConnectUser> findByAuthUid(String authUid) {
        JsonNode data = client.executeQuery("BuscarUsuarioPorAuthUid", Map.of("authUid", authUid.trim()));
        return firstUser(data.get("users"));
    }

    @Override
    public List<DataConnectUser> findActive() {
        JsonNode data = client.executeQuery("ListarUsuariosActivos", Map.of());
        return DataConnectJsonMapper.users(data.get("users"));
    }

    private Optional<DataConnectUser> firstUser(JsonNode users) {
        if (users == null || !users.isArray() || users.isEmpty()) {
            return Optional.empty();
        }
        return DataConnectJsonMapper.optionalUser(users.get(0));
    }
}
