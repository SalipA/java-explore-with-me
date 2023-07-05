package ru.practicum.state;

import ru.practicum.exception.NotFoundException;

public enum EventState {
    PENDING, PUBLISHED, CANCELED;

    public static EventState getState(String state) {
        if (state.toUpperCase().equals(PENDING.name())) {
            return PENDING;
        } else if (state.toUpperCase().equals(PUBLISHED.name())) {
            return PUBLISHED;
        } else if (state.toUpperCase().equals(CANCELED.name())) {
            return CANCELED;
        } else {
            String message = "Event state = " + state + " was not found";
            throw new NotFoundException(message);
        }
    }
}