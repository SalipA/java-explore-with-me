package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class StatsService {
    public final StatsRepository statsRepository;


    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public void saveHit(EndpointHitDto hitDto) {
        EndpointHit hit = EndpointHitMapper.toEndpointHit(hitDto);
        EndpointHit savedHit = statsRepository.save(hit);
        log.info("Hit value = {} has been saved, id = {}", hit, savedHit.getId());
    }

    public List<ViewStats> getStats(String start, String end, String[] uri, Boolean unique) {

        LocalDateTime startDate = DateTimeParser.parseToDate(start);
        LocalDateTime endDate = DateTimeParser.parseToDate(end);

        validateStartEndTime(startDate, endDate);

        List<ViewStats> stats;
        if (uri == null) {
            if (unique) {
                stats = statsRepository.getStatsAllDistinct(startDate, endDate);
            } else {
                stats = statsRepository.getStatsAll(startDate, endDate);
            }
        } else {
            if (unique) {
                stats = statsRepository.getStatsDistinctByUris(startDate, endDate, uri);
            } else {
                stats = statsRepository.getStatsByUris(startDate, endDate, uri);
            }
        }
        return stats;
    }

    private void validateStartEndTime(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            log.error("Start date must be before End date");
            throw new InvalidStartEndTimeExceptionStats("Start date must be before End date");
        }
    }
}
