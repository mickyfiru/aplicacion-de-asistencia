package com.asistencia.model.dataconnect;

import java.time.LocalTime;
import java.util.List;

public class DataConnectSchedule {
    private final String id;
    private final String usuarioId;
    private final List<Integer> diasSemana;
    private final LocalTime horaEntrada;
    private final LocalTime horaSalida;
    private final boolean activo;

    public DataConnectSchedule(String id, String usuarioId, List<Integer> diasSemana, LocalTime horaEntrada, LocalTime horaSalida, boolean activo) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.diasSemana = List.copyOf(diasSemana);
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.activo = activo;
    }

    public String getId() {
        return id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public List<Integer> getDiasSemana() {
        return diasSemana;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public boolean isActivo() {
        return activo;
    }
}
