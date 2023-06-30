package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class EndpointHitDto {
    @NotBlank(message = "Значение поля должно быть указанно")
    @Length(max = 30, message = "Максимальное значение поля app 30 символов")
    private String app;
    @NotBlank(message = "Значение поля должно быть указанно")
    @Length(max = 30, message = "Максимальное значение поля uri 30 символов")
    private String uri;
    @NotBlank(message = "Значение поля должно быть указанно")
    @Length(max = 15, message = "Максимальное значение поля ip 15 символов")
    private String ip;
    @NotNull(message = "Значение поля должно быть указанно")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}