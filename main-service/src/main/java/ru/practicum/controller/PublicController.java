package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.reversible.CategoryDto;
import ru.practicum.dto.output.CompilationDto;
import ru.practicum.dto.output.EventFullDto;
import ru.practicum.dto.output.EventShortDto;
import ru.practicum.service.CategoryService;
import ru.practicum.service.CompilationService;
import ru.practicum.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@Validated
@Slf4j
public class PublicController {
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;

    public PublicController(CategoryService categoryService, EventService eventService, CompilationService compilationService) {
        this.categoryService = categoryService;
        this.eventService = eventService;
        this.compilationService = compilationService;
    }

    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getCategories(@RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                           @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("GET: /categories, from = {}, size = {}", from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategory(@PathVariable @Positive Long catId) {
        log.info("GET: /categories/{}", catId);
        return categoryService.getCategory(catId);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) Long[] categories, @RequestParam(required =
        false) Boolean paid, @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd, @RequestParam(required =
        false, defaultValue = "false") Boolean onlyAvailable, @RequestParam(required = false) String sort,
                                         @RequestParam(required = false,
                                             defaultValue = "0") @Min(0) Integer from,
                                         @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size,
                                         HttpServletRequest request) {
        log.info("GET: /events, text = {}, categories = {}, paid = {}, rangeStart = {}, rangeEnd = {}, onlyAvailable " +
                "= {}, sort = {}, from = {}, size = {}", text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
            sort, from, size);
        return eventService.getEventsPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from,
            size, request);
    }

    @GetMapping("/events/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable(name = "id") @Positive Long eventId, HttpServletRequest request) {
        log.info("GET: /events/{}", eventId);
        return eventService.getEventPublic(eventId, request);
    }

    @GetMapping("/compilations")
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                                @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("GET: /compilations, pinned = {}, from = {}, size = {}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto getCompilation(@PathVariable @Positive Long compId) {
        log.info("GET: /compilations/{}", compId);
        return compilationService.getCompilation(compId);
    }
}