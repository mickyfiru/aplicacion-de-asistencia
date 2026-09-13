package com.asistencia.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FirebaseDataConnectConfigTest {
    @Test
    void contieneProyectoYConectorEntregadosPorBaseDeDatos() {
        FirebaseDataConnectConfig config = new FirebaseDataConnectConfig();

        assertEquals("sistema-asistencia-77dcc", config.getProjectId());
        assertEquals("southamerica-west1/sistema-asistencia-77dcc-service/default", config.getConnector());
    }
}
