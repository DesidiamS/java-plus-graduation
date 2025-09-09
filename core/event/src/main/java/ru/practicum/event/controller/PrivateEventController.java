package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
@Validated
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getAllUserEvents(@PathVariable(name = "userId") Long userId,
                                                                @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer from,
                                                                @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer size) {
        log.info("Пришел GET запрос на /users/{}/events", userId);
        List<EventShortDto> events = eventService.getAllUsersEvents(userId, PageRequest.of(from, size));
        log.info("Отправлен ответ GET /users/{}/events с телом: {}", userId, events);
        return ResponseEntity.ok(events);
    }

    @PostMapping
    public ResponseEntity<EventFullDto> addNewEvent(@PathVariable(name = "userId") Long userId,
                                                    @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Пришел POST запрос на /users/{}/events с телом: {}", userId, newEventDto);
        EventFullDto event = eventService.addNewEvent(userId, newEventDto);
        log.info("Отправлен ответ POST /users/{}/events с телом: {}", userId, event);
        return new ResponseEntity<>(event, HttpStatus.CREATED);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable(name = "userId") Long userId,
                                                     @PathVariable(name = "eventId") Long eventId) {
        log.info("Пришел GET запрос на /users/{}/events/{}", userId, eventId);
        EventFullDto event = eventService.getEventForUser(userId, eventId);
        log.info("Отправлен ответ на GET /users/{}/events/{} с телом: {}", userId, eventId, event);
        return ResponseEntity.ok(event);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable(name = "userId") Long userId,
                                                    @PathVariable(name = "eventId") Long eventId,
                                                    @Valid @RequestBody UpdateEventUserRequest eventDto) {
        log.info("Пришел PATCH запрос на /users/{}/events/{} с телом: {}", userId, eventId, eventDto);
        EventFullDto event = eventService.updateEventByUser(userId, eventId, eventDto);
        log.info("Отправлен ответ на PATCH /users/{}/events/{} с телом: {}", userId, eventId, event);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getEventRequests(@PathVariable(name = "userId") Long userId,
                                                                          @PathVariable(name = "eventId") Long eventId) {
        log.info("Пришел GET запрос на /users/{}/events/{}/requests", userId, eventId);
        List<ParticipationRequestDto> requests = eventService.getEventRequests(userId, eventId);
        log.info("Отправлен ответ на GET /users/{}/events/{}/requests с телом: {}", userId, eventId, requests);
        return ResponseEntity.ok(requests);
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequests(@PathVariable(name = "userId") Long userId,
                                                                         @PathVariable(name = "eventId") Long eventId,
                                                                         @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("Пришел PATCH запрос на /users/{}/events/{}/requests с телом: {}", userId, eventId, updateRequest);
        EventRequestStatusUpdateResult result = eventService.updateEventRequests(userId, eventId, updateRequest);
        log.info("Отправлен ответ на PATCH /users/{}/events/{},requests с телом: {}", userId, eventId, result.toString());
        return ResponseEntity.ok(result);
    }
}
