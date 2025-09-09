package ru.practictum.service;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practictum.MapperRequest;
import ru.practictum.RequestRepository;
import ru.practictum.model.Request;
import ru.practicum.dto.ConfirmedRequests;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.DuplicateException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.EventFeign;
import ru.practicum.feign.UserFeign;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.Constants.EVENT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final MapperRequest mapperRequest;
    private final EventFeign eventFeign;
    private final UserFeign userFeign;

    @Override
    public List<ParticipationRequestDto> getParticipationRequests(Long userId) {
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return requests.stream().map(mapperRequest::toParticipationRequestDto).toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {

        EventFullDto event;
        try {
            event = eventFeign.getEventById(eventId).getBody();
        } catch (FeignException.NotFound e) {
            throw new ConflictException(EVENT_NOT_FOUND);
        }

        UserShortDto user = userFeign.getUser(userId);

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new DuplicateException("Запрос на такое событие уже есть");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Невозможно создать запрос на неопубликованное событие");
        }

        if (event.getParticipantLimit() != 0 && requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
                >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит запросов на событие");
        }

        if (user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("Невозможно создать запрос будучи инициатором события");
        }

        boolean isPreModerationOn = isPreModerationOn(event.getRequestModeration(), event.getParticipantLimit());

        Request request = new Request(
                null,
                user.getId(),
                event.getId(),
                isPreModerationOn ? RequestStatus.PENDING : RequestStatus.CONFIRMED,
                LocalDateTime.now()
        );

        request = requestRepository.save(request);

        return mapperRequest.toParticipationRequestDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос не найден"));

        // просто проверка на существование юзера
        userFeign.getUser(userId);

        request.setStatus(RequestStatus.CANCELED);

        request = requestRepository.save(request);

        return mapperRequest.toParticipationRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequestsByEventId(Long eventId) {
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        List<ParticipationRequestDto> result = new ArrayList<>();
        for (Request request : requests) {
            ParticipationRequestDto dto = mapperRequest.toParticipationRequestDto(request);
            result.add(dto);
            //result.add(mapperRequest.toParticipationRequestDto(request));
        }

        return result;
        //return requests.stream().map(mapperRequest::toParticipationRequestDto).toList();
    }

    @Override
    public ParticipationRequestDto confirmParticipationRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос не найден"));

        // просто проверка на существование юзера
        userFeign.getUser(userId);

        request.setStatus(RequestStatus.CONFIRMED);

        request = requestRepository.save(request);

        return mapperRequest.toParticipationRequestDto(request);
    }

    @Override
    public List<ConfirmedRequests> getParticipationRequestsByEventsId(List<Long> eventsId) {
        return requestRepository.getConfirmedRequests(eventsId, RequestStatus.CONFIRMED);
    }

    private boolean isPreModerationOn(boolean moderationStatus, int limit) {
        return moderationStatus && limit != 0;
    }
}
