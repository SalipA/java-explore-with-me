package ru.practicum.entity;

import lombok.*;
import org.hibernate.annotations.Where;
import ru.practicum.state.UserProfileState;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 250, unique = true)
    private String name;
    @Column(nullable = false, length = 254, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private UserProfileState profile;

    @OneToMany(mappedBy = "initiator")
    @Where(clause = "state = 'PUBLISHED'")
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "subscribedTo")
    @Where(clause = "subscribe_state = 'CONFIRMED'")
    private List<Subscription> subscribers = new ArrayList<>();

    public User(Long id, String name, String email, UserProfileState profile) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profile = profile;
    }
}
