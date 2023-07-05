package ru.practicum.mapper;

import ru.practicum.dto.output.SubscriptionDto;
import ru.practicum.entity.Subscription;

import java.util.List;
import java.util.stream.Collectors;

public class SubscriptionMapper {
    public static SubscriptionDto toSubscriptionDto(Subscription subscription) {
        SubscriptionDto subscriptionDto = new SubscriptionDto();
        subscriptionDto.setFrom(UserMapper.toUserShortDto(subscription.getSubscriber()));
        subscriptionDto.setTo(UserMapper.toUserShortDto(subscription.getSubscribedTo()));
        subscriptionDto.setState(subscription.getState());
        return subscriptionDto;
    }

    public static List<SubscriptionDto> toSubscriptionDtoList(List<Subscription> subscriptions) {
        return subscriptions.stream().map(SubscriptionMapper::toSubscriptionDto).collect(Collectors.toList());
    }
}
