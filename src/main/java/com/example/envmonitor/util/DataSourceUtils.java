package com.example.envmonitor.util;

import java.util.List;
import java.util.Locale;
import org.springframework.util.StringUtils;

public final class DataSourceUtils {
    public static final String REAL_SERIAL = "REAL_SERIAL";
    public static final String REAL_MQTT = "REAL_MQTT";
    public static final String MOCK = "MOCK";
    public static final String MANUAL = "MANUAL";
    public static final String ALL = "ALL";

    public static final List<String> REAL_SOURCES = List.of(REAL_SERIAL, REAL_MQTT);
    public static final List<String> WRITE_SOURCES = List.of(REAL_SERIAL, REAL_MQTT, MOCK, MANUAL);
    public static final List<String> QUERY_SOURCES = List.of(REAL_SERIAL, REAL_MQTT, MOCK, MANUAL, ALL);

    private DataSourceUtils() {
    }

    public static String normalizeQuerySource(String source) {
        return normalize(source, REAL_SERIAL, QUERY_SOURCES);
    }

    public static String normalizeWriteSource(String source) {
        return normalize(source, MANUAL, WRITE_SOURCES);
    }

    public static boolean isAll(String source) {
        return ALL.equals(normalizeQuerySource(source));
    }

    public static boolean isReal(String source) {
        String normalized = normalizeQuerySource(source);
        return REAL_SERIAL.equals(normalized) || REAL_MQTT.equals(normalized);
    }

    private static String normalize(String source, String defaultValue, List<String> allowed) {
        if (!StringUtils.hasText(source)) {
            return defaultValue;
        }
        String normalized = source.trim().toUpperCase(Locale.ROOT);
        return allowed.contains(normalized) ? normalized : defaultValue;
    }
}
