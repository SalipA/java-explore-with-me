package ru.practicum.dto.output;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ru.practicum.state.EventState;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EventShortDto extends EventOutputDto {
    @JsonIgnore
    public EventState getState() {
        return super.getState();
    }

    @JsonIgnore
    public LocalDateTime getPublishedOn() {
        return super.getPublishedOn();
    }
}