package com.asistencia.service;

import com.asistencia.dao.UsuarioDAO;
import com.asistencia.model.Usuario;
import com.asistencia.util.PasswordUtil;
import com.asistencia.util.ValidationUtil;

import java.util.Optional;

public class AuthService {
    private final UsuarioDAO usuarioDAO;

    public AuthService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Optional<Usuario> login(String correo, String password) {
        if (ValidationUtil.isBlank(correo) || ValidationUtil.isBlank(password)) {
            return Optional.empty();
        }
        Optional<Usuario> usuario = usuarioDAO.buscarActivoPorCorreo(correo.trim());
        if (usuario.isEmpty()) {
            return Optional.empty();
        }
        return PasswordUtil.verifyPassword(password, usuario.get().getPasswordHash()) ? usuario : Optional.empty();
    }
}
