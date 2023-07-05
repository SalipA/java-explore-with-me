import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import ru.practicum.*;

import javax.validation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {ExploreWithMeMainService.class})
@SpringBootTest(classes = StatsClient.class)
@ExtendWith(MockitoExtension.class)
@EnableAutoConfiguration
public class StatsClientTest {
    @Autowired
    StatsClient statsClient;

    @Mock
    RestTemplate restTemplateMock;

    EndpointHitDto hitDto;
    LocalDateTime start;
    LocalDateTime end;
    String[] uri;
    Boolean unique;
    @Captor
    ArgumentCaptor<String> urlCaptor;
    @Captor
    ArgumentCaptor<Map<String, Object>> mapCaptor;

    @BeforeEach
    public void setRestTemplateMock() {
        statsClient.setRest(restTemplateMock);
    }

    @BeforeEach
    public void createEntitiesAndEnvironment() {
        this.hitDto = new EndpointHitDto("main-service", "/events", "111.111.111.111",
            LocalDateTime.of(2023, 1, 1, 1, 1, 1));
        this.start = LocalDateTime.of(2022, 1, 1, 1, 1, 1);
        this.end = LocalDateTime.of(2023, 1, 1, 1, 1, 1);
        this.uri = new String[]{"/events", "/events/1"};
        this.unique = true;
    }

    @Test
    public void shouldSaveHitIfEndpointHitDtoNullCase() {
        ConstraintViolationException exp = Assertions.assertThrows(ConstraintViolationException.class,
            () -> statsClient.saveHit(null));
        Assertions.assertEquals("saveHit.hitDto: не должно быть null", exp.getMessage());
        verify(restTemplateMock, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Object>>any());
    }

    @Test
    public void shouldSaveHitIfEndpointHitDtoFieldAppIsBlankCase() {
        hitDto.setApp(" ");
        ConstraintViolationException exp = Assertions.assertThrows(ConstraintViolationException.class,
            () -> statsClient.saveHit(hitDto));
        Assertions.assertEquals("saveHit.hitDto.app: Значение поля должно быть указанно", exp.getMessage());
        verify(restTemplateMock, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Object>>any());
    }

    @Test
    public void shouldSaveHitIfEndpointHitDtoFieldAppIsMoreThanMaxLengthCase() {
        hitDto.setApp("1234567891011121314151617181920");
        ConstraintViolationException exp = Assertions.assertThrows(ConstraintViolationException.class,
            () -> statsClient.saveHit(hitDto));
        Assertions.assertEquals("saveHit.hitDto.app: Максимальное значение поля app 30 символов", exp.getMessage());
        verify(restTemplateMock, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Object>>any());
    }

    @Test
    public void shouldSaveHitIfEndpointHitDtoFieldTimestampNullCase() {
        hitDto.setTimestamp(null);
        ConstraintViolationException exp = Assertions.assertThrows(ConstraintViolationException.class,
            () -> statsClient.saveHit(hitDto));
        Assertions.assertEquals("saveHit.hitDto.timestamp: Значение поля должно быть указанно",
            exp.getMessage());
        verify(restTemplateMock, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Object>>any());
    }

