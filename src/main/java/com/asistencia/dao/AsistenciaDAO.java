package com.asistencia.dao;

import com.asistencia.database.DatabaseConnection;
import com.asistencia.model.Asistencia;
import com.asistencia.model.ReporteAsistencia;
import com.asistencia.model.TipoAsistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AsistenciaDAO {
    private final DatabaseConnection databaseConnection;

    public AsistenciaDAO(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public Asistencia crear(Asistencia asistencia) {
        String sql = "INSERT INTO asistencias (usuario_id, tipo, fecha, hora) VALUES (?, ?, ?, ?)";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, asistencia.getUsuarioId());
            statement.setString(2, asistencia.getTipo().name());
            statement.setString(3, asistencia.getFecha().toString());
            statement.setString(4, asistencia.getHora().toString());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    asistencia.setId(keys.getInt(1));
                }
            }
            return asistencia;
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo crear el registro de asistencia", exception);
        }
    }

    public List<Asistencia> listarPorUsuarioYFecha(int usuarioId, LocalDate fecha) {
        String sql = "SELECT * FROM asistencias WHERE usuario_id = ? AND fecha = ? ORDER BY hora";
        List<Asistencia> asistencias = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioId);
            statement.setString(2, fecha.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    asistencias.add(mapAsistencia(resultSet));
                }
            }
            return asistencias;
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudieron consultar asistencias", exception);
        }
    }

    public Optional<Asistencia> buscarUltimaPorUsuarioYFecha(int usuarioId, LocalDate fecha) {
        String sql = "SELECT * FROM asistencias WHERE usuario_id = ? AND fecha = ? ORDER BY hora DESC, id DESC LIMIT 1";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioId);
            statement.setString(2, fecha.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapAsistencia(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo consultar el ultimo registro", exception);
        }
    }

    public boolean existeRegistro(int usuarioId, LocalDate fecha, TipoAsistencia tipo) {
        String sql = "SELECT COUNT(*) FROM asistencias WHERE usuario_id = ? AND fecha = ? AND tipo = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioId);
            statement.setString(2, fecha.toString());
            statement.setString(3, tipo.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo validar el registro de asistencia", exception);
        }
    }

    public List<ReporteAsistencia> listarEntradasTardias(LocalTime horaLimite, LocalDate fecha) {
        String sql = """
                SELECT u.nombre, a.fecha, a.hora
                FROM asistencias a
                JOIN usuarios u ON u.id = a.usuario_id
                WHERE u.activo = 1
                  AND u.rol = 'USUARIO'
                  AND a.tipo = 'ENTRADA'
                  AND a.hora > ?
                  AND (? IS NULL OR a.fecha = ?)
                ORDER BY a.fecha DESC, a.hora DESC
                """;
        return consultarReporteAsistencia(sql, horaLimite, fecha, "Atraso");
    }

    public List<ReporteAsistencia> listarSalidasAnticipadas(LocalTime horaLimite, LocalDate fecha) {
        String sql = """
                SELECT u.nombre, a.fecha, a.hora
                FROM asistencias a
                JOIN usuarios u ON u.id = a.usuario_id
                WHERE u.activo = 1
                  AND u.rol = 'USUARIO'
                  AND a.tipo = 'SALIDA'
                  AND a.hora < ?
                  AND (? IS NULL OR a.fecha = ?)
                ORDER BY a.fecha DESC, a.hora ASC
                """;
        return consultarReporteAsistencia(sql, horaLimite, fecha, "Salida anticipada");
    }

    public boolean usuarioTieneRegistrosEnFecha(int usuarioId, LocalDate fecha) {
        String sql = "SELECT COUNT(*) FROM asistencias WHERE usuario_id = ? AND fecha = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioId);
            statement.setString(2, fecha.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo consultar inasistencia", exception);
        }
    }

    private List<ReporteAsistencia> consultarReporteAsistencia(
            String sql,
            LocalTime horaLimite,
            LocalDate fecha,
            String observacion
    ) {
        List<ReporteAsistencia> registros = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, horaLimite.toString());
            if (fecha == null) {
                statement.setObject(2, null);
                statement.setObject(3, null);
            } else {
                statement.setString(2, fecha.toString());
                statement.setString(3, fecha.toString());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    registros.add(new ReporteAsistencia(
                            resultSet.getString("nombre"),
                            LocalDate.parse(resultSet.getString("fecha")),
                            LocalTime.parse(resultSet.getString("hora")),
                            observacion
                    ));
                }
            }
            return registros;
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo consultar el reporte", exception);
        }
    }

    private Asistencia mapAsistencia(ResultSet resultSet) throws SQLException {
        return new Asistencia(
                resultSet.getInt("id"),
                resultSet.getInt("usuario_id"),
                TipoAsistencia.valueOf(resultSet.getString("tipo")),
                LocalDate.parse(resultSet.getString("fecha")),
                LocalTime.parse(resultSet.getString("hora"))
        );
    }
}
