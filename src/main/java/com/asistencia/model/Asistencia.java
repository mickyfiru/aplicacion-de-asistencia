package com.asistencia.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Asistencia {
    private Integer id;
    private int usuarioId;
    private TipoAsistencia tipo;
    private LocalDate fecha;
    private LocalTime hora;

    public Asistencia(Integer id, int usuarioId, TipoAsistencia tipo, LocalDate fecha, LocalTime hora) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.fecha = fecha;
        this.hora = hora;
    }

    public Asistencia(int usuarioId, TipoAsistencia tipo, LocalDate fecha, LocalTime hora) {
        this(null, usuarioId, tipo, fecha, hora);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public TipoAsistencia getTipo() {
        return tipo;
    }

    public void setTipo(TipoAsistencia tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }
}
