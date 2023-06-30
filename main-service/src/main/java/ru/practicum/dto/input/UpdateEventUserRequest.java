package ru.practicum.dto.input;

import lombok.*;
import org.springframework.lang.Nullable;
import ru.practicum.state.StateActionPrivate;

@Getter
@Setter
@NoArgsConstructor
public class UpdateEventUserRequest extends UpdateEventRequestDto {
    @Nullable
    private StateActionPrivate stateAction;
}
