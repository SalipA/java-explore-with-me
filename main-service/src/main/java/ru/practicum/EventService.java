package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolationException;

@Service
@Slf4j
public class EventService {
    private final StatsClient statsClient;

    public EventService(StatsClient statsClient) {
        this.statsClient = statsClient;
    }

    public void getEvent() {
        EndpointHitDto hitDto = new EndpointHitDto();
        try {
            statsClient.saveHit(hitDto);
        } catch (ConstraintViolationException exp) {
            log.error(exp.getMessage());
        }
    }
}