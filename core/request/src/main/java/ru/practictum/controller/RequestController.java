package ru.practictum.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practictum.service.RequestService;
import ru.practicum.dto.ConfirmedRequests;
import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class RequestController {

    private final RequestService requestService;

    @GetMapping("/{eventId}")
    public List<ParticipationRequestDto> getRequestsByEventId(@Min(1) @PathVariable Long eventId) {
        return requestService.getParticipationRequestsByEventId(eventId);
    }

    @GetMapping
    public List<ConfirmedRequests> getRequestsByEvents(@RequestParam List<Long> eventsId) {
        return requestService.getParticipationRequestsByEventsId(eventsId);
    }
}
