package ru.practicum.dto.output;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.state.UserProfileState;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventInitiatorDto {
    private Long id;
    private String name;
    private UserProfileState profile;
    private Long events;
    private Long subscribers;

    public Long getEvents() {
        return events == null ? 0 : events;
    }

    public Long getSubscribers() {
        return subscribers == null ? 0 : subscribers;
    }
}