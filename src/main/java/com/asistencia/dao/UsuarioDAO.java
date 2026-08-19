package com.asistencia.dao;

import com.asistencia.database.DatabaseConnection;
import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO {
    private final DatabaseConnection databaseConnection;

    public UsuarioDAO(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public Usuario crear(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, correo, password, rol, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, usuario);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    usuario.setId(keys.getInt(1));
                }
            }
            return usuario;
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo crear el usuario", exception);
        }
    }

    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre = ?, correo = ?, password = ?, rol = ?, activo = ? WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, usuario);
            statement.setInt(6, usuario.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo actualizar el usuario", exception);
        }
    }

    public void eliminarLogico(int usuarioId) {
        String sql = "UPDATE usuarios SET activo = 0 WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo eliminar el usuario", exception);
        }
    }

    public List<Usuario> listarActivos() {
        String sql = "SELECT * FROM usuarios WHERE activo = 1 ORDER BY nombre";
        return consultarUsuarios(sql);
    }

    public List<Usuario> listarTrabajadoresActivos() {
        String sql = "SELECT * FROM usuarios WHERE activo = 1 AND rol = 'USUARIO' ORDER BY nombre";
        return consultarUsuarios(sql);
    }

    public Optional<Usuario> buscarPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUsuario(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo buscar el usuario", exception);
        }
    }

    public Optional<Usuario> buscarActivoPorCorreo(String correo) {
        String sql = "SELECT * FROM usuarios WHERE lower(correo) = lower(?) AND activo = 1";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, correo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUsuario(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo buscar el usuario", exception);
        }
    }

    public boolean existeCorreo(String correo, Integer ignoredUserId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE lower(correo) = lower(?) AND (? IS NULL OR id <> ?)";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, correo);
            if (ignoredUserId == null) {
                statement.setObject(2, null);
                statement.setObject(3, null);
            } else {
                statement.setInt(2, ignoredUserId);
                statement.setInt(3, ignoredUserId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo validar el correo", exception);
        }
    }

    private List<Usuario> consultarUsuarios(String sql) {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                usuarios.add(mapUsuario(resultSet));
            }
            return usuarios;
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudieron consultar usuarios", exception);
        }
    }

    private void fillStatement(PreparedStatement statement, Usuario usuario) throws SQLException {
        statement.setString(1, usuario.getNombre());
        statement.setString(2, usuario.getCorreo());
        statement.setString(3, usuario.getPasswordHash());
        statement.setString(4, usuario.getRol().name());
        statement.setInt(5, usuario.isActivo() ? 1 : 0);
    }

    private Usuario mapUsuario(ResultSet resultSet) throws SQLException {
        return new Usuario(
                resultSet.getInt("id"),
                resultSet.getString("nombre"),
                resultSet.getString("correo"),
                resultSet.getString("password"),
                Rol.valueOf(resultSet.getString("rol")),
                resultSet.getInt("activo") == 1
        );
    }
}
