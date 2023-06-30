package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.state.EventState;
import ru.practicum.exception.IllegalActionException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.state.RequestState;
import ru.practicum.dto.input.EventRequestStatusUpdateRequest;
import ru.practicum.dto.output.EventRequestStatusUpdateResult;
import ru.practicum.dto.output.ParticipationRequestDto;
import ru.practicum.entity.Event;
import ru.practicum.entity.Request;
import ru.practicum.entity.User;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.repository.RequestRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EventService eventService;

    private final Clock clock;

    public RequestService(RequestRepository requestRepository, UserService userService, EventService
        eventService, Clock clock) {
        this.requestRepository = requestRepository;
        this.userService = userService;
        this.eventService = eventService;
        this.clock = clock;
    }

    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User user = userService.getUserIfExists(userId);
        Event event = eventService.getEventIfExist(eventId);
        RequestState state;
        if (isUserEventInitiator(user, event)) {
            String message = "Unavailable action: unable to create request. Reason: User id = " + user.getId() + " is" +
                " Event id = " + event.getId() + "initiator";
            log.error(message);
            throw new IllegalActionException(message);
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            String message = "Unavailable action: unable to create request. Reason: Event id = " + event.getId() + " " +
                "is not published";
            log.error(message);
            throw new IllegalActionException(message);
        }
        if (isEventNeedConfirmationOfRequests(event)) {
            state = RequestState.PENDING;
        } else {
            state = RequestState.CONFIRMED;
        }
        checkEventsParticipantLimitIsNotReached(event);
        LocalDateTime created = LocalDateTime.now(clock);
        Request request = RequestMapper.toRequest(user, event, created, state);
        Request saved = requestRepository.save(request);
        log.info("Request value = {} has been saved, id = {}", request, saved.getId());
        return RequestMapper.toParticipationRequestDto(saved);
    }

    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userService.getUserIfExists(userId);
        Request request = getRequestIfExist(requestId);
        boolean isRequester = request.getRequester().getId().equals(userId);
        if (!isRequester) {
            String message = "Unavailable action: unable to cancel request. Reason: User id = " + userId + " is not" +
                " Request id = " + request.getId() + "initiator";
            log.error(message);
            throw new IllegalActionException(message);
        } else {
            request.setState(RequestState.CANCELED);
            Request canceled = requestRepository.save(request);
            log.info("Request id = {} has been canceled by requests initiator", requestId);
            return RequestMapper.toParticipationRequestDto(canceled);
        }
    }

    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        User user = userService.getUserIfExists(userId);
        Event event = eventService.getEventIfExist(eventId);
        if (isUserEventInitiator(user, event)) {
            List<Request> requests = requestRepository.findAllByEvent(event);
            log.info("Get request for event participants list by eventId = {} processed successfully", eventId);
            return RequestMapper.toParticipationRequestDtoList(requests);
        } else {
            String message =
                "Unavailable action: unable to get event participants. Reason: User id = " + user.getId() +
                    " is not Event id = " + event.getId() + "initiator";
            log.error(message);
            throw new IllegalActionException(message);
        }
    }

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        User user = userService.getUserIfExists(userId);
        List<Request> requests = requestRepository.findAllByRequester(user);
        log.info("Get request for event participant requests list by requester = {} processed successfully", userId);
        return RequestMapper.toParticipationRequestDtoList(requests);
    }

    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        User user = userService.getUserIfExists(userId);
        Event event = eventService.getEventIfExist(eventId);
        if (!isUserEventInitiator(user, event)) {
            String message =
                "Unavailable action: unable to change requests status. Reason: User id = " + user.getId() +
                    " is not Event id = " + event.getId() + "initiator";
            log.error(message);
            throw new IllegalActionException(message);
        }
        if (!isEventNeedConfirmationOfRequests(event)) {
            String message =
                "Unavailable action: unable to change requests status. Reason: Event id = " + event.getId() + " " +
                    "is not need confirmation of requests";
            log.error(message);
            throw new IllegalActionException(message);
        }
        checkRequestsStatusesArePending(updateRequest.getRequestIds());
        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequest = new ArrayList<>();
        switch (updateRequest.getStatus()) {
            case CONFIRMED:
                checkEventsParticipantLimitIsNotReached(updateRequest, event);
                requestRepository.updateRequestStateByIds(updateRequest.getRequestIds(), RequestState.CONFIRMED);
                confirmedRequests = requestRepository.findAllByIdInAndState(updateRequest.getRequestIds(),
                    RequestState.CONFIRMED);
                try {
                    checkEventsParticipantLimitIsNotReached(event);
                } catch (IllegalActionException exp) {
                    List<Long> needToReject =
                        requestRepository.findAllByState(RequestState.PENDING).stream().map(Request::getId).collect(Collectors.toList());
                    requestRepository.rejectRequests(RequestState.REJECTED, RequestState.PENDING);
                    rejectedRequest = requestRepository.findAllByIdInAndState(needToReject, RequestState.REJECTED);
                }
                break;
            case REJECTED:
                requestRepository.updateRequestStateByIds(updateRequest.getRequestIds(), RequestState.REJECTED);
                rejectedRequest = requestRepository.findAllByIdInAndState(updateRequest.getRequestIds(),
                    RequestState.REJECTED);
                break;
            default:
                String message =
                    "Unavailable action: unable to change requests status. Reason: request status = " +
                        updateRequest.getStatus().name() + " is not supported";
                throw new IllegalActionException(message);
        }
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(RequestMapper.toParticipationRequestDtoList(confirmedRequests));
        result.setRejectedRequests(RequestMapper.toParticipationRequestDtoList(rejectedRequest));
        log.info("Request status for Event id = {} participant requests have been changed successfully by event " +
            "initiator", eventId);
        return result;
    }

    private Request getRequestIfExist(Long requestId) {
        Optional<Request> request = requestRepository.findById(requestId);
        if (request.isPresent()) {
            return request.get();
        } else {
            String message = "Request with id=" + requestId + " was not found";
            log.error(message);
            throw new NotFoundException(message);
        }
    }

    private boolean isUserEventInitiator(User user, Event event) {
        return event.getInitiator().equals(user);
    }

    private void checkEventsParticipantLimitIsNotReached(Event event) {
        int participantLimit = event.getParticipantLimit();
        if (participantLimit != 0) {
            int minLimit = 1;
            int confirmedRequests = event.getRequests().size();
            boolean isLimitReached = (participantLimit - confirmedRequests) < minLimit;
            if (isLimitReached) {
                String message = "Unavailable action: unable to create request. Reason: Event id = " + event.getId() + " " +
                    "participant limit was reached";
                log.error(message);
                throw new IllegalActionException(message);
            }
        }
    }

    private void checkEventsParticipantLimitIsNotReached(EventRequestStatusUpdateRequest updateRequest, Event event) {
        int participantLimit = event.getParticipantLimit();
        if (participantLimit != 0) {
            int maxLimit = participantLimit - event.getRequests().size();
            boolean isLimitReached = updateRequest.getRequestIds().size() > maxLimit;
            if (isLimitReached) {
                String message =
                    "Unavailable action: unable to confirmed request. Reason: Event id = " + event.getId() + " " +
                        "participant limit was reached";
                log.error(message);
                throw new IllegalActionException(message);
            }
        }
    }

    private void checkRequestsStatusesArePending(List<Long> requestIds) {
        List<Request> requestsFromDb = requestRepository.findAllByIdInAndState(requestIds, RequestState.PENDING);
        boolean arePending = requestIds.size() == requestsFromDb.size();
        if (!arePending) {
            String message = "Unavailable action: unable to confirmed request. Reason: not all requests are PENDING";
            log.error(message);
            throw new IllegalActionException(message);
        }
    }

    private boolean isEventNeedConfirmationOfRequests(Event event) {
        int participantLimit = event.getParticipantLimit();
        if (participantLimit == 0) {
            return false;
        } else {
            return event.getRequestModeration();
        }
    }
}