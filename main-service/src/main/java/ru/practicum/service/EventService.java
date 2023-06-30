package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.*;
import ru.practicum.dto.input.NewEventDto;
import ru.practicum.dto.input.UpdateEventAdminRequest;
import ru.practicum.dto.input.UpdateEventRequestDto;
import ru.practicum.dto.input.UpdateEventUserRequest;
import ru.practicum.dto.output.EventFullDto;
import ru.practicum.dto.output.EventOutputDto;
import ru.practicum.dto.output.EventShortDto;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.entity.User;
import ru.practicum.exception.IllegalActionException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.repository.EventRepository;
import ru.practicum.state.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final StatsClient statsClient;
    private final Clock clock;

    public EventService(EventRepository eventRepository, CategoryService categoryService, UserService userService,
                        StatsClient statsClient,
                        Clock clock) {
        this.eventRepository = eventRepository;
        this.categoryService = categoryService;
        this.userService = userService;
        this.statsClient = statsClient;
        this.clock = clock;
    }

    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User userFromDb = userService.getUserIfExists(userId);
        Category categoryFromDb = categoryService.getCategoryIfExists(newEventDto.getCategory());
        LocalDateTime created = LocalDateTime.now(clock);
        Event newEvent = EventMapper.toEvent(newEventDto, created, userFromDb, categoryFromDb);
        newEvent.setState(EventState.PENDING);
        Event saved = eventRepository.save(newEvent);
        log.info("Event value = {} has been saved, id = {}", newEventDto, saved.getId());
        return EventMapper.toEventFullDto(saved);
    }

    public List<EventShortDto> getEventsPrivate(Long userId, Integer from, Integer size) {
        User userFromDb = userService.getUserIfExists(userId);
        Pageable pageRequest = PageRequestSpecifier.getPageRequestWithoutSort(from, size);
        List<Event> eventsFromDb = eventRepository.findAllByInitiator(userFromDb, pageRequest).getContent();
        List<EventShortDto> eventShortDtoList = EventMapper.toEventShortDtoList(eventsFromDb);
        addViews(eventShortDtoList);
        log.info("Get request for events by initiator = {} processed successfully", userId);
        return eventShortDtoList;
    }

    public EventFullDto getEventPrivate(Long userId, Long eventId) {
        userService.getUserIfExists(userId);
        Event eventFromDb = getEventIfExist(eventId);
        checkUserIsEventInitiator(userId, eventFromDb);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(eventFromDb);
        addViews(eventFullDto);
        log.info("Get request for event id = {} by initiator = {} processed successfully", eventId, userId);
        return eventFullDto;
    }

    public List<EventFullDto> getEventsAdmin(Long[] users, String[] states, Long[] categories, String rangeStart,
                                             String rangeEnd, Integer from, Integer size) {
        Pageable pageRequest = PageRequestSpecifier.getPageRequestWithoutSort(from, size);
        List<User> userList = users != null ? userService.getUsersById(users) : null;
        List<EventState> stateList = states != null ? Arrays.stream(states).map(EventState::getState)
            .collect(Collectors.toList()) : null;
        List<Category> categoryList = categories != null ? categoryService.getCategoriesById(categories) : null;
        LocalDateTime dateTimeStart = rangeStart != null ? DateTimeParser.parseToDate(rangeStart) : null;
        LocalDateTime dateTimeEnd = rangeEnd != null ? DateTimeParser.parseToDate(rangeEnd) : null;
        if (dateTimeStart != null && dateTimeEnd != null) {
            validateStartEndTime(dateTimeStart, dateTimeEnd);
        }
        List<Event> events = eventRepository.findAllAdminByFilter(userList, stateList, categoryList, dateTimeStart,
            dateTimeEnd, pageRequest).getContent();
        log.info("Get request for events by filters: userList = {}, stateList = {}, categoryList = {}, dataTimeStart " +
                "= {}, dataTimeEnd = {} processed successfully", userList, stateList, categoryList, dateTimeStart,
            dateTimeEnd);
        List<EventFullDto> eventFullDtoList = EventMapper.toEventFullDtoList(events);
        addViews(eventFullDtoList);
        return eventFullDtoList;
    }

    public EventFullDto getEventPublic(Long eventId, HttpServletRequest request) {
        Event eventFromDb = getEventIfExist(eventId);
        boolean isPublished = eventFromDb.getState().equals(EventState.PUBLISHED);
        if (!isPublished) {
            String message = "Event id = " + eventId + "is not published";
            log.error(message);
            throw new NotFoundException(message);
        }
        EventFullDto eventFullDto = EventMapper.toEventFullDto(eventFromDb);
        addViews(eventFullDto);
        saveStats(request);
        log.info("Get request for Event by id = {} processed successfully", eventId);
        return eventFullDto;
    }

    public List<EventShortDto> getEventsPublic(String text, Long[] categories, Boolean paid, String rangeStart,
                                               String rangeEnd, Boolean onlyAvailable, String
                                                   sort, Integer from,
                                               Integer size, HttpServletRequest request) {
        Pageable pageRequest = PageRequestSpecifier.getPageRequestWithoutSort(from, size);
        List<Category> categoryList = categories != null ? categoryService.getCategoriesById(categories) : null;
        LocalDateTime dateTimeStart = rangeStart != null ? DateTimeParser.parseToDate(rangeStart) : null;
        LocalDateTime dateTimeEnd = rangeEnd != null ? DateTimeParser.parseToDate(rangeEnd) : null;
        if (dateTimeStart != null && dateTimeEnd != null) {
            validateStartEndTime(dateTimeStart, dateTimeEnd);
        }
        List<Event> eventsUnsorted;
        if (dateTimeStart == null && dateTimeEnd == null) {
            LocalDateTime now = LocalDateTime.now(clock);
            eventsUnsorted = eventRepository.findAllPublicByFilter(text, categoryList, paid, onlyAvailable, now,
                pageRequest).getContent();
        } else {
            eventsUnsorted = eventRepository.findAllPublicByFilter(text, categoryList, paid, dateTimeStart,
                dateTimeEnd, onlyAvailable, pageRequest).getContent();
        }
        List<EventShortDto> eventsDtoUnsorted = EventMapper.toEventShortDtoList(eventsUnsorted);
        addViews(eventsDtoUnsorted);
        List<EventShortDto> eventsDtoSorted;
        if (sort == null) {
            return eventsDtoUnsorted;
        } else {
            switch (sort) {
                case "EVENT_DATE":
                    eventsDtoSorted = eventsDtoUnsorted.stream().sorted(
                        Comparator.comparing(EventShortDto::getEventDate)).collect(Collectors.toList());
                    break;
                case "VIEWS":
                    eventsDtoSorted = eventsDtoUnsorted.stream().sorted(
                        Comparator.comparing(EventShortDto::getViews,
                            Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
                    break;
                default:
                    String message = "Unavailable action: unable to get events. Reason: event sort = " + sort
                        + " is not supported";
                    throw new IllegalActionException(message);
            }
            saveStats(request);
            log.info("Get request for events by filters: text = {}, categories = {}, paid = {},  dataTimeStart " +
                    "= {}, dataTimeEnd = {}, onlyAvailable = {}, sort = {} processed successfully", text,
                categoryList, paid, dateTimeStart, dateTimeEnd, onlyAvailable, sort);
            return eventsDtoSorted;
        }
    }

    public EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        userService.getUserIfExists(userId);
        Event eventFromDb = getEventIfExist(eventId);
        checkUserIsEventInitiator(userId, eventFromDb);
        checkEventStateIsNotPublished(eventFromDb);
        Event updateEvent = fixUpdateChanges(eventFromDb, updateRequest);
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    updateEvent.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    updateEvent.setState(EventState.CANCELED);
                    break;
                default:
                    String message = "Unavailable action: unable to update event. Reason: update state action = "
                        + updateRequest.getStateAction() + " is not supported";
                    throw new IllegalActionException(message);
            }
        }
        Event saved = eventRepository.save(updateEvent);
        log.info("Event id = {} has been updated by initiator = {}", eventId, userId);
        return EventMapper.toEventFullDto(saved);
    }

    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event eventFromDb = getEventIfExist(eventId);
        Event updateEvent;
        if (updateRequest.getStateAction() == null) {
            checkEventStateIsNotPublished(eventFromDb);
            updateEvent = fixUpdateChanges(eventFromDb, updateRequest);
        } else {
            switch (updateRequest.getStateAction()) {
                case PUBLISH_EVENT:
                    boolean isPending = eventFromDb.getState().equals(EventState.PENDING);
                    if (!isPending) {
                        String message = "Unavailable action: can not update Event id = " + eventFromDb.getId() + ". " +
                            "Reason: events state is " + EventState.PENDING.name();
                        log.error(message);
                        throw new IllegalActionException(message);
                    }
                    LocalDateTime publishedOn = LocalDateTime.now(clock);
                    updateEvent = fixUpdateChanges(eventFromDb, updateRequest);
                    updateEvent.setState(EventState.PUBLISHED);
                    updateEvent.setPublishedOn(publishedOn);
                    break;
                case REJECT_EVENT:
                    checkEventStateIsNotPublished(eventFromDb);
                    updateEvent = fixUpdateChanges(eventFromDb, updateRequest);
                    updateEvent.setState(EventState.CANCELED);
                    break;
                default:
                    String message = "Unavailable action: unable to update event. Reason: update state action = "
                        + updateRequest.getStateAction() + " is not supported";
                    throw new IllegalActionException(message);
            }
        }
        Event saved = eventRepository.save(updateEvent);
        log.info("Event id = {} has been updated by admin", eventId);
        return EventMapper.toEventFullDto(saved);
    }

    public Event getEventIfExist(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isPresent()) {
            return event.get();
        } else {
            String message = "Event with id=" + eventId + " was not found";
            log.error(message);
            throw new NotFoundException(message);
        }
    }

    public void addViews(List<? extends EventOutputDto> eventOutputDtoList) {
        LocalDateTime end = LocalDateTime.now(clock);
        List<EventOutputDto> filteredAndSortedEvents = eventOutputDtoList.stream()
            .filter(event -> event.getState() == EventState.PUBLISHED)
            .sorted(Comparator.comparing(EventOutputDto::getPublishedOn))
            .collect(Collectors.toList());
        if (!filteredAndSortedEvents.isEmpty()) {
            int size = filteredAndSortedEvents.size();
            LocalDateTime start = filteredAndSortedEvents.get(0).getPublishedOn();
            String[] uris = new String[size];
            for (int i = 0; i < size; i++) {
                String uri = "/events/";
                uris[i] = uri + filteredAndSortedEvents.get(i).getId().toString();
            }
            Boolean unique = true;
            List<ViewStats> stats = statsClient.getStats(start, end, uris, unique);
            Map<Long, Long> idsViews = new HashMap<>();
            for (ViewStats viewStats : stats) {
                String uri = viewStats.getUri();
                String id = uri.substring(uri.lastIndexOf('/') + 1);
                idsViews.put(Long.parseLong(id), viewStats.getHits());
            }
            for (EventOutputDto eventOutputDto : eventOutputDtoList) {
                if (idsViews.containsKey(eventOutputDto.getId())) {
                    eventOutputDto.setViews(idsViews.get(eventOutputDto.getId()));
                }
            }
        }
    }

    private void saveStats(HttpServletRequest request) {
        EndpointHitDto hitDto = new EndpointHitDto();
        hitDto.setIp(request.getRemoteAddr());
        hitDto.setApp("main-service");
        hitDto.setUri(request.getRequestURI());
        LocalDateTime now = LocalDateTime.now(clock);
        hitDto.setTimestamp(now);
        statsClient.saveHit(hitDto);
    }

    private void addViews(EventFullDto eventFullDto) {
        if (eventFullDto.getState().equals(EventState.PUBLISHED)) {
            LocalDateTime start = eventFullDto.getPublishedOn();
            LocalDateTime end = eventFullDto.getEventDate();
            String[] uris = new String[1];
            uris[0] = "/events/" + eventFullDto.getId().toString();
            Boolean unique = true;
            List<ViewStats> stats = statsClient.getStats(start, end, uris, unique);
            if (stats != null && !stats.isEmpty()) {
                eventFullDto.setViews(stats.get(0).getHits());
            }
        }
    }

    private Event fixUpdateChanges(Event event, UpdateEventRequestDto updateEventUserRequest) {
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(categoryService.getCategoryIfExists(updateEventUserRequest.getCategory()));
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(updateEventUserRequest.getLocation());
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        return event;
    }

    private void checkUserIsEventInitiator(Long userId, Event event) {
        boolean isInitiator = event.getInitiator().getId().equals(userId);
        if (!isInitiator) {
            String message = "Unavailable action: User id = " + userId + " is not" +
                " Event id = " + event.getId() + "initiator";
            log.error(message);
            throw new IllegalActionException(message);
        }
    }

    private void checkEventStateIsNotPublished(Event event) {
        boolean isPublished = event.getState().equals(EventState.PUBLISHED);
        if (isPublished) {
            String message = "Unavailable action: can not update Event id = " + event.getId() + ". Reason: event has " +
                "been published";
            log.error(message);
            throw new IllegalActionException(message);
        }
    }

    private void validateStartEndTime(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            log.error("Start date must be before End date");
            throw new InvalidStartEndTimeException("Дата начала должна быть ранее даты окончания");
        }
    }

    public List<Event> getEventsByIds(List<Long> ids) {
        return eventRepository.findAllByIdIn(ids);
    }
}