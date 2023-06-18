import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ContextConfiguration(classes = {ExploreWithMeStatsService.class})
@WebMvcTest(controllers = StatsController.class)
@ExtendWith(MockitoExtension.class)
public class StatsControllerTest {
    EndpointHitDto hitDto;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private StatsService statsService;
    @Autowired
    private ObjectMapper objectMapper;
    private String[] uri;

    @BeforeEach
    public void createEntitiesAndEnvironment() {
        this.hitDto = new EndpointHitDto("main-service", "/events", "111.111.111.111",
            LocalDateTime.of(2023, 1, 1, 1, 1, 1));
        this.uri = new String[]{"/events", "/event/1"};
    }

    @SneakyThrows
    @Test
    public void shouldHitStandardCase() {
        doNothing().when(statsService).saveHit(any());

        mockMvc.perform(post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hitDto)))
            .andExpect(status().isCreated());

        verify(statsService, times(1)).saveHit(hitDto);
    }

    @SneakyThrows
    @Test
    public void shouldHitIfEndpointHitDtoFieldUriIsNullCase() {
        hitDto.setUri(null);

        mockMvc.perform(post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hitDto)))
            .andExpect(status().isBadRequest());

        verify(statsService, never()).saveHit(hitDto);
    }

    @SneakyThrows
    @Test
    public void shouldHitIfEndpointHitDtoFieldAppIsBlankCase() {
        hitDto.setApp("  ");

        mockMvc.perform(post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hitDto)))
            .andExpect(status().isBadRequest());

        verify(statsService, never()).saveHit(hitDto);
    }

    @SneakyThrows
    @Test
    public void shouldHitIfEndpointHitDtoFieldIpIsMoreThanMaxLengthCase() {
        hitDto.setIp("123456789012345678");

        mockMvc.perform(post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hitDto)))
            .andExpect(status().isBadRequest());

        verify(statsService, never()).saveHit(hitDto);
    }

    @SneakyThrows
    @Test
    public void shouldGetStatsStandardCase() {
        when(statsService.getStats(anyString(), anyString(), any(), any())).thenReturn(List.of(new ViewStats("app",
            "/events", 3L)));

        mockMvc.perform(get("/stats")
                .param("start", "2022-01-01 01:01:01")
                .param("end", "2023-01-01 01:01:01")
                .param("uris", uri)
                .param("unique", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].app").value("app"))
            .andExpect(jsonPath("$[0].uri").value("/events"))
            .andExpect(jsonPath("$[0].hits").value(3L));

        verify(statsService, times(1)).getStats(anyString(), anyString(), any(), any());
    }

    @SneakyThrows
    @Test
    public void shouldGetStatsIfRequiredParamStartIsNullCase() {
        mockMvc.perform(get("/stats")
                .param("end", "2023-01-01 01:01:01")
                .param("uris", uri)
                .param("unique", "true"))
            .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    public void shouldGetStatsIfRequiredParamEndIsNullCase() {
        mockMvc.perform(get("/stats")
                .param("start", "2022-01-01 01:01:01")
                .param("uris", uri)
                .param("unique", "true"))
            .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    public void shouldGetStatsIfNotRequiredParamUriIsNull() {
        when(statsService.getStats(anyString(), anyString(), any(), any())).thenReturn(List.of(new ViewStats("app",
            "/events", 3L)));

        mockMvc.perform(get("/stats")
                .param("start", "2022-01-01 01:01:01")
                .param("end", "2023-01-01 01:01:01")
                .param("unique", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].app").value("app"))
            .andExpect(jsonPath("$[0].uri").value("/events"))
            .andExpect(jsonPath("$[0].hits").value(3L));

        verify(statsService, times(1)).getStats("2022-01-01 01:01:01",
            "2023-01-01 01:01:01", null, true);
    }

    @SneakyThrows
    @Test
    public void shouldGetStatsIfNotRequiredParamUniqueIsNull() {
        when(statsService.getStats(anyString(), anyString(), any(), any())).thenReturn(List.of(new ViewStats("app",
            "/events", 3L)));

        mockMvc.perform(get("/stats")
                .param("start", "2022-01-01 01:01:01")
                .param("end", "2023-01-01 01:01:01")
            .param("uris", uri))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].app").value("app"))
            .andExpect(jsonPath("$[0].uri").value("/events"))
            .andExpect(jsonPath("$[0].hits").value(3L));

        verify(statsService, times(1)).getStats("2022-01-01 01:01:01",
            "2023-01-01 01:01:01", uri, false);
    }

    @SneakyThrows
    @Test
    public void shouldGetStatsIfNotRequiredParamUriAndUniqueAreNull() {
        when(statsService.getStats(anyString(), anyString(), any(), any())).thenReturn(List.of(new ViewStats("app",
            "/events", 3L)));

        mockMvc.perform(get("/stats")
                .param("start", "2022-01-01 01:01:01")
                .param("end", "2023-01-01 01:01:01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].app").value("app"))
            .andExpect(jsonPath("$[0].uri").value("/events"))
            .andExpect(jsonPath("$[0].hits").value(3L));

        verify(statsService, times(1)).getStats("2022-01-01 01:01:01",
            "2023-01-01 01:01:01", null, false);
    }
}