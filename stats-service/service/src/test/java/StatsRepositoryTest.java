import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.EndpointHit;
import ru.practicum.ExploreWithMeStatsService;
import ru.practicum.StatsRepository;
import ru.practicum.ViewStats;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ContextConfiguration(classes = {ExploreWithMeStatsService.class})
@DataJpaTest
@AutoConfigureTestDatabase
public class StatsRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StatsRepository repository;
    private EndpointHit hit1;
    private EndpointHit hit2;
    private EndpointHit hit3;
    private EndpointHit hit4;

    @BeforeEach
    public void createEntitiesAndEnvironment() {
        this.hit1 = new EndpointHit(null, "main-service", "/events", "111.111.111.111",
            LocalDateTime.of(2023, 1, 1, 1, 1, 1));
        this.hit2 = new EndpointHit(null, "main-service", "/events", "111.111.111.0",
            LocalDateTime.of(2024, 1, 1, 1, 1, 2));
        this.hit3 = new EndpointHit(null, "main-service", "/events", "111.111.111.111",
            LocalDateTime.of(2023, 1, 10, 1, 1, 1));
        this.hit4 = new EndpointHit(null, "main-service", "/event/1", "111.111.111.111",
            LocalDateTime.of(2023, 1, 5, 1, 1, 1));
    }

    @Test
    public void shouldGetStatsDistinctByUrisAllInDatePeriodCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);

        List<ViewStats> expected = List.of(new ViewStats("main-service", "/events", 2L),
            new ViewStats("main-service", "/event/1", 1L));
        List<ViewStats> actual = repository.getStatsDistinctByUris(
            LocalDateTime.of(2000, 1, 1, 1, 11),
            LocalDateTime.of(2050, 1, 1, 1, 11),
            new String[]{"/events", "/event/1"});
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStatsAllDistinctAllInDatePeriodCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);
        List<ViewStats> expected = List.of(new ViewStats("main-service", "/events", 2L),
            new ViewStats("main-service", "/event/1", 1L));
        List<ViewStats> actual = repository.getStatsAllDistinct(
            LocalDateTime.of(2000, 1, 1, 1, 11),
            LocalDateTime.of(2050, 1, 1, 1, 11));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStatsAlLAllInDatePeriodCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);
        List<ViewStats> expected = List.of(new ViewStats("main-service", "/events", 3L),
            new ViewStats("main-service", "/event/1", 1L));
        List<ViewStats> actual = repository.getStatsAll(
            LocalDateTime.of(2000, 1, 1, 1, 11),
            LocalDateTime.of(2050, 1, 1, 1, 11));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStatsByUrisAllInDatePeriodCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);
        List<ViewStats> expected = List.of(new ViewStats("main-service", "/events", 3L),
            new ViewStats("main-service", "/event/1", 1L));
        List<ViewStats> actual = repository.getStatsByUris(
            LocalDateTime.of(2000, 1, 1, 1, 11),
            LocalDateTime.of(2050, 1, 1, 1, 11),
            new String[]{"/events", "/event/1"});
        Assertions.assertEquals(expected, actual);
    }


    @Test
    public void shouldGetStatsDistinctByUrisNoInDatePeriodCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);

        List<ViewStats> expected = List.of();
        List<ViewStats> actual = repository.getStatsDistinctByUris(
            LocalDateTime.of(2045, 1, 1, 1, 11, 1),
            LocalDateTime.of(2050, 1, 1, 1, 11, 1),
            new String[]{"/events", "/event/1"});
        Assertions.assertEquals(0, actual.size());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStatsAllDistinctNoInDatePeriodCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);

        List<ViewStats> expected = List.of();
        List<ViewStats> actual = repository.getStatsAllDistinct(
            LocalDateTime.of(2045, 1, 1, 1, 11, 1),
            LocalDateTime.of(2050, 1, 1, 1, 11, 1));
        Assertions.assertEquals(0, actual.size());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStatsAllNoInDatePeriodCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);

        List<ViewStats> expected = List.of();
        List<ViewStats> actual = repository.getStatsAllDistinct(
            LocalDateTime.of(2045, 1, 1, 1, 11, 1),
            LocalDateTime.of(2050, 1, 1, 1, 11, 1));
        Assertions.assertEquals(0, actual.size());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStatsByUrisNoInDatePeriodCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);

        List<ViewStats> expected = List.of();
        List<ViewStats> actual = repository.getStatsByUris(
            LocalDateTime.of(2045, 1, 1, 1, 11, 1),
            LocalDateTime.of(2050, 1, 1, 1, 11, 1),
            new String[]{"/events", "/event/1"});
        Assertions.assertEquals(0, actual.size());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStatsDistinctByUrisOneInDatePeriodCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);

        List<ViewStats> expected = List.of(new ViewStats("main-service", "/events", 1L),
            new ViewStats("main-service", "/event/1", 1L));
        List<ViewStats> actual = repository.getStatsDistinctByUris(
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2023, 2, 1, 1, 11, 1),
            new String[]{"/events", "/event/1"});

        Set<ViewStats> expectedSet = new HashSet<>(expected);
        Set<ViewStats> actualSet = new HashSet<>(actual);

        Assertions.assertEquals(expectedSet, actualSet);
    }

    @Test
    public void shouldGetStatsAllDistinctOneInDatePeriodCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);

        List<ViewStats> expected = List.of(new ViewStats("main-service", "/events", 1L),
            new ViewStats("main-service", "/event/1", 1L));
        List<ViewStats> actual = repository.getStatsAllDistinct(
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2023, 2, 1, 1, 11, 1));

        Set<ViewStats> expectedSet = new HashSet<>(expected);
        Set<ViewStats> actualSet = new HashSet<>(actual);

        Assertions.assertEquals(expectedSet, actualSet);
    }

    @Test
    public void shouldGetStatsAllOneInDatePeriodCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);

        List<ViewStats> expected = List.of(new ViewStats("main-service", "/events", 2L),
            new ViewStats("main-service", "/event/1", 1L));
        List<ViewStats> actual = repository.getStatsAll(
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2023, 2, 1, 1, 11, 1));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStatsByUriOneInDatePeriodCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);

        List<ViewStats> expected = List.of(new ViewStats("main-service", "/events", 2L),
            new ViewStats("main-service", "/event/1", 1L));
        List<ViewStats> actual = repository.getStatsByUris(
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2023, 2, 1, 1, 11, 1),
            new String[]{"/events", "/event/1"});
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStatsDistinctByUrisDatesBoundaryValuesCase() {
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);

        List<ViewStats> expected = List.of(new ViewStats("main-service", "/events", 2L));
        List<ViewStats> actual = repository.getStatsDistinctByUris(
            LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2024, 1, 1, 1, 1, 2),
            new String[]{"/events", "/event1"});
        Assertions.assertEquals(1, actual.size());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStatsByUrisIfNotFound() {
        entityManager.persist(hit1);
        List<ViewStats> expected = List.of();
        List<ViewStats> actual = repository.getStatsByUris(LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2024, 1, 1, 1, 1, 2),
            new String[]{"/hit", "/hits2"});
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStatsDistinctByUrisIfNotFound() {
        entityManager.persist(hit1);
        List<ViewStats> expected = List.of();
        List<ViewStats> actual = repository.getStatsDistinctByUris(LocalDateTime.of(2023, 1, 1, 1, 1, 1),
            LocalDateTime.of(2024, 1, 1, 1, 1, 2),
            new String[]{"/hit", "/hits2"});
        Assertions.assertEquals(expected, actual);
    }


}
