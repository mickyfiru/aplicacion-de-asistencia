package com.asistencia.model.dataconnect;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class DataConnectAttendance {
    private final String id;
    private final String usuarioId;
    private final LocalDate fecha;
    private final OffsetDateTime horaEntrada;
    private final String estadoEntrada;
    private final int minutosAtraso;
    private final OffsetDateTime horaSalida;
    private final String estadoSalida;
    private final Integer minutosSalidaAnticipada;

    public DataConnectAttendance(
            String usuarioId,
            LocalDate fecha,
            OffsetDateTime horaEntrada,
            String estadoEntrada,
            int minutosAtraso,
            OffsetDateTime horaSalida,
            String estadoSalida,
            Integer minutosSalidaAnticipada
    ) {
        this(null, usuarioId, fecha, horaEntrada, estadoEntrada, minutosAtraso, horaSalida, estadoSalida, minutosSalidaAnticipada);
    }

    public DataConnectAttendance(
            String id,
            String usuarioId,
            LocalDate fecha,
            OffsetDateTime horaEntrada,
            String estadoEntrada,
            int minutosAtraso,
            OffsetDateTime horaSalida,
            String estadoSalida,
            Integer minutosSalidaAnticipada
    ) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.estadoEntrada = estadoEntrada;
        this.minutosAtraso = minutosAtraso;
        this.horaSalida = horaSalida;
        this.estadoSalida = estadoSalida;
        this.minutosSalidaAnticipada = minutosSalidaAnticipada;
    }

    public String getId() {
        return id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public OffsetDateTime getHoraEntrada() {
        return horaEntrada;
    }

    public String getEstadoEntrada() {
        return estadoEntrada;
    }

    public int getMinutosAtraso() {
        return minutosAtraso;
    }

    public OffsetDateTime getHoraSalida() {
        return horaSalida;
    }

    public String getEstadoSalida() {
        return estadoSalida;
    }

    public Integer getMinutosSalidaAnticipada() {
        return minutosSalidaAnticipada;
    }
}
