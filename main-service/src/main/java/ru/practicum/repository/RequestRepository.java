package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.state.RequestState;
import ru.practicum.entity.Event;
import ru.practicum.entity.Request;
import ru.practicum.entity.User;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEvent(Event event);

    List<Request> findAllByRequester(User user);

    List<Request> findAllByIdInAndState(List<Long> requestId, RequestState requestState);

    List<Request> findAllByState(RequestState requestState);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Request r SET r.state = :state WHERE r.id IN (:requestIds)")
    void updateRequestStateByIds(@Param("requestIds") List<Long> requestIds, @Param("state") RequestState state);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Request r SET r.state = :state WHERE r.state = :pending")
    void rejectRequests(@Param("state") RequestState newState, @Param("pending") RequestState oldState);
}
