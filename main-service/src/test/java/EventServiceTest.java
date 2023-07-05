import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.practicum.StatsClient;
import ru.practicum.ViewStats;
import ru.practicum.dto.input.UpdateEventAdminRequest;
import ru.practicum.dto.input.UpdateEventUserRequest;
import ru.practicum.dto.output.EventFullDto;
import ru.practicum.dto.output.EventShortDto;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.entity.Location;
import ru.practicum.entity.User;
import ru.practicum.exception.IllegalActionException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.EventService;
import ru.practicum.service.UserService;
import ru.practicum.state.EventState;
import ru.practicum.state.StateActionAdmin;
import ru.practicum.state.StateActionPrivate;
import ru.practicum.state.UserProfileState;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @InjectMocks
    EventService eventService;
    @Mock
    EventRepository eventRepository;
    @Mock
    StatsClient statsClient;
    @Mock
    UserService userService;
    @Mock
    HttpServletRequest servletRequest;
    @Mock
    Clock clock;

    User user1;
    Event event1;
    Event event2;
    Event event3;


    @BeforeEach
    public void createEntitiesAndEnvironment() {
        this.user1 = new User(1L, "testUser1", "testUser1@mail.ru", UserProfileState.PUBLIC);
        this.event1 = new Event(1L, "annotation1ForTestLengthMoreThan20",
            new Category(1L, "testCategory1"), LocalDateTime.now(), "description1ForTestLengthMore" +
            "Than20", LocalDateTime.of(2024, 1, 1, 1, 1, 1), user1,
            new Location(), true, 0,
            LocalDateTime.of(2023, 1, 1, 1, 1, 1), true,
            EventState.PUBLISHED, "title1", new ArrayList<>());
        this.event2 = new Event(2L, "annotation2ForTestLengthMoreThan20",
            new Category(2L, "testCategory2"), LocalDateTime.now(), "description2ForTestLengthMore" +
            "Than20", LocalDateTime.of(2024, 2, 2, 2, 2, 2), user1,
            new Location(), false, 10, null, true, EventState.PENDING,
            "title2", new ArrayList<>());
        this.event3 = new Event(3L, "annotation3ForTestLengthMoreThan20",
            new Category(3L, "testCategory3"), LocalDateTime.now(), "description1ForTestLengthMore" +
            "Than20", LocalDateTime.of(2024, 3, 3, 3, 3, 3), user1,
            new Location(), true, 20,
            LocalDateTime.of(2023, 1, 1, 1, 1, 1), true,
            EventState.PUBLISHED, "title1", new ArrayList<>());
    }

    @Test
    public void shouldGetEventPrivateStandardCase() {
        when(userService.getUserIfExists(anyLong())).thenReturn(user1);
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event1));
        when(statsClient.getStats(any(), any(), any(), any())).thenReturn(List.of(new ViewStats("test",
            "/events/1", 100L)));

        EventFullDto actual = eventService.getEventPrivate(1L, 1L);
        Assertions.assertEquals(100L, actual.getViews());
    }

    @Test
    public void shouldGetEventPublicStandardCase() {
        when(servletRequest.getRemoteAddr()).thenReturn("111.111.111.111");
        when(servletRequest.getRequestURI()).thenReturn("/events/1");

        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        Mockito.when(clock.instant()).thenReturn(date.toInstant(ZoneOffset.UTC));
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event1));
        when(statsClient.getStats(any(), any(), any(), any())).thenReturn(List.of(new ViewStats("test",
            "/events/1", 100L)));

        EventFullDto actual = eventService.getEventPublic(1L, servletRequest);
        Assertions.assertEquals(100L, actual.getViews());
    }

    @Test
    public void shouldGetEventsAdminStandardCase() {
        List<Event> eventsList = List.of(event1, event2, event3);
        when(eventRepository.findAllAdminByFilter(any(), any(), any(), any(), any(), any())).thenReturn(
            new PageImpl<>(eventsList));
        List<ViewStats> statsList = List.of(new ViewStats("main-server", "events/3", 100L));
        when(statsClient.getStats(any(), any(), any(), any())).thenReturn(statsList);

        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        Mockito.when(clock.instant()).thenReturn(date.toInstant(ZoneOffset.UTC));
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        List<EventFullDto> actual = eventService.getEventsAdmin(null, null, null, null,
            null, 0, 10);

        Assertions.assertEquals(3, actual.size());
        Assertions.assertEquals(actual.get(0), EventMapper.toEventFullDto(event1));
        Assertions.assertEquals(actual.get(1), EventMapper.toEventFullDto(event2));
        Assertions.assertEquals(100L, actual.get(2).getViews());
    }

    @Test
    public void shouldGetEventsPublicEventDateSortCase() {
        List<Event> eventsList = List.of(event1, event3);
        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        List<ViewStats> statsList = List.of(new ViewStats("main-server", "events/3", 100L));
        when(statsClient.getStats(any(), any(), any(), any())).thenReturn(statsList);
        Mockito.when(clock.instant()).thenReturn(date.toInstant(ZoneOffset.UTC));
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        when(eventRepository.findAllPublicByFilter(any(), any(), any(), any(), any(), any())).thenReturn(
            new PageImpl<>(eventsList));
        when(statsClient.getStats(any(), any(), any(), any())).thenReturn(statsList);
        List<EventShortDto> actual = eventService.getEventsPublic(null, null, null, null,
            null, false, "EVENT_DATE", 0, 10, servletRequest);
        System.out.println(actual);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(actual.get(0), EventMapper.toEventShortDto(event1));
        Assertions.assertEquals(100L, actual.get(1).getViews());
    }

    @Test
    public void shouldGetEventsPublicViewsSortCase() {
        List<Event> eventsList = List.of(event1, event3);
        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        List<ViewStats> statsList = List.of(new ViewStats("main-server", "events/3", 100L));
        when(statsClient.getStats(any(), any(), any(), any())).thenReturn(statsList);
        Mockito.when(clock.instant()).thenReturn(date.toInstant(ZoneOffset.UTC));
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        when(eventRepository.findAllPublicByFilter(any(), any(), any(), any(), any(), any())).thenReturn(
            new PageImpl<>(eventsList));
        when(statsClient.getStats(any(), any(), any(), any())).thenReturn(statsList);
        List<EventShortDto> actual = eventService.getEventsPublic(null, null, null, null,
            null, false, "VIEWS", 0, 10, servletRequest);

        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(100L, actual.get(0).getViews());
        Assertions.assertEquals(actual.get(1), EventMapper.toEventShortDto(event1));
    }

    @Test
    public void shouldGetEventsPublicSortNotSupported() {
        List<Event> eventsList = List.of(event1, event3);
        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        List<ViewStats> statsList = List.of(new ViewStats("main-server", "events/3", 100L));
        when(statsClient.getStats(any(), any(), any(), any())).thenReturn(statsList);
        Mockito.when(clock.instant()).thenReturn(date.toInstant(ZoneOffset.UTC));
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        when(eventRepository.findAllPublicByFilter(any(), any(), any(), any(), any(), any())).thenReturn(
            new PageImpl<>(eventsList));
        when(statsClient.getStats(any(), any(), any(), any())).thenReturn(statsList);
        Assertions.assertThrows(IllegalActionException.class, () -> eventService.getEventsPublic(null,
            null, null, null, null, false, "NOT_SUPPORTED", 0,
            10, servletRequest));
    }

    @Test
    public void shouldGetEventIfExistEventNotFoundCase() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> eventService.getEventIfExist(1L));
    }

    @Test
    public void shouldUpdateEventAdminPublishEventCase() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event2));

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(StateActionAdmin.PUBLISH_EVENT);
        when(eventRepository.save(any())).thenReturn(event2);

        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        Mockito.when(clock.instant()).thenReturn(date.toInstant(ZoneOffset.UTC));
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        EventFullDto actual = eventService.updateEventAdmin(2L, request);
        Assertions.assertEquals(EventState.PUBLISHED, actual.getState());
    }

    @Test
    public void shouldUpdateEventAdminRejectEventCase() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event2));

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(StateActionAdmin.REJECT_EVENT);
        when(eventRepository.save(any())).thenReturn(event2);

        EventFullDto actual = eventService.updateEventAdmin(2L, request);
        Assertions.assertEquals(EventState.CANCELED, actual.getState());
    }

    @Test
    public void shouldUpdateEventUserSendToReviewCase() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event2));

        UpdateEventUserRequest request = new UpdateEventUserRequest();
        request.setStateAction(StateActionPrivate.SEND_TO_REVIEW);
        when(eventRepository.save(any())).thenReturn(event2);

        EventFullDto actual = eventService.updateEventUser(1L, 2L, request);
        Assertions.assertEquals(EventState.PENDING, actual.getState());
    }

    @Test
    public void shouldUpdateEventUserCancelReviewCase() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event2));

        UpdateEventUserRequest request = new UpdateEventUserRequest();
        request.setStateAction(StateActionPrivate.CANCEL_REVIEW);
        when(eventRepository.save(any())).thenReturn(event2);

        EventFullDto actual = eventService.updateEventUser(1L, 2L, request);
        Assertions.assertEquals(EventState.CANCELED, actual.getState());
    }
}