    @Test
    public void shouldSaveHitIfEndpointHitDtoStandardCase() {
        ResponseEntity<Object> response = new ResponseEntity<>(HttpStatus.CREATED);
        when(restTemplateMock.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Object>>any())).thenReturn(response);
        ResponseEntity<Object> actual = statsClient.saveHit(hitDto);
        verify(restTemplateMock).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Object>>any());
        Assertions.assertEquals(response.getStatusCode(), actual.getStatusCode());
        Assertions.assertEquals(response.getBody(), actual.getBody());
    }

    @Test
    public void shouldGetStatsStandardCase() {
        ResponseEntity<List<ViewStats>> response = new ResponseEntity<>(List.of(new ViewStats("test", "/event", 2L)),
            HttpStatus.OK);
        when(restTemplateMock.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<List<ViewStats>>>any(), anyMap())).thenReturn(response);

        List<ViewStats> actual = statsClient.getStats(start, end, uri, unique);

        verify(restTemplateMock).exchange(urlCaptor.capture(), any(), any(),
            ArgumentMatchers.<ParameterizedTypeReference<List<ViewStats>>>any(), mapCaptor.capture());

        String urlExpected = "/stats?start={start}&end={end}&uris={uri}&unique={unique}";

        Assertions.assertEquals(urlCaptor.getValue(), urlExpected);
        Assertions.assertEquals(mapCaptor.getValue().get("start"), DateTimeParser.parseToString(start));
        Assertions.assertEquals(mapCaptor.getValue().get("end"), DateTimeParser.parseToString(end));
        Assertions.assertEquals(mapCaptor.getValue().get("uri"), uri);
        Assertions.assertEquals(mapCaptor.getValue().get("unique"), unique);

        Assertions.assertEquals(response.getBody(), actual);
    }

    @Test
    public void shouldGetStatsStartNullCase() {
        ConstraintViolationException epx = Assertions.assertThrows(ConstraintViolationException.class,
            () -> statsClient.getStats(null, end, uri, unique));
        Assertions.assertEquals("getStats.startDate: не должно быть null", epx.getMessage());
        verify(restTemplateMock, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Object>>any(), anyMap());
    }

    @Test
    public void shouldGetStatsEndNullCase() {
        ConstraintViolationException epx = Assertions.assertThrows(ConstraintViolationException.class,
            () -> statsClient.getStats(start, null, uri, unique));
        Assertions.assertEquals("getStats.endDate: не должно быть null", epx.getMessage());
        verify(restTemplateMock, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Object>>any(), anyMap());
    }

    @Test
    public void shouldGetStatsUriNullCase() {
        ResponseEntity<List<ViewStats>> response = new ResponseEntity<>(List.of(new ViewStats("test", "/event", 2L)),
            HttpStatus.OK);
        when(restTemplateMock.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<List<ViewStats>>>any(), anyMap())).thenReturn(response);

        List<ViewStats> actual = statsClient.getStats(start, end, null, unique);

        verify(restTemplateMock).exchange(urlCaptor.capture(), any(), any(),
            ArgumentMatchers.<ParameterizedTypeReference<List<ViewStats>>>any(), mapCaptor.capture());

        String urlExpected = "/stats?start={start}&end={end}&unique={unique}";

        Assertions.assertEquals(urlCaptor.getValue(), urlExpected);
        Assertions.assertFalse(mapCaptor.getValue().containsKey("uri"));
        Assertions.assertEquals(mapCaptor.getValue().get("start"), DateTimeParser.parseToString(start));
        Assertions.assertEquals(mapCaptor.getValue().get("end"), DateTimeParser.parseToString(end));
        Assertions.assertEquals(mapCaptor.getValue().get("unique"), unique);

        Assertions.assertEquals(response.getBody(), actual);
    }

    @Test
    public void shouldGetStatsUniqueNullCase() {
        ResponseEntity<List<ViewStats>> response = new ResponseEntity<>(List.of(new ViewStats("test", "/event", 2L)),
            HttpStatus.OK);
        when(restTemplateMock.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<List<ViewStats>>>any(), anyMap())).thenReturn(response);

        List<ViewStats> actual = statsClient.getStats(start, end, uri, null);

        verify(restTemplateMock).exchange(urlCaptor.capture(), any(), any(),
            ArgumentMatchers.<ParameterizedTypeReference<List<ViewStats>>>any(), mapCaptor.capture());

        String urlExpected = "/stats?start={start}&end={end}&uris={uri}";

        Assertions.assertEquals(urlCaptor.getValue(), urlExpected);
        Assertions.assertFalse(mapCaptor.getValue().containsKey("unique"));
        Assertions.assertEquals(mapCaptor.getValue().get("start"), DateTimeParser.parseToString(start));
        Assertions.assertEquals(mapCaptor.getValue().get("end"), DateTimeParser.parseToString(end));
        Assertions.assertEquals(mapCaptor.getValue().get("uri"), uri);

        Assertions.assertEquals(response.getBody(), actual);
    }

    @Test
    public void shouldGetStatsUniqueAndUriNullCase() {
        ResponseEntity<List<ViewStats>> response = new ResponseEntity<>(List.of(new ViewStats("test", "/event", 2L)),
            HttpStatus.OK);
        when(restTemplateMock.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<List<ViewStats>>>any(), anyMap())).thenReturn(response);

        List<ViewStats> actual = statsClient.getStats(start, end, null, null);

        verify(restTemplateMock).exchange(urlCaptor.capture(), any(), any(),
            ArgumentMatchers.<ParameterizedTypeReference<List<ViewStats>>>any(), mapCaptor.capture());

        String urlExpected = "/stats?start={start}&end={end}";

        Assertions.assertEquals(urlCaptor.getValue(), urlExpected);
        Assertions.assertFalse(mapCaptor.getValue().containsKey("unique"));
        Assertions.assertFalse(mapCaptor.getValue().containsKey("uri"));
        Assertions.assertEquals(mapCaptor.getValue().get("start"), DateTimeParser.parseToString(start));
        Assertions.assertEquals(mapCaptor.getValue().get("end"), DateTimeParser.parseToString(end));

        Assertions.assertEquals(response.getBody(), actual);
    }

    @Test
    public void shouldGetStatsEndTimeIsBeforeStartCase() {
        end = LocalDateTime.of(1900, 1,1,1,1,1);
        InvalidStartEndTimeException exp = Assertions.assertThrows(InvalidStartEndTimeException.class,
            () -> statsClient.getStats(start, end, uri, unique));
        Assertions.assertEquals("Дата начала должна быть ранее даты окончания", exp.getMessage());
        verify(restTemplateMock, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Object>>any(), anyMap());
    }
}