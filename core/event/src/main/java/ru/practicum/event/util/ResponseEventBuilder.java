package ru.practicum.event.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.dto.ConfirmedRequests;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ResponseEvent;
import ru.practicum.dto.StatParam;
import ru.practicum.dto.UserShortDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.enums.RequestStatus;
import ru.practicum.event.mapper.MapperEvent;
import ru.practicum.event.model.Event;
import ru.practicum.feign.RequestFeign;
import ru.practicum.feign.StatsFeign;
import ru.practicum.feign.UserFeign;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.Constants.MIN_START_DATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseEventBuilder {
    private final MapperEvent eventMapper;
    private final StatsFeign statsFeign;
    private final RequestFeign requestFeign;
    private final UserFeign userFeign;

    public <T extends ResponseEvent> T buildOneEventResponseDto(Event event, Class<T> type) {
        T dto;

        if (type == EventFullDto.class) {
            EventFullDto dtoTemp = eventMapper.toEventFullDto(event);
            UserShortDto user = userFeign.getUser(event.getInitiatorId());
            dtoTemp.setInitiator(user);
            dto = type.cast(dtoTemp);
        } else {
            EventShortDto dtoTemp = eventMapper.toEventShortDto(event);
            UserShortDto user = userFeign.getUser(event.getInitiatorId());
            dtoTemp.setInitiator(user);
            dto = type.cast(dtoTemp);
        }

        long eventId = event.getId();
        LocalDateTime created = event.getCreatedOn();

        dto.setConfirmedRequests(getOneEventConfirmedRequests(eventId));
        dto.setViews(getOneEventViews(created, eventId));
        return dto;
    }

    public <T extends ResponseEvent> List<T> buildManyEventResponseDto(List<Event> events, Class<T> type) {
        Map<Long, T> dtoById = new HashMap<>();

        for (Event event : events) {
            if (type == EventFullDto.class) {
                EventFullDto dtoTemp = eventMapper.toEventFullDto(event);
                UserShortDto user = userFeign.getUser(event.getInitiatorId());
                dtoTemp.setInitiator(user);
                dtoById.put(event.getId(), type.cast(dtoTemp));
            } else {
                EventShortDto dtoTemp = eventMapper.toEventShortDto(event);
                UserShortDto user = userFeign.getUser(event.getInitiatorId());
                dtoTemp.setInitiator(user);
                dtoById.put(event.getId(), type.cast(dtoTemp));
            }
        }

        getManyEventsConfirmedRequests(dtoById.keySet()).forEach(req ->
                dtoById.get(req.eventId()).setConfirmedRequests(req.countRequests()));


        getManyEventsViews(dtoById.keySet()).forEach(stats -> {
            Long id = Long.parseLong(stats.getUri().replace("/events/", ""));
            dtoById.get(id).setViews(stats.getHits());
        });

        return new ArrayList<>(dtoById.values());
    }

    private int getOneEventConfirmedRequests(long eventId) {
        return Math.toIntExact(requestFeign.getRequestsByEventId(eventId).stream()
                .filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED))
                .count());
    }

    private long getOneEventViews(LocalDateTime created, long eventId) {
        StatParam statParam = StatParam.builder()
                .start(created.minusMinutes(1))
                .end(LocalDateTime.now().plusMinutes(1))
                .unique(true)
                .uris(List.of("/events/" + eventId))
                .build();

        List<ViewStatsDto> viewStats = statsFeign.getStats(statParam.getStart(), statParam.getEnd(), statParam.getUris(), statParam.getUnique()).getBody();

        if (viewStats == null) {
            viewStats = new ArrayList<>();
        }

        log.debug("Статистика пустая = {} . Одиночный от статистики по запросу uris = {}, start = {}, end = {}",
                viewStats.isEmpty(),
                statParam.getUris(),
                statParam.getStart(),
                statParam.getEnd());
        return viewStats.isEmpty() ? 0 : viewStats.getFirst().getHits();
    }

    private List<ConfirmedRequests> getManyEventsConfirmedRequests(Collection<Long> eventIds) {
        return requestFeign.getRequestsByEvents(eventIds.stream().toList());
    }

    private List<ViewStatsDto> getManyEventsViews(Collection<Long> eventIds) {
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        StatParam statParam = StatParam.builder()
                .start(MIN_START_DATE)
                .end(LocalDateTime.now().plusMinutes(1))
                .unique(true)
                .uris(uris)
                .build();

        List<ViewStatsDto> viewStats = statsFeign.getStats(statParam.getStart(), statParam.getEnd(), statParam.getUris(),
                statParam.getUnique()).getBody();

        if (viewStats == null) {
            viewStats = new ArrayList<>();
        }

        log.debug("Получен ответ size = {}, массовый от статистики по запросу uris = {}, start = {}, end = {}",
                viewStats.size(),
                statParam.getUris(),
                statParam.getStart(),
                statParam.getEnd());
        return viewStats;
    }
}
