package com.asistencia.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FirebaseDataConnectConfigTest {
    @Test
    void contieneProyectoYConectorEntregadosPorBaseDeDatos() {
        FirebaseDataConnectConfig config = new FirebaseDataConnectConfig();

        assertEquals("app-asistencia-5e6fe", config.getProjectId());
        assertEquals("southamerica-west1/app-asistencia-5e6fe-service/default", config.getConnector());
    }
}
