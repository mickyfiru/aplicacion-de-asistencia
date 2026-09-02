package com.asistencia.controller;

import com.asistencia.model.AttendanceCorrectionRequest;
import com.asistencia.model.AttendanceFilter;
import com.asistencia.model.AttendanceRecord;
import com.asistencia.model.EarlyDepartureReportItem;
import com.asistencia.model.LateArrivalReportItem;
import com.asistencia.model.Usuario;
import com.asistencia.model.WorkerReference;
import com.asistencia.service.AttendanceService;

import java.util.List;

public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    public AttendanceRecord registerEntry(WorkerReference worker) {
        return attendanceService.registerEntry(worker);
    }

    public AttendanceRecord registerExit(WorkerReference worker) {
        return attendanceService.registerExit(worker);
    }

    public List<LateArrivalReportItem> getLateArrivals(AttendanceFilter filter) {
        return attendanceService.findLateArrivals(filter);
    }

    public List<EarlyDepartureReportItem> getEarlyDepartures(AttendanceFilter filter) {
        return attendanceService.findEarlyDepartures(filter);
    }

    public AttendanceRecord correctRecord(AttendanceCorrectionRequest request, Usuario administrator) {
        return attendanceService.correctRecord(request, administrator);
    }
}
