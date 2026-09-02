package com.asistencia.service;

import com.asistencia.time.OfficialTimeProvider;

import java.time.LocalDateTime;

class FakeOfficialTimeProvider implements OfficialTimeProvider {
    private LocalDateTime currentDateTime;

    FakeOfficialTimeProvider(LocalDateTime currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    void setCurrentDateTime(LocalDateTime currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    @Override
    public LocalDateTime now() {
        return currentDateTime;
    }
}
