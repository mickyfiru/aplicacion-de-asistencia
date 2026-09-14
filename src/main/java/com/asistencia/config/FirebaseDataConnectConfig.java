package com.asistencia.config;

public class FirebaseDataConnectConfig {
    public static final String DEFAULT_PROJECT_ID = "app-asistencia-5e6fe";
    public static final String DEFAULT_CONNECTOR = "southamerica-west1/app-asistencia-5e6fe-service/default";

    private final String projectId;
    private final String connector;

    public FirebaseDataConnectConfig() {
        this(
                valueOrDefault(DEFAULT_PROJECT_ID, "FIREBASE_PROJECT_ID", "FIREBASE_PROJECT"),
                valueOrDefault(DEFAULT_CONNECTOR, "FIREBASE_DATACONNECT_CONNECTOR_RESOURCE", "FDC_CONNECTOR")
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

    private static String valueOrDefault(String defaultValue, String... environmentNames) {
        for (String environmentName : environmentNames) {
            String value = System.getenv(environmentName);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return defaultValue;
    }
}
