package com.asistencia.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    public static final String DEFAULT_DB_URL = "jdbc:sqlite:asistencia.db";

    private final String jdbcUrl;

    public DatabaseConnection() {
        this(System.getProperty("asistencia.db.url", DEFAULT_DB_URL));
    }

    public DatabaseConnection(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }
}
