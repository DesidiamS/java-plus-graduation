package ru.practicum.feign;

import jakarta.validation.constraints.Min;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.ConfirmedRequests;
import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service")
@Validated
public interface RequestFeign {

    @GetMapping("/users/{userId}/requests")
    List<ParticipationRequestDto> getParticipationRequests(@Min(1) @PathVariable Long userId);

    @GetMapping("/requests/{eventId}")
    List<ParticipationRequestDto> getRequestsByEventId(@Min(1) @PathVariable Long eventId);

    @GetMapping("/requests")
    List<ConfirmedRequests> getRequestsByEvents(@RequestParam List<Long> eventsId);

    @PostMapping("/users/{userId}/requests/{requestId}/confirm")
    ParticipationRequestDto confirmParticipationRequest(@Min(1) @PathVariable Long userId,
                                                        @Min(1) @PathVariable Long requestId);

    @PostMapping("/users/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancelParticipationRequest(@Min(1) @PathVariable Long userId,
                                                       @Min(1) @PathVariable Long requestId);
}
