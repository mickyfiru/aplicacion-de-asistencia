package com.asistencia.model;

import java.time.LocalDate;

public class ReporteInasistencia {
    private final String usuario;
    private final LocalDate fecha;
    private final String observacion;

    public ReporteInasistencia(String usuario, LocalDate fecha, String observacion) {
        this.usuario = usuario;
        this.fecha = fecha;
        this.observacion = observacion;
    }

    public String getUsuario() {
        return usuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getObservacion() {
        return observacion;
    }
}
