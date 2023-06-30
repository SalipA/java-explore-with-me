package ru.practicum.dto.output;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.state.EventState;
import ru.practicum.dto.reversible.CategoryDto;

import java.time.LocalDateTime;

@Data
public class EventOutputDto {
    protected String annotation;
    protected CategoryDto category;
    protected Long confirmedRequests;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime eventDate;
    protected Long id;
    protected UserShortDto initiator;
    protected Boolean paid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime publishedOn;
    protected EventState state;
    protected String title;
    protected Long views;

    public Long getViews() {
        return views == null ? 0 : views;
    }
}
