package com.asistencia.service;

import com.asistencia.dao.AsistenciaDAO;
import com.asistencia.dao.UsuarioDAO;
import com.asistencia.model.ReporteAsistencia;
import com.asistencia.model.ReporteInasistencia;
import com.asistencia.model.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReporteService {
    public static final LocalTime HORA_LIMITE_ENTRADA = LocalTime.of(9, 30);
    public static final LocalTime HORA_LIMITE_SALIDA = LocalTime.of(17, 30);

    private final UsuarioDAO usuarioDAO;
    private final AsistenciaDAO asistenciaDAO;

    public ReporteService(UsuarioDAO usuarioDAO, AsistenciaDAO asistenciaDAO) {
        this.usuarioDAO = usuarioDAO;
        this.asistenciaDAO = asistenciaDAO;
    }

    public List<ReporteAsistencia> obtenerAtrasos(LocalDate fecha) {
        return asistenciaDAO.listarEntradasTardias(HORA_LIMITE_ENTRADA, fecha);
    }

    public List<ReporteAsistencia> obtenerSalidasAnticipadas(LocalDate fecha) {
        return asistenciaDAO.listarSalidasAnticipadas(HORA_LIMITE_SALIDA, fecha);
    }

    public List<ReporteInasistencia> obtenerInasistencias(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("Debe indicar una fecha");
        }
        List<ReporteInasistencia> inasistencias = new ArrayList<>();
        for (Usuario usuario : usuarioDAO.listarTrabajadoresActivos()) {
            if (!asistenciaDAO.usuarioTieneRegistrosEnFecha(usuario.getId(), fecha)) {
                inasistencias.add(new ReporteInasistencia(usuario.getNombre(), fecha, "Inasistencia"));
            }
        }
        return inasistencias;
    }
}
