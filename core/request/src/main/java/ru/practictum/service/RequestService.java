package ru.practictum.service;

import ru.practicum.dto.ConfirmedRequests;
import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getParticipationRequests(Long userId);

    ParticipationRequestDto createParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId);

    ParticipationRequestDto confirmParticipationRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getParticipationRequestsByEventId(Long eventId);

    List<ConfirmedRequests> getParticipationRequestsByEventsId(List<Long> eventsId);
}
