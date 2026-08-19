package com.asistencia.database;

import com.asistencia.model.Rol;
import com.asistencia.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private final DatabaseConnection databaseConnection;

    public DatabaseInitializer(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void initialize() {
        try (Connection connection = databaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS usuarios (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT NOT NULL,
                        correo TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        rol TEXT NOT NULL,
                        activo INTEGER NOT NULL DEFAULT 1
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS asistencias (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        usuario_id INTEGER NOT NULL,
                        tipo TEXT NOT NULL,
                        fecha TEXT NOT NULL,
                        hora TEXT NOT NULL,
                        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
                    )
                    """);

            insertAdminIfMissing(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo inicializar la base de datos", exception);
        }
    }

    private void insertAdminIfMissing(Connection connection) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM usuarios WHERE correo = ?";
        try (PreparedStatement countStatement = connection.prepareStatement(countSql)) {
            countStatement.setString(1, "admin@empresa.cl");
            try (ResultSet resultSet = countStatement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    return;
                }
            }
        }

        String insertSql = "INSERT INTO usuarios (nombre, correo, password, rol, activo) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            insertStatement.setString(1, "Administrador de prueba");
            insertStatement.setString(2, "admin@empresa.cl");
            insertStatement.setString(3, PasswordUtil.hashPassword("Admin123"));
            insertStatement.setString(4, Rol.ADMINISTRADOR.name());
            insertStatement.executeUpdate();
        }
    }
}
