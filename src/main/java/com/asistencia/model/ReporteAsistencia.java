package com.asistencia.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReporteAsistencia {
    private final String usuario;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final String observacion;

    public ReporteAsistencia(String usuario, LocalDate fecha, LocalTime hora, String observacion) {
        this.usuario = usuario;
        this.fecha = fecha;
        this.hora = hora;
        this.observacion = observacion;
    }

    public String getUsuario() {
        return usuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getObservacion() {
        return observacion;
    }
}
