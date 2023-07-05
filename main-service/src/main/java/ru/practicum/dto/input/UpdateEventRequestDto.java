package ru.practicum.dto.input;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.lang.Nullable;
import ru.practicum.entity.Location;

import javax.validation.constraints.Future;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class UpdateEventRequestDto {
    @Nullable
    @Size(min = 20, max = 2000, message = "length must be min=20, max=2000")
    protected String annotation;
    @Nullable
    protected Long category;
    @Nullable
    @Size(min = 20, max = 7000, message = "length must be min=20, max=7000")
    protected String description;
    @Nullable
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Future
    protected LocalDateTime eventDate;
    @Nullable
    protected Location location;
    @Nullable
    protected Boolean paid;
    @Nullable
    protected Integer participantLimit;
    @Nullable
    protected Boolean requestModeration;
    @Nullable
    @Size(min = 3, max = 120, message = "length must be min=3, max=120")
    protected String title;
}