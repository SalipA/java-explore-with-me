package ru.practicum.dto.input;

import lombok.*;
import ru.practicum.state.RequestState;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    @NotNull(message = "must not be null")
    private List<Long> requestIds;
    @NotNull(message = "must not be null")
    private RequestState status;
}