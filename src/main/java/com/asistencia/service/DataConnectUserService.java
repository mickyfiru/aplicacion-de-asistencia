package com.asistencia.service;

import com.asistencia.exception.DuplicateUserException;
import com.asistencia.model.dataconnect.CreateDataConnectUserRequest;
import com.asistencia.model.dataconnect.DataConnectUser;
import com.asistencia.repository.dataconnect.DataConnectUserGateway;
import com.asistencia.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

public class DataConnectUserService {
    private final DataConnectUserGateway userGateway;

    public DataConnectUserService(DataConnectUserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public DataConnectUser createUser(CreateDataConnectUserRequest request) {
        userGateway.findByEmail(request.getCorreo()).ifPresent(user -> {
            throw new DuplicateUserException("Ya existe un usuario con ese correo");
        });
        userGateway.findByRut(request.getRut()).ifPresent(user -> {
            throw new DuplicateUserException("Ya existe un usuario con ese RUT");
        });
        return userGateway.create(request, PasswordUtil.hashPassword(request.getPassword()));
    }

    public Optional<DataConnectUser> findById(String id) {
        return userGateway.findById(id);
    }

    public Optional<DataConnectUser> findByEmail(String correo) {
        return userGateway.findByEmail(correo);
    }

    public Optional<DataConnectUser> findByRut(String rut) {
        return userGateway.findByRut(rut);
    }

    public Optional<DataConnectUser> findByAuthUid(String authUid) {
        return userGateway.findByAuthUid(authUid);
    }

    public List<DataConnectUser> findActiveUsers() {
        return userGateway.findActive();
    }
}
