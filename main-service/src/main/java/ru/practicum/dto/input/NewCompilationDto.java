package ru.practicum.dto.input;

import lombok.*;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
public class NewCompilationDto {
    private List<Long> events = new ArrayList<>();
    @Nullable
    private Boolean pinned;
    @NotBlank
    @Size(min = 1, max = 50, message = "length must be min=1, max=50")
    private String title;
}