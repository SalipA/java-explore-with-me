package ru.practicum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new ru.practicum.ViewStats (h.app, h.uri, COUNT(DISTINCT h.ip)) FROM EndpointHit h " +
        "WHERE h.timestamp BETWEEN :start AND :end AND h.uri IN :uris GROUP BY h.app, h.uri ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> getStatsDistinctByUris(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                           @Param("uris") String[] uris);

    @Query("SELECT new ru.practicum.ViewStats (h.app, h.uri, COUNT(DISTINCT h.ip)) FROM EndpointHit h " +
        "WHERE h.timestamp BETWEEN :start AND :end GROUP BY h.app, h.uri ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> getStatsAllDistinct(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.ViewStats (h.app, h.uri, COUNT(h)) FROM EndpointHit h " +
        "WHERE h.timestamp BETWEEN :start AND :end GROUP BY h.app, h.uri ORDER BY COUNT(h) DESC")
    List<ViewStats> getStatsAll(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.ViewStats (h.app, h.uri, COUNT(h)) FROM EndpointHit h " +
        "WHERE h.timestamp BETWEEN :start AND :end AND h.uri IN :uris GROUP BY h.app, h.uri ORDER BY COUNT(h) DESC")
    List<ViewStats> getStatsByUris(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                   @Param("uris") String[] uris);

}
