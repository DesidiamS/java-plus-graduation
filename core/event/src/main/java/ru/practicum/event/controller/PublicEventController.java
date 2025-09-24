package ru.practicum.event.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.RecommendationDto;
import ru.practicum.event.enums.EventSortType;
import ru.practicum.event.service.EventService;
import ru.practicum.event.service.param.GetEventUserParam;
import ru.practicum.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static ru.practicum.Constants.DATE_PATTERN;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/events")
@Validated
public class PublicEventController {

    private final EventService eventService;
    private static final String USER_HEADER = "X-EWM-USER-ID";

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEventsByFilters(@RequestParam(name = "text", required = false) String text,
                                                                  @RequestParam(name = "categories", required = false) List<Long> categories,
                                                                  @RequestParam(name = "paid", required = false) Boolean paid,
                                                                  @RequestParam(name = "rangeStart", required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeStart,
                                                                  @RequestParam(name = "rangeEnd", required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeEnd,
                                                                  @RequestParam(name = "onlyAvailable", defaultValue = "false") Boolean onlyAvailable,
                                                                  @RequestParam(name = "sort", required = false) EventSortType sort,
                                                                  @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer from,
                                                                  @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer size) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("rangeStart > rangeEnd");
        }
        log.info("Пришел GET запрос /events на Public Event Controller");

        Pageable page;
        if (sort != null) {
            Sort sortType = switch (sort) {
                case EVENT_DATE -> Sort.by("createdOn").ascending();
                case VIEWS -> Sort.by("views").ascending();
            };
            page = PageRequest.of(from, size, sortType);
        } else {
            page = PageRequest.of(from, size);
        }

        GetEventUserParam param = GetEventUserParam.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .page(page)
                .build();

        List<EventShortDto> events = eventService.getEventsByUser(param);
        log.info("Отправлен ответ на GET /events Public Event Controller с телом: {}", events);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long eventId,
                                                     @RequestHeader(USER_HEADER) long userId) {
        log.info("Пришел GET запрос на /events/{} Public Event Controller", eventId);
        EventFullDto event = eventService.getEventById(eventId, userId);
        log.info("Отправлен ответ на GET /events/{} c телом: {}", eventId, event);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{eventId}")
    public void putLike(@PathVariable Long eventId,
                        @RequestHeader(USER_HEADER) long userId) {
        eventService.putLike(eventId, userId);
    }

    @GetMapping("/recommendations")
    public List<RecommendationDto> getRecommendations(@RequestHeader(USER_HEADER) long userId,
                                                      @RequestParam(defaultValue = "5") int limit) {
        return eventService.getRecommendations(userId, limit);
    }

    @GetMapping("/find")
    public ResponseEntity<Set<EventShortDto>> getEventByIds(@RequestParam Set<Long> eventIds) {
        return new ResponseEntity<>(eventService.getEventsByIds(eventIds), HttpStatus.OK);
    }
}
