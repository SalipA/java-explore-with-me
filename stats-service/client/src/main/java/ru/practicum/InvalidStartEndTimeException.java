package ru.practicum;

public class InvalidStartEndTimeException extends RuntimeException {
    public InvalidStartEndTimeException(String message) {
        super(message);
    }
}
