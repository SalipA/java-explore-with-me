package ru.practicum.dto.output;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.state.RequestState;

import java.time.LocalDateTime;

@Data
public class ParticipationRequestDto {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    private Long event;
    private Long id;
    private Long requester;
    private RequestState status;
}
