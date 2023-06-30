package ru.practicum;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ViewStats {
    private String app;
    private String uri;
    private Long hits;
}
