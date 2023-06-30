package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.state.EventState;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findAllByInitiator(User user, Pageable pageable);

    List<Event> findAllByIdIn(List<Long> ids);

    @Query("SELECT e FROM Event e WHERE ((:users) IS NULL OR e.initiator IN (:users)) " +
        "AND ((:states) IS NULL OR e.state IN (:states)) " +
        "AND ((:categories) IS NULL OR e.category IN (:categories)) " +
        "AND (cast(:rangeStart as timestamp) IS NULL OR e.eventDate >= :rangeStart) " +
        "AND (cast(:rangeEnd as timestamp) IS NULL OR e.eventDate <= :rangeEnd)")
    Page<Event> findAllAdminByFilter(@Param("users") List<User> users,
                                     @Param("states") List<EventState> states,
                                     @Param("categories") List<Category> categories,
                                     @Param("rangeStart") LocalDateTime rangeStart,
                                     @Param("rangeEnd") LocalDateTime rangeEnd,
                                     Pageable pageable);

    @Query("SELECT e FROM Event e WHERE(e.state = 'PUBLISHED')" +
        "AND (:text IS NULL OR UPPER (e.annotation) LIKE CONCAT ('%', UPPER(:text), '%') OR " +
        "UPPER (e.description) LIKE CONCAT ('%',UPPER (:text), '%')) " +
        "AND (:paid IS NULL OR e.paid = :paid) " +
        "AND ((:categories) IS NULL OR e.category IN (:categories)) " +
        "AND (cast(:rangeStart as timestamp) IS NULL OR e.eventDate >= :rangeStart) " +
        "AND (cast(:rangeEnd as timestamp) IS NULL OR e.eventDate <= :rangeEnd) " +
        "AND (:onlyAvailable IS FALSE OR (e.participantLimit = 0 OR SIZE(e.requests) < e.participantLimit)) ")
    Page<Event> findAllPublicByFilter(@Param("text") String text, @Param("categories") List<Category> categories,
                                      @Param("paid") Boolean paid, @Param("rangeStart") LocalDateTime rangeStart,
                                      @Param("rangeEnd") LocalDateTime rangeEnd,
                                      @Param("onlyAvailable") Boolean onlyAvailable,
                                      Pageable pageable);

    @Query("SELECT e FROM Event e WHERE(e.state = 'PUBLISHED')" +
        "AND (:text IS NULL OR UPPER (e.annotation) LIKE CONCAT ('%', UPPER(:text), '%') OR " +
        "UPPER (e.description) LIKE CONCAT ('%',UPPER (:text), '%')) " +
        "AND (:paid IS NULL OR e.paid = :paid) " +
        "AND ((:categories) IS NULL OR e.category IN (:categories)) " +
        "AND (e.eventDate >= :now) " +
        "AND (:onlyAvailable IS FALSE OR (e.participantLimit = 0 OR SIZE(e.requests) < e.participantLimit)) ")
    Page<Event> findAllPublicByFilter(@Param("text") String text, @Param("categories") List<Category> categories,
                                      @Param("paid") Boolean paid,
                                      @Param("onlyAvailable") Boolean onlyAvailable,
                                      @Param("now") LocalDateTime now, Pageable pageable);
}
