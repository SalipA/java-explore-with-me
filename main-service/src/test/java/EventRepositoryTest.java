import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.ExploreWithMeMainService;
import ru.practicum.entity.*;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.PageRequestSpecifier;
import ru.practicum.state.EventState;
import ru.practicum.state.RequestState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ContextConfiguration(classes = {ExploreWithMeMainService.class})
@DataJpaTest
@AutoConfigureTestDatabase
public class EventRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository repository;
    private User user1;
    private User user2;
    private User user3;
    private Category category1;
    private Category category2;
    private Category category3;
    private Event event1;
    private Event event2;
    private Event event3;
    private Event event4;
    private Pageable pageRequest;


    @BeforeEach
    public void createEntitiesAndEnvironment() {
        this.pageRequest = PageRequestSpecifier.getPageRequestWithoutSort(0, 10);
        this.user1 = new User(null, "testUser1", "testUser1@mail.ru");
        this.user2 = new User(null, "testUser2", "testUser2@mail.ru");
        this.user3 = new User(null, "testUser3", "testUser3@mail.ru");
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        this.category1 = new Category(null, "testCategory1");
        this.category2 = new Category(null, "testCategory2");
        this.category3 = new Category(null, "testCategory3");
        entityManager.persist(category1);
        entityManager.persist(category2);
        entityManager.persist(category3);
        this.event1 = new Event(null, "annotation1ForTestLengthMoreThan20tion4",category1,
            LocalDateTime.now(), "description1ForTestLengthMoreThan20",
            LocalDateTime.of(2024, 1, 1, 1, 1, 1), user1, new Location(),
            true, 0, LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            true, EventState.PUBLISHED, "title1", new ArrayList<>());
        this.event2 = new Event(null, "annotation2ForTestLengthMoreThan20", category2, LocalDateTime.now(),
            "description2ForTestLengthMoreThan20",
            LocalDateTime.of(2024, 2, 2, 2, 2, 2), user2, new Location(),
            false, 10, null, true, EventState.PENDING, "title2",
            new ArrayList<>());
        this.event3 = new Event(null, "annotation3ForTestLengthMoreThan20", category3, LocalDateTime.now(),
            "description3ForTestLengthMoreThan20",
            LocalDateTime.of(2024, 3, 3, 3, 3, 3), user3, new Location(),
            false, 2,
            LocalDateTime.of(2023, 3, 3, 3, 3, 3), false,
            EventState.PUBLISHED, "title3", new ArrayList<>());
        this.event4 = new Event(null, "annotation4ForTestLengthMoreThan20", category2, LocalDateTime.now(),
            "description4ForTestLengthMoreThan20",
            LocalDateTime.of(2024, 4, 4, 4, 4, 4), user2, new Location(),
            false, 1,
            LocalDateTime.of(2023, 4, 4, 4, 4, 4), true,
            EventState.PUBLISHED, "title4", new ArrayList<>());
        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.persist(event3);
        entityManager.persist(event4);
    }

    @Test
    public void shouldFindAllAdminByFilterNullAllCase() {
        List<Event> actual = repository.findAllAdminByFilter(null, null, null, null,
            null, pageRequest).getContent();
        List<Event> expected = List.of(event1, event2, event3, event4);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllAdminByFilterOneUserCase() {
        List<Event> actual = repository.findAllAdminByFilter(List.of(user2), null, null, null,
            null, pageRequest).getContent();
        List<Event> expected = List.of(event2, event4);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllAdminByFilterSomeUsersCase() {
        List<Event> actual = repository.findAllAdminByFilter(List.of(user2, user3), null, null,
            null, null, pageRequest).getContent();
        List<Event> expected = List.of(event2, event3, event4);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllAdminByFilterOneStateCase() {
        List<Event> actual = repository.findAllAdminByFilter(null, List.of(EventState.PENDING), null,
            null, null, pageRequest).getContent();
        List<Event> expected = List.of(event2);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllAdminByFilterSomeStatesCase() {
        List<Event> actual = repository.findAllAdminByFilter(null, List.of(EventState.PUBLISHED,
            EventState.CANCELED), null, null, null, pageRequest).getContent();
        List<Event> expected = List.of(event1, event3, event4);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllAdminByFilterOneCategoryCase() {
        List<Event> actual = repository.findAllAdminByFilter(null, null, List.of(category1), null,
            null, pageRequest).getContent();
        List<Event> expected = List.of(event1);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllAdminByFilterSomeCategoryCase() {
        List<Event> actual = repository.findAllAdminByFilter(null, null, List.of(category1, category3),
            null, null, pageRequest).getContent();
        List<Event> expected = List.of(event1, event3);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllAdminByFilterRangeStartNotNullRangeEndNullCase() {
        List<Event> actual = repository.findAllAdminByFilter(null, null, null,
            LocalDateTime.of(2024, 3, 3, 3, 3, 3), null,
            pageRequest).getContent();
        List<Event> expected = List.of(event3, event4);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllAdminByFilterRangeStartNullRangeEndNotNullCase() {
        List<Event> actual = repository.findAllAdminByFilter(null, null, null, null,
            LocalDateTime.of(2024, 3, 3, 3, 3, 3), pageRequest).getContent();
        List<Event> expected = List.of(event1, event2, event3);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllAdminByFilterRangeStartIsEqualsRangeEndCase() {
        List<Event> actual = repository.findAllAdminByFilter(null, null, null,
            LocalDateTime.of(2024, 3, 3, 3, 3, 3),
            LocalDateTime.of(2024, 3, 3, 3, 3, 3), pageRequest).getContent();
        List<Event> expected = List.of(event3);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllAdminByFilterSpotSettingForEvent4Case() {
        List<Event> actual = repository.findAllAdminByFilter(List.of(user2), List.of(EventState.PUBLISHED),
            List.of(category2), LocalDateTime.of(2024, 1, 1, 1, 1, 1),
            LocalDateTime.of(2024, 12, 12, 12, 12, 12),
            pageRequest).getContent();
        List<Event> expected = List.of(event4);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllAdminByFilterEventsNotFoundCase() {
        List<Event> actual = repository.findAllAdminByFilter(List.of(user1), List.of(EventState.CANCELED),
            List.of(category2), LocalDateTime.of(2024, 1, 1, 1, 1, 1),
            LocalDateTime.of(2024, 12, 12, 12, 12, 12),
            pageRequest).getContent();
        List<Event> expected = List.of();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllPublicByFilterAllNullOreDefaultCase() {
        List<Event> actual = repository.findAllPublicByFilter(null, null, null,
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2030, 1, 1, 1, 1, 1), false,
            pageRequest).getContent();
        List<Event> expected = List.of(event1, event3, event4);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllPublicByFilterOnlyTextSearchCase() {
        List<Event> actual = repository.findAllPublicByFilter("TioN4", null, null,
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2030, 1, 1, 1, 1, 1), false,
            pageRequest).getContent();
        List<Event> excepted = List.of(event1, event4);
        Assertions.assertEquals(excepted, actual);
    }

    @Test
    public void shouldFindAllPublicByFilterOneCategoryCase() {
        List<Event> actual = repository.findAllPublicByFilter(null, List.of(category1), null,
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2030, 1, 1, 1, 1, 1), false,
            pageRequest).getContent();
        List<Event> expected = List.of(event1);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllPublicByFilterSomeCategoriesCase() {
        List<Event> actual = repository.findAllPublicByFilter(null, List.of(category1, category3), null,
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2030, 1, 1, 1, 1, 1), false,
            pageRequest).getContent();
        List<Event> expected = List.of(event1, event3);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllPublicByFilterPaidTrueCase() {
        List<Event> actual = repository.findAllPublicByFilter(null, null, true,
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2030, 1, 1, 1, 1, 1), false,
            pageRequest).getContent();
        List<Event> expected = List.of(event1);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllPublicByFilterPaidFalseCase() {
        List<Event> actual = repository.findAllPublicByFilter(null, null, false,
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2030, 1, 1, 1, 1, 1), false,
            pageRequest).getContent();
        List<Event> expected = List.of(event3, event4);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllPublicByFilterRangeStartAndRangeEndNullCase() {
        LocalDateTime nowFilter = LocalDateTime.of(2024, 2, 2, 2, 2, 2);
        List<Event> actual = repository.findAllPublicByFilter(null, null, null, false,
            nowFilter, pageRequest).getContent();
        List<Event> expected = List.of(event3, event4);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllPublicByFilterRangeStartNotNullAndRangeEndNullCase() {
        List<Event> actual = repository.findAllPublicByFilter(null, null, null,
            LocalDateTime.of(2024, 3, 3, 3, 3, 4), null, false, pageRequest).getContent();
        List<Event> expected = List.of(event4);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllPublicByFilterRangeStartNullAndRangeEndNotNullCase() {
        List<Event> actual = repository.findAllPublicByFilter(null, null, null, null,
            LocalDateTime.of(2024, 3, 3, 3, 3, 4), false,
            pageRequest).getContent();
        List<Event> expected = List.of(event1, event3);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllPublicByFilterOnlyAvailableCase() {
        Request request = RequestMapper.toRequest(user3,event4,LocalDateTime.now(),RequestState.CONFIRMED);
        entityManager.persist(request);
        List<Event> actual = repository.findAllPublicByFilter(null, null, null,
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2030, 1, 1, 1, 1, 1), true,
            pageRequest).getContent();
        List<Event> expected = List.of(event1, event3);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAllPublicByFilterOnlyAvailableRequestNotConfirmedCase() {
        Request request = new Request();
        request.setRequester(user3);
        request.setEvent(event4);
        request.setCreated(LocalDateTime.now());
        request.setState(RequestState.PENDING);
        entityManager.persist(request);
        List<Event> actual = repository.findAllPublicByFilter(null, null, null,
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2030, 1, 1, 1, 1, 1), true,
            pageRequest).getContent();
        List<Event> expected = List.of(event1, event3, event4);
        Assertions.assertEquals(expected, actual);
    }
}