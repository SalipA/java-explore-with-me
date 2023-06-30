package ru.practicum.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.dto.output.ApiError;
import ru.practicum.DateTimeParser;
import ru.practicum.exception.IllegalActionException;
import ru.practicum.InvalidStartEndTimeException;
import ru.practicum.exception.NotFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestControllerAdvice(assignableTypes = {AdminController.class, PublicController.class, PrivateController.class})
public class MainServiceExceptionController {

    private static final String BAD_REQUEST_REASON = "Incorrectly made request";
    private static final String CONFLICT_REASON = "Integrity constraint has been violated";
    private static final String NOT_FOUND_REASON = "The required object was not found";
    private static final String FORBIDDEN_REASON = "For the requested operation the conditions are not met";

    private final Clock clock;

    public MainServiceExceptionController(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolationExp(final ConstraintViolationException exp) {
        Set<ConstraintViolation<?>> constraintViolations = exp.getConstraintViolations();
        StringBuilder message = new StringBuilder();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            message.append("Field: ").append(constraintViolation.getPropertyPath()).append(". Error: ")
                .append(constraintViolation.getMessageTemplate()).append(". Value: ")
                .append(constraintViolation.getInvalidValue()).append(" ").append(". ");
        }
        return createApiError(ApiErrorStatuses.BAD_REQUEST, BAD_REQUEST_REASON, message.toString());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidExp(final MethodArgumentNotValidException exp) {
        StringBuilder message = new StringBuilder();
        List<FieldError> errors = exp.getFieldErrors();
        if (!errors.isEmpty()) {
            for (FieldError fieldError : errors) {
                message.append("Field: ").append(fieldError.getField()).append(". Error: ")
                    .append(fieldError.getDefaultMessage()).append(". Value: ")
                    .append(fieldError.getRejectedValue()).append(". ");
            }
        }
        return createApiError(ApiErrorStatuses.BAD_REQUEST, BAD_REQUEST_REASON, message.toString());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationExp(final DataIntegrityViolationException exp) {
        return createApiError(ApiErrorStatuses.CONFLICT, CONFLICT_REASON, exp.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIllegalActionExp(final IllegalActionException exp) {
        return createApiError(ApiErrorStatuses.FORBIDDEN, FORBIDDEN_REASON, exp.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundExp(final NotFoundException exp) {
        return createApiError(ApiErrorStatuses.NOT_FOUND, NOT_FOUND_REASON, exp.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNumberFormatExp(final NumberFormatException exp) {
        return createApiError(ApiErrorStatuses.BAD_REQUEST, BAD_REQUEST_REASON, exp.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalid(final InvalidStartEndTimeException exp) {
        return createApiError(ApiErrorStatuses.BAD_REQUEST, BAD_REQUEST_REASON, exp.getMessage());
    }

    private ApiError createApiError(ApiErrorStatuses status, String reason, String message) {
        ApiError response = new ApiError();
        response.setStatus(status.name());
        response.setReason(reason);
        response.setMessage(message);
        response.setTimestamp(DateTimeParser.parseToString(LocalDateTime.now(clock)));
        return response;
    }

    enum ApiErrorStatuses {
        BAD_REQUEST, CONFLICT, NOT_FOUND, FORBIDDEN
    }
}