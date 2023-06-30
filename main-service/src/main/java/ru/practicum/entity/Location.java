package ru.practicum.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Embeddable
public class Location {
    private float lat;
    private float lon;
}