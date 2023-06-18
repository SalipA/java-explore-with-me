package ru.practicum;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "hits")
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30)
    private String app;
    @Column(nullable = false, length = 30)
    private String uri;
    @Column(nullable = false, length = 15)
    private String ip;
    @Column(nullable = false)
    private LocalDateTime timestamp;
}