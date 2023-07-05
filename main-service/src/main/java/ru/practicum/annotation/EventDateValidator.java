package ru.practicum.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Clock;
import java.time.LocalDateTime;

public class EventDateValidator implements ConstraintValidator<EventDateConstraint, LocalDateTime> {
    private static final int TIME_PERIOD_IN_HOURS = 2;
    @Autowired
    Clock clock;

    @Override
    public boolean isValid(LocalDateTime eventDate, ConstraintValidatorContext constraintValidatorContext) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime afterConstraintHours = now.plusHours(TIME_PERIOD_IN_HOURS);
        return eventDate.isAfter(afterConstraintHours) || eventDate.equals(afterConstraintHours);
    }
}

