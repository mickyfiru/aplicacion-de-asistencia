package com.asistencia.backend.config;

public record DataConnectSettings(String projectId, String location, String serviceId, String connectorId) {
    public static DataConnectSettings fromEnvironment() {
        return new DataConnectSettings(
                valueOrDefault("app-asistencia-5e6fe", "FIREBASE_PROJECT_ID", "FIREBASE_PROJECT"),
                valueOrDefault("southamerica-west1", "FIREBASE_DATACONNECT_LOCATION", "FDC_LOCATION"),
                valueOrDefault("app-asistencia-5e6fe-service", "FIREBASE_DATACONNECT_SERVICE", "FDC_SERVICE"),
                valueOrDefault("default", "FIREBASE_DATACONNECT_CONNECTOR", "FDC_CONNECTOR")
        );
    }

    public String connectorResourceName() {
        return "projects/" + projectId
                + "/locations/" + location
                + "/services/" + serviceId
                + "/connectors/" + connectorId;
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
