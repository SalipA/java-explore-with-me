package ru.practicum.dto.reversible;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CategoryDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @NotBlank(message = "must not be blank")
    @Size(min = 1, max = 50, message = "length must be min=1, max=50")
    private String name;
}
