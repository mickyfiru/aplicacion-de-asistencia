package com.asistencia.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public final class DateTimeUtil {
    private DateTimeUtil() {
    }

    public static Optional<LocalDate> parseOptionalDate(String text) {
        if (ValidationUtil.isBlank(text)) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(text.trim()));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("La fecha debe tener formato yyyy-MM-dd");
        }
    }
}
