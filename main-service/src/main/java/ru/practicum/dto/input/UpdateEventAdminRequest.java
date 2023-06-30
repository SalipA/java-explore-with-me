package ru.practicum.dto.input;

import lombok.*;
import org.springframework.lang.Nullable;
import ru.practicum.state.StateActionAdmin;

@Getter
@Setter
@NoArgsConstructor
public class UpdateEventAdminRequest extends UpdateEventRequestDto {
    @Nullable
    private StateActionAdmin stateAction;
}