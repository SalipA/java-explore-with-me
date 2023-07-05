package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.input.EventRequestStatusUpdateRequest;
import ru.practicum.dto.input.NewEventDto;
import ru.practicum.dto.input.UpdateEventUserRequest;
import ru.practicum.dto.output.*;
import ru.practicum.dto.reversible.UserDto;
import ru.practicum.service.EventService;
import ru.practicum.service.RequestService;
import ru.practicum.service.UserService;
import ru.practicum.state.SubscriptionState;
import ru.practicum.state.UserProfileState;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
public class PrivateController {
    private final EventService eventService;
    private final RequestService requestService;
    private final UserService userService;

    public PrivateController(EventService eventService, RequestService requestService, UserService userService) {
        this.eventService = eventService;
        this.requestService = requestService;
        this.userService = userService;
    }

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable @Positive Long userId, @RequestBody @Valid NewEventDto newEventDto) {
        log.info("POST: /users/{}/events, value = {}", userId, newEventDto);
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(@PathVariable @Positive Long userId, @RequestParam(required = false,
        defaultValue = "0") @Min(0) Integer from, @RequestParam(required = false, defaultValue = "10") @Min(1) Integer
        size) {
        log.info("GET: /users/{}/events, from = {}, size = {}", userId, from, size);
        return eventService.getEventsPrivate(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable @Positive Long userId, @PathVariable @Positive Long eventId) {
        log.info("GET: /users/{}/events/{}", userId, eventId);
        return eventService.getEventPrivate(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable @Positive Long userId, @PathVariable @Positive Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("PATCH: /users/{}/events/{}, value = {}", userId, eventId, updateEventUserRequest);
        return eventService.updateEventUser(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getEventParticipants(@PathVariable @Positive Long userId,
                                                              @PathVariable @Positive Long eventId) {
        log.info("GET: /users/{}/events/{}/requests", userId, eventId);
        return requestService.getEventParticipants(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable @Positive Long userId,
                                                              @PathVariable @Positive Long eventId,
                                                              @RequestBody @Valid EventRequestStatusUpdateRequest
                                                                  updateRequest) {
        log.info("PATCH: /users/{}/events/{}/requests, value = {}", userId, eventId, updateRequest);
        return requestService.changeRequestStatus(userId, eventId, updateRequest);
    }


    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable @Positive Long userId, @RequestParam
    @Positive Long eventId) {
        log.info("POST: /users/{}/requests, eventId = {}", userId, eventId);
        return requestService.addParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        log.info("PATCH: /users/{}/requests/{}/cancel", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        log.info("GET: /users/{}/requests", userId);
        return requestService.getUserRequests(userId);
    }

    @PatchMapping("/{userId}/profile") //изменить профиль с публичного (по умолчанию) на приватный и наоборот
    @ResponseStatus(HttpStatus.OK)
    public UserDto changeUserProfile(@PathVariable @Positive Long userId, @RequestParam UserProfileState profile) {
        log.info("PATCH: /users/{}/profile, value = {}", userId, profile);
        return userService.changeUserProfile(userId, profile);
    }

    @PostMapping("/{subscriberId}/subscribe/{subscribesToId}") // подписаться на пользователя
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionDto addSubscription(@PathVariable @Positive Long subscriberId,
                                           @PathVariable @Positive Long subscribesToId) {
        log.info("POST: /users/{}/subscribe/{}", subscriberId, subscribesToId);
        return userService.addSubscription(subscriberId, subscribesToId);
    }

    @DeleteMapping("/{subscriberId}/subscribe/{subscribedToId}") // удалить свою подписку
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubscription(@PathVariable @Positive Long subscriberId,
                                   @PathVariable @Positive Long subscribedToId) {
        log.info("DELETE: /users/{}/subscribe/{}", subscriberId, subscribedToId);
        userService.deleteSubscription(subscriberId, subscribedToId);
    }

    @GetMapping("{userId}/subscriptions") // получить свои подписки на других пользователей или подписки на себя
    @ResponseStatus(HttpStatus.OK)
    public List<SubscriptionDto> getUsersSubscriptions(@PathVariable @Positive Long userId,
                                                       @RequestParam @NotBlank(message = "must be only 'TO_ME' or " +
                                                           "'FROM_ME'") String direction,
                                                       @RequestParam(required = false) SubscriptionState state,
                                                       @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                                       @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("GET: /users/{}/subscriptions, direction = {}, state = {}, from = {}, size = {}", userId,
            direction, state, from, size);
        return userService.getUsersSubscriptions(userId, direction, state, from, size);
    }

    @PatchMapping("/{subscribedToId}/subscribe/{subscriberId}") // изменить статут подписки на себя
    @ResponseStatus(HttpStatus.OK)
    public SubscriptionDto changeSubscriptionStatus(@PathVariable @Positive Long subscribedToId,
                                                    @PathVariable @Positive Long subscriberId,
                                                    @RequestParam SubscriptionState newState) {
        log.info("PATCH: /users/{}/subscribe/{}, state = {}", subscribedToId, subscriberId, newState);
        return userService.changeSubscriptionStatus(subscribedToId, subscriberId, newState);
    }

    @GetMapping("/{subscriberId}/follow/{subscribedToId}/events") // получить список событий пользователя, на которого
    // подписан
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsBySubscription(@PathVariable @Positive Long subscriberId,
                                                       @PathVariable @Positive Long subscribedToId,
                                                       @RequestParam (required = false, defaultValue = "0") @Min(0) Integer from,
                                                       @RequestParam (required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("GET: /users/{}/follow/{}/events, from = {}, size = {}", subscriberId, subscribedToId, from, size);
        return eventService.getEventsBySubscription(subscriberId, subscribedToId, from, size);
    }
}