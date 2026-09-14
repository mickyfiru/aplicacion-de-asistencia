package com.asistencia.service;

import com.asistencia.model.Usuario;

import java.util.Optional;

public interface LoginService {
    Optional<Usuario> login(String correo, String password);

    default void logout() {
    }
}
