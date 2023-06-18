import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatsServiceTest {
    @Mock
    StatsRepository statsRepository;
    @InjectMocks
    StatsService statsService;
    EndpointHitDto hitDto;
    EndpointHit hit;
    @Captor
    ArgumentCaptor<EndpointHit> hitCaptor;
    String start;
    String end;
    String[] uri;
    Boolean unique;
    List<ViewStats> listStats;
    @Captor
    ArgumentCaptor<LocalDateTime> dateCaptor;
    @Captor
    ArgumentCaptor<String[]> uriCaptor;

    @BeforeEach
    public void createEntitiesAndEnvironment() {
        this.hitDto = new EndpointHitDto("main-service", "/events", "111.111.111.111",
            LocalDateTime.of(2023, 1, 1, 1, 1, 1));
        this.hit = new EndpointHit();
        hit.setApp("main-service");
        hit.setUri("/events");
        hit.setIp("111.111.111.111");
        hit.setTimestamp(LocalDateTime.of(2023, 1, 1, 1, 1, 1));
        this.start = "2019-01-01 01:01:01";
        this.end = "2023-01-01 01:01:01";
        this.uri = new String[]{"/events", "/event/1"};
        this.unique = true;
        this.listStats = List.of(new ViewStats("main-service", "/events", 6L));
    }

    @Test
    public void shouldSaveHitStandardCase() {
        when(statsRepository.save(any())).thenReturn(hit);
        statsService.saveHit(hitDto);
        verify(statsRepository, times(1)).save(hitCaptor.capture());
        EndpointHit actual = hitCaptor.getValue();
        Assertions.assertEquals(hit, actual);
    }

    @Test
    public void shouldGetStatsIfUriIsNullAndUniqueIsTrueCase() {
        when(statsRepository.getStatsAllDistinct(any(), any())).thenReturn(listStats);
        statsService.getStats(start, end, null, unique);
        verify(statsRepository, times(1)).getStatsAllDistinct(dateCaptor.capture(),
            dateCaptor.capture());
        Assertions.assertEquals(dateCaptor.getAllValues().size(), 2);
        Assertions.assertEquals(dateCaptor.getAllValues().get(0), DateTimeParser.parseToDate(start));
        Assertions.assertEquals(dateCaptor.getAllValues().get(1), DateTimeParser.parseToDate(end));
        verify(statsRepository, never()).getStatsAll(any(), any());
        verify(statsRepository, never()).getStatsByUris(any(), any(), any());
        verify(statsRepository, never()).getStatsDistinctByUris(any(), any(), any());
    }

    @Test
    public void shouldGetStatsIfUriIsNullAndUniqueIsFalseCase() {
        unique = false;
        when(statsRepository.getStatsAll(any(), any())).thenReturn(listStats);
        statsService.getStats(start, end, null, unique);
        verify(statsRepository, times(1)).getStatsAll(dateCaptor.capture(),
            dateCaptor.capture());
        Assertions.assertEquals(dateCaptor.getAllValues().size(), 2);
        Assertions.assertEquals(dateCaptor.getAllValues().get(0), DateTimeParser.parseToDate(start));
        Assertions.assertEquals(dateCaptor.getAllValues().get(1), DateTimeParser.parseToDate(end));
        verify(statsRepository, never()).getStatsAllDistinct(any(), any());
        verify(statsRepository, never()).getStatsByUris(any(), any(), any());
        verify(statsRepository, never()).getStatsDistinctByUris(any(), any(), any());
    }

    @Test
    public void shouldGetStatsIfUriIsNotNullAndUniqueIsFalseCase() {
        unique = false;
        when(statsRepository.getStatsByUris(any(), any(), any())).thenReturn(listStats);
        statsService.getStats(start, end, uri, unique);
        verify(statsRepository, times(1)).getStatsByUris(dateCaptor.capture(),
            dateCaptor.capture(), uriCaptor.capture());
        Assertions.assertEquals(dateCaptor.getAllValues().size(), 2);
        Assertions.assertEquals(dateCaptor.getAllValues().get(0), DateTimeParser.parseToDate(start));
        Assertions.assertEquals(dateCaptor.getAllValues().get(1), DateTimeParser.parseToDate(end));
        Assertions.assertEquals(uriCaptor.getValue(), uri);
        verify(statsRepository, never()).getStatsAll(any(), any());
        verify(statsRepository, never()).getStatsAllDistinct(any(), any());
        verify(statsRepository, never()).getStatsDistinctByUris(any(), any(), any());
    }

    @Test
    public void shouldGetStatsIfUriIsNotNullAndUniqueIsTrueCase() {
        when(statsRepository.getStatsDistinctByUris(any(), any(), any())).thenReturn(listStats);
        statsService.getStats(start, end, uri, unique);
        verify(statsRepository, times(1)).getStatsDistinctByUris(dateCaptor.capture(),
            dateCaptor.capture(), uriCaptor.capture());
        Assertions.assertEquals(dateCaptor.getAllValues().size(), 2);
        Assertions.assertEquals(dateCaptor.getAllValues().get(0), DateTimeParser.parseToDate(start));
        Assertions.assertEquals(dateCaptor.getAllValues().get(1), DateTimeParser.parseToDate(end));
        Assertions.assertEquals(uriCaptor.getValue(), uri);
        verify(statsRepository, never()).getStatsAll(any(), any());
        verify(statsRepository, never()).getStatsAllDistinct(any(), any());
        verify(statsRepository, never()).getStatsByUris(any(), any(), any());
    }
}