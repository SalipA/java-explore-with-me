package ru.practicum.entity;

import lombok.*;
import ru.practicum.state.SubscriptionState;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "subscribed_to")
    private User subscribedTo;
    @ManyToOne
    @JoinColumn(name = "subscriber")
    private User subscriber;
    @Column(name = "subscribe_state")
    @Enumerated(EnumType.STRING)
    private SubscriptionState state;
}