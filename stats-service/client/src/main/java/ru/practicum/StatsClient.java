package ru.practicum;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Validated
@Setter// для возможности внедрить мок для тестирования
public class StatsClient {
    private static final String API_PATH_HITS = "/hit";
    private static final String API_PREFIX_STATS = "/stats";
    private RestTemplate rest;

    public StatsClient(@Value("${S_HOST}") String serverHost, @Value("${S_PORT}") String serverPort) {
        String serverUrl = "http://" + serverHost + ":" + serverPort;
        RestTemplateBuilder builder = new RestTemplateBuilder();
        this.rest = builder
            .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
            .requestFactory(HttpComponentsClientHttpRequestFactory::new)
            .build();
    }

    public ResponseEntity<Object> saveHit(@NotNull(message = "не должно быть null") @Valid EndpointHitDto hitDto) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(hitDto);
        ResponseEntity<Object> statsServiceResponse;
        statsServiceResponse = rest.exchange(API_PATH_HITS, HttpMethod.POST, requestEntity, Object.class);
        return statsServiceResponse;
    }

    public List<ViewStats> getStats(@NotNull(message = "не должно быть null") LocalDateTime startDate,
                                    @NotNull(message = "не должно быть null") LocalDateTime endDate, @Nullable String[] uri,
                                    @Nullable Boolean unique) {
        validateStartEndTime(startDate, endDate);

        String start = DateTimeParser.parseToString(startDate);
        String end = DateTimeParser.parseToString(endDate);

        Map<String, Object> parameters;
        String path;

        if (uri == null) {
            if (unique == null) {
                parameters = Map.of("start", start, "end", end);
                path = "?start={start}&end={end}";
            } else {
                parameters = Map.of("start", start, "end", end, "unique", unique);
                path = "?start={start}&end={end}&unique={unique}";
            }
        } else {
            if (unique == null) {
                parameters = Map.of("start", start, "end", end, "uri", uri);
                path = "?start={start}&end={end}&uris={uri}";
            } else {
                parameters = Map.of("start", start, "end", end, "uri", uri, "unique", unique);
                path = "?start={start}&end={end}&uris={uri}&unique={unique}";
            }
        }
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, null);
        ResponseEntity<List<ViewStats>> statsServiceResponse;
        statsServiceResponse = rest.exchange(API_PREFIX_STATS + path, HttpMethod.GET, requestEntity,
            new ParameterizedTypeReference<>() {
            }, parameters);
        return statsServiceResponse.getBody();
    }

    private void validateStartEndTime(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            log.error("Start date must be before End date");
            throw new InvalidStartEndTimeException("Дата начала должна быть ранее даты окончания");
        }
    }
}