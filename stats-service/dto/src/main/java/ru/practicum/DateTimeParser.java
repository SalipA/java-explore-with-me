package ru.practicum;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeParser {
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    public static LocalDateTime parseToDate(String str) {
        return LocalDateTime.parse(str, formatter);
    }

    public static String parseToString(LocalDateTime localDateTime) {
        return localDateTime.format(formatter);
    }
}