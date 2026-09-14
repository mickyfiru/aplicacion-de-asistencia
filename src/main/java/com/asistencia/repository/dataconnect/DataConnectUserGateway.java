package com.asistencia.repository.dataconnect;

import com.asistencia.model.dataconnect.CreateDataConnectUserRequest;
import com.asistencia.model.dataconnect.DataConnectUser;

import java.util.List;
import java.util.Optional;

public interface DataConnectUserGateway {
    DataConnectUser create(CreateDataConnectUserRequest request, String passwordHash);

    Optional<DataConnectUser> findById(String id);

    Optional<DataConnectUser> findByEmail(String correo);

    Optional<DataConnectUser> findByRut(String rut);

    Optional<DataConnectUser> findByAuthUid(String authUid);

    List<DataConnectUser> findActive();
}
