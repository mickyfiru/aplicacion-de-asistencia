package com.asistencia.config;

public class FirebaseDataConnectConfig {
    public static final String DEFAULT_PROJECT_ID = "sistema-asistencia-77dcc";
    public static final String DEFAULT_CONNECTOR = "southamerica-west1/sistema-asistencia-77dcc-service/default";

    private final String projectId;
    private final String connector;

    public FirebaseDataConnectConfig() {
        this(
                valueOrDefault(System.getenv("FIREBASE_PROJECT"), DEFAULT_PROJECT_ID),
                valueOrDefault(System.getenv("FDC_CONNECTOR"), DEFAULT_CONNECTOR)
        );
    }

    public FirebaseDataConnectConfig(String projectId, String connector) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el proyecto Firebase");
        }
        if (connector == null || connector.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el conector Firebase Data Connect");
        }
        this.projectId = projectId.trim();
        this.connector = connector.trim();
    }

    public String getProjectId() {
        return projectId;
    }

    public String getConnector() {
        return connector;
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
