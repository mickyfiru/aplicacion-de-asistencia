package com.asistencia.service;

import com.asistencia.dao.UsuarioDAO;
import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;
import com.asistencia.util.PasswordUtil;
import com.asistencia.util.ValidationUtil;

import java.util.List;

public class UsuarioService {
    private final UsuarioDAO usuarioDAO;

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario crearUsuario(String nombre, String correo, String password, Rol rol) {
        validarDatosUsuario(nombre, correo, password, rol, null);
        Usuario usuario = new Usuario(nombre.trim(), correo.trim().toLowerCase(), PasswordUtil.hashPassword(password), rol);
        return usuarioDAO.crear(usuario);
    }

    public void modificarUsuario(int id, String nombre, String correo, String password, Rol rol, boolean activo) {
        validarDatosUsuario(nombre, correo, password, rol, id);
        Usuario usuario = new Usuario(id, nombre.trim(), correo.trim().toLowerCase(), PasswordUtil.hashPassword(password), rol, activo);
        usuarioDAO.actualizar(usuario);
    }

    public void eliminarUsuario(int usuarioId) {
        // Eliminacion logica: mantiene intactos los registros historicos de asistencia.
        usuarioDAO.eliminarLogico(usuarioId);
    }

    public List<Usuario> listarUsuariosActivos() {
        return usuarioDAO.listarActivos();
    }

    private void validarDatosUsuario(String nombre, String correo, String password, Rol rol, Integer ignoredUserId) {
        if (ValidationUtil.isBlank(nombre)) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (!ValidationUtil.isValidEmail(correo)) {
            throw new IllegalArgumentException("El correo no tiene un formato valido");
        }
        if (ValidationUtil.isBlank(password)) {
            throw new IllegalArgumentException("La contrasena no puede estar vacia");
        }
        if (rol == null) {
            throw new IllegalArgumentException("Debe seleccionar un rol");
        }
        if (usuarioDAO.existeCorreo(correo.trim(), ignoredUserId)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }
    }
}
