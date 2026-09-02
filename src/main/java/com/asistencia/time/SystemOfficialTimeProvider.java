package com.asistencia.time;

import java.time.Clock;
import java.time.LocalDateTime;

public class SystemOfficialTimeProvider implements OfficialTimeProvider {
    private final Clock clock;

    public SystemOfficialTimeProvider() {
        this(Clock.systemDefaultZone());
    }

    public SystemOfficialTimeProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
