package ru.practicum.dto.input;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.lang.Nullable;
import ru.practicum.annotation.EventDateConstraint;
import ru.practicum.entity.Location;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class NewEventDto {
    @NotBlank(message = "must not be blank")
    @Size(min = 20, max = 2000, message = "length must be min=20, max=2000")
    private String annotation;

    @NotNull(message = "must not be null")
    @Positive(message = "must be positive")
    private Long category;

    @NotBlank(message = "must not be blank")
    @Size(min = 20, max = 7000, message = "length must be min=20, max=7000")
    private String description;

    @NotNull(message = "must not be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Future
    @EventDateConstraint
    private LocalDateTime eventDate;

    @NotNull(message = "must not be null")
    private Location location;

    @Nullable
    private Boolean paid = false;

    @Nullable
    private Boolean requestModeration = true;

    @Nullable
    @PositiveOrZero
    private Integer participantLimit = 0;

    @NotBlank(message = "must not be blank")
    @Size(min = 3, max = 120, message = "length must be min=3, max=120")
    private String title;
}