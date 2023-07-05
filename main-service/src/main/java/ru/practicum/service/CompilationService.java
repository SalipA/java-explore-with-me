package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.IllegalActionException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.dto.output.CompilationDto;
import ru.practicum.dto.output.EventShortDto;
import ru.practicum.dto.input.NewCompilationDto;
import ru.practicum.dto.input.UpdateCompilationRequest;
import ru.practicum.entity.Compilation;
import ru.practicum.entity.Event;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.repository.CompilationRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventService eventService;

    public CompilationService(CompilationRepository compilationRepository, EventService eventService) {
        this.compilationRepository = compilationRepository;
        this.eventService = eventService;
    }

    public CompilationDto saveCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        List<Long> eventsIds = newCompilationDto.getEvents();
        if (!eventsIds.isEmpty()) {
            List<Event> eventsFromDb = eventService.getEventsByIds(eventsIds);
            if (eventsFromDb.size() == eventsIds.size()) {
                compilation.setEvents(eventsFromDb);
                Compilation saved = compilationRepository.save(compilation);
                log.info("Compilation value = {} has been saved, id = {}", newCompilationDto, saved.getId());
                return getCompilationDtoWithEventsViews(saved);
            } else {
                String message = "Unavailable action: can not save new compilation. Reason: eventsIds list contains " +
                    "non-existent events";
                log.error(message);
                throw new IllegalActionException(message);
            }
        } else {
            Compilation saved = compilationRepository.save(compilation);
            log.info("Compilation value = {} has been saved, id = {}", newCompilationDto, saved.getId());
            return CompilationMapper.toCompilationDto(saved);
        }
    }

    public void deleteCompilation(Long compId) {
        getCompilationIfExists(compId);
        compilationRepository.deleteById(compId);
        log.info("Compilation with id={} has been deleted", compId);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilationFromDb = getCompilationIfExists(compId);
        Compilation updatedCompilation = fixUpdateChanges(compilationFromDb, updateRequest);
        Compilation saved = compilationRepository.save(updatedCompilation);
        log.info("Compilation value = {} has been updated, id = {}", updatedCompilation, compId);
        return getCompilationDtoWithEventsViews(saved);
    }

    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageRequest = PageRequestSpecifier.getPageRequestWithoutSort(from, size);
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageRequest).getContent();
        }
        List<CompilationDto> compilationDtoList = compilations.stream()
            .map(this::getCompilationDtoWithEventsViews)
            .collect(Collectors.toList());
        log.info("Get request for Compilations list by pinned = {} processed successfully", pinned);
        return compilationDtoList;
    }

    public CompilationDto getCompilation(Long compId) {
        Compilation compilationFromDb = getCompilationIfExists(compId);
        log.info("Get request for Compilation by id = {} processed successfully", compId);
        return getCompilationDtoWithEventsViews(compilationFromDb);
    }

    private Compilation getCompilationIfExists(Long compId) {
        Optional<Compilation> compilation = compilationRepository.findById(compId);
        if (compilation.isPresent()) {
            return compilation.get();
        } else {
            String message = "Compilation with id =" + compId + " was not found";
            log.error(message);
            throw new NotFoundException(message);
        }
    }

    private Compilation fixUpdateChanges(Compilation compilation, UpdateCompilationRequest updateRequest) {
        if (!updateRequest.getEvents().isEmpty()) {
            List<Event> events = eventService.getEventsByIds(updateRequest.getEvents());
            if (events.size() == updateRequest.getEvents().size()) {
                compilation.setEvents(events);
            } else {
                String message = "Unavailable action: can not update compilation. Reason: eventsIds list contains " +
                    "non-existent events";
                log.error(message);
                throw new IllegalActionException(message);
            }
        }
        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }
        return compilation;
    }

    private CompilationDto getCompilationDtoWithEventsViews(Compilation compilation) {
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilation);
        List<EventShortDto> eventShortDtoList = EventMapper.toEventShortDtoList(compilation.getEvents());
        eventService.addViews(eventShortDtoList);
        compilationDto.setEvents(eventShortDtoList);
        return compilationDto;
    }
}