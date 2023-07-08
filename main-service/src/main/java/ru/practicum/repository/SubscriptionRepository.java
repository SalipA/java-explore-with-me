package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.entity.Subscription;
import ru.practicum.entity.User;
import ru.practicum.state.SubscriptionState;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findBySubscriberAndSubscribedTo(User subscriber, User initiator);

    @Query("SELECT s FROM Subscription s WHERE (s.subscriber = :subscriber) AND (:state IS NULL OR s.state = :state)")
    Page<Subscription> findBySubscriberAndState(@Param("subscriber") User subscriber,
                                                @Param("state")SubscriptionState state, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE (s.subscribedTo = :initiator) AND (:state IS NULL OR s.state = " +
        ":state)")
    Page<Subscription> findByInitiatorAndState(@Param("initiator") User initiator,
                                               @Param("state")SubscriptionState state, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Subscription s SET s.state = :new WHERE s.state = :old AND s.subscribedTo = :initiator")
    void confirmedSubscriptions(@Param("new") SubscriptionState newState, @Param("old") SubscriptionState oldState,
                                @Param("initiator") User user);
}
