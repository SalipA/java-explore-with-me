package ru.practicum.dto.output;

import lombok.Data;
import ru.practicum.state.SubscriptionState;

@Data
public class SubscriptionDto {
    private UserShortDto from;
    private UserShortDto to;
    private SubscriptionState state;
}
