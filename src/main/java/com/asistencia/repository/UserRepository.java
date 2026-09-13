package com.asistencia.repository;

import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<Usuario> authenticate(String email, String password);

    Optional<Usuario> findById(int userId);

    Optional<Usuario> findByEmail(String email);

    Usuario create(String name, String email, String password, Rol role);

    Usuario update(Usuario user);

    void deactivate(int userId);

    List<Usuario> findActiveUsers();
}
