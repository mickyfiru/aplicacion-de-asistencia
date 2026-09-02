package com.asistencia.time;

import java.time.LocalDateTime;

public interface OfficialTimeProvider {
    LocalDateTime now();
}
