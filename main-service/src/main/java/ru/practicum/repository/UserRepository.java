package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.output.EventInitiatorDto;
import ru.practicum.entity.User;
import ru.practicum.state.UserProfileState;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> getUsersByIdIn(List<Long> ids, Pageable pageable);

    List<User> getUsersByIdIn(List<Long> ids);

    @Query("SELECT new ru.practicum.dto.output.EventInitiatorDto(u.id, u.name, u.profile, COUNT(DISTINCT e.id), " +
        "    COUNT(DISTINCT s.id)) " +
        "FROM User u " +
        "LEFT JOIN u.events e " +
        "LEFT JOIN u.subscribers s " +
        "WHERE (:profile IS NULL OR u.profile = :profile)" +
        "GROUP BY u.id, u.name, u.profile " +
        "ORDER BY COUNT(DISTINCT s.id) DESC")
    Page<EventInitiatorDto> findMostPopular(@Param("profile") UserProfileState profile, Pageable pageable);

    @Query("SELECT new ru.practicum.dto.output.EventInitiatorDto(u.id, u.name, u.profile, COUNT(DISTINCT e.id), COUNT" +
        "(DISTINCT s.id)) " +
        "FROM User u " +
        "LEFT JOIN u.events e " +
        "LEFT JOIN u.subscribers s " +
        "WHERE (:profile IS NULL OR u.profile = :profile)" +
        "GROUP BY u.id, u.name, u.profile " +
        "ORDER BY COUNT(DISTINCT e.id) DESC")
    Page<EventInitiatorDto> findMostInitiative(@Param("profile") UserProfileState profile, Pageable pageable);
}
