package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;

import java.util.Set;

@FeignClient(name = "event-service")
@Validated
public interface EventFeign {

    @GetMapping("/events/{eventId}")
    ResponseEntity<EventFullDto> getEventById(@PathVariable Long eventId);

    @GetMapping("/events/find")
    ResponseEntity<Set<EventShortDto>> getEventsByIds(@RequestParam Set<Long> eventIds);
}
