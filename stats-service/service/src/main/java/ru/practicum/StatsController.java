package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@Slf4j
@Validated
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void hit(@RequestBody @Valid EndpointHitDto hitDto) {
        log.info("POST: /hit, hitDto = {}", hitDto);
        statsService.saveHit(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(@RequestParam @NotBlank String start, @RequestParam @NotBlank String end,
                                    @RequestParam(required = false) String[] uris,
                                    @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        log.info("GET: /stats, start = {}, end = {}, uri = {}, unique = {}", start, end, uris, unique);
        return statsService.getStats(start, end, uris, unique);
    }
}