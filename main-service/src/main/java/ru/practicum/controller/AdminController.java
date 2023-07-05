package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.input.NewCompilationDto;
import ru.practicum.dto.input.UpdateCompilationRequest;
import ru.practicum.dto.input.UpdateEventAdminRequest;
import ru.practicum.dto.output.CompilationDto;
import ru.practicum.dto.output.EventFullDto;
import ru.practicum.dto.reversible.CategoryDto;
import ru.practicum.dto.reversible.UserDto;
import ru.practicum.service.CategoryService;
import ru.practicum.service.CompilationService;
import ru.practicum.service.EventService;
import ru.practicum.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/admin")
@Validated
@Slf4j
public class AdminController {
    private final CategoryService categoryService;
    private final UserService userService;
    private final EventService eventService;
    private final CompilationService compilationService;

    public AdminController(CategoryService categoryService, UserService userService, EventService eventService,
                           CompilationService compilationService) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.eventService = eventService;
        this.compilationService = compilationService;
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid CategoryDto categoryDto) {
        log.info("POST: /admin/categories, value = {}", categoryDto);
        return categoryService.addCategory(categoryDto);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @Positive Long catId) {
        log.info("DELETE: /admin/categories/{}", catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable @Positive Long catId,
                                      @RequestBody @Valid CategoryDto categoryDto) {
        log.info("PATCH: /admin/categories/{}, value = {}", catId, categoryDto);
        return categoryService.updateCategory(catId, categoryDto);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto registerUser(@RequestBody @Valid UserDto userDto) {
        log.info("POST: /admin/users, value = {}", userDto);
        return userService.registerUser(userDto);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Positive Long userId) {
        log.info("DELETE: /admin/users/{}", userId);
        userService.deleteUser(userId);
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(@RequestParam(required = false) Long[] ids,
                                  @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                  @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("GET: /admin/users, ids = {}, from = {}, size = {}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEvents(@RequestParam(required = false) Long[] users, @RequestParam(required =
        false) String[] states, @RequestParam(required = false) Long[] categories, @RequestParam(required = false)
                                        String rangeStart, @RequestParam(required = false) String rangeEnd,
                                        @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                        @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("GET: /admin/events, users = {}, states = {}, categories = {}, rangeStart = {}, rangeEnd = {} from =" +
            "= {}, size = {}", users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getEventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable @Positive Long eventId,
                                    @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("PATCH: /admin/events/{}, value = {}", eventId, updateEventAdminRequest);
        return eventService.updateEventAdmin(eventId, updateEventAdminRequest);
    }

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto saveCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("POST: /admin/compilations, value = {}", newCompilationDto);
        return compilationService.saveCompilation(newCompilationDto);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable @Positive Long compId) {
        log.info("DELETE: /admin/compilations/{}", compId);
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable @Positive Long compId,
                                            @RequestBody @Valid UpdateCompilationRequest updateRequest) {
        log.info("PATCH: /admin/compilations/{}, value = {}", compId, updateRequest);
        return compilationService.updateCompilation(compId, updateRequest);
    }
}