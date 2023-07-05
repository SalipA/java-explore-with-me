package ru.practicum.mapper;

import ru.practicum.state.RequestState;
import ru.practicum.dto.output.ParticipationRequestDto;
import ru.practicum.entity.Event;
import ru.practicum.entity.Request;
import ru.practicum.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class RequestMapper {
    public static Request toRequest(User user, Event event, LocalDateTime created, RequestState state) {
        Request request = new Request();
        request.setCreated(created);
        request.setEvent(event);
        request.setRequester(user);
        request.setState(state);
        return request;
    }

    public static ParticipationRequestDto toParticipationRequestDto(Request request) {
        ParticipationRequestDto requestDto = new ParticipationRequestDto();
        requestDto.setId(request.getId());
        requestDto.setCreated(request.getCreated());
        requestDto.setEvent(request.getEvent().getId());
        requestDto.setRequester(request.getRequester().getId());
        requestDto.setStatus(request.getState());
        return requestDto;
    }

    public static List<ParticipationRequestDto> toParticipationRequestDtoList(List<Request> requests) {
        return requests.stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }
}
