package ru.practicum.dto.output;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

@Data
public class ApiError {
    private String status;
    private String reason;
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String timestamp;
}