package com.asistencia.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class ChileBusinessCalendar {
    public static final ZoneId ZONE = ZoneId.of("America/Santiago");

    public LocalDate currentDate(OfficialTimeProvider officialTimeProvider) {
        return officialTimeProvider.now().toLocalDate();
    }

    public LocalTime currentMinute(OfficialTimeProvider officialTimeProvider) {
        return officialTimeProvider.now().toLocalTime().withSecond(0).withNano(0);
    }

    public int dayOfWeek(LocalDate date) {
        return date.getDayOfWeek().getValue();
    }

    public LocalDateTime currentDateTime(OfficialTimeProvider officialTimeProvider) {
        return officialTimeProvider.now().withSecond(0).withNano(0);
    }
}
