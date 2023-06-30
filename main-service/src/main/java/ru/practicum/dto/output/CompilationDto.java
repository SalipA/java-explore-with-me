package ru.practicum.dto.output;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompilationDto {
    private List<EventShortDto> events = new ArrayList<>();
    private Long id;
    private Boolean pinned;
    private String title;
}