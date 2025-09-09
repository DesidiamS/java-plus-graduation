package ru.practicum.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.param.GetEventAdminParam;
import ru.practicum.event.service.param.GetEventUserParam;

import java.util.List;
import java.util.Set;

public interface EventService {

    List<EventFullDto> getEventsByAdmin(GetEventAdminParam param);

    List<EventShortDto> getEventsByUser(GetEventUserParam param);

    EventFullDto getEventForUser(Long userId, Long eventId);

    List<EventShortDto> getAllUsersEvents(Long userId, Pageable page);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest eventDto);

    EventFullDto addNewEvent(Long userId, NewEventDto eventDto);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventDto);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest);

    EventFullDto getEventById(Long eventId);

    Set<EventShortDto> getEventsByIds(Set<Long> eventIds);
}
