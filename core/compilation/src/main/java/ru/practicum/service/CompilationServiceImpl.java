package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.CompilationRepository;
import ru.practicum.EventCompilationRepository;
import ru.practicum.MapperCompilation;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.EventFeign;
import ru.practicum.model.Compilation;
import ru.practicum.model.EventCompilation;
import ru.practicum.model.EventCompilationId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final MapperCompilation mapperCompilation;
    private final CompilationRepository compilationRepository;
    private final EventFeign eventFeign;
    private final EventCompilationRepository eventCompilationRepository;

    @Override
    public List<CompilationDto> getCompilations(GetCompilationsParam param) {
        log.debug("Попытка получить подборку событий с параметрами {}", param);

        List<Compilation> compilations =
                compilationRepository.findAllByPinned(param.getPinned(), param.getPageable());
        log.debug("Успешно получено {} подборок с параметрами {}", compilations.size(), param);
        List<CompilationDto> result = compilations.stream()
                .map(mapperCompilation::toCompilationDto)
                .toList();

        Map<Long, Set<EventShortDto>> compilationToEvents = getEventsByCompilation(compilations);

        result.forEach(c -> c.setEvents(compilationToEvents.getOrDefault(c.getId(), Set.of())));

        return result;
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.trace("Попытка получить подборку по eventId = {}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с eventId = " + compId + " не найдена"));
        log.trace("Подборка с eventId = {} найдена", compId);

        CompilationDto result = mapperCompilation.toCompilationDto(compilation);

        result.setEvents(getEventsByCompilation(List.of(compilation)).getOrDefault(compId, Set.of()));

        return result;
    }

    @Transactional
    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.trace("Попытка создать новую подборку");
        Compilation compilation = mapperCompilation.toCompilation(newCompilationDto);
        List<Long> events = new ArrayList<>(newCompilationDto.getEvents());
        compilation.setEvents(events);
        compilationRepository.save(compilation);
        List<EventCompilation> eventCompilations = new ArrayList<>();
        for (Long event : events) {
            EventCompilation eventCompilation = new EventCompilation();
            eventCompilation.setEventCompilationId(new EventCompilationId(event, compilation.getId()));

            eventCompilations.add(eventCompilation);
        }

        eventCompilationRepository.saveAll(eventCompilations);
        log.trace("Успешно сохранена подборка, eventId = {}", compilation.getId());
        CompilationDto result = mapperCompilation.toCompilationDto(compilation);

        result.setEvents(getEventsByCompilation(List.of(compilation)).getOrDefault(compilation.getId(), Set.of()));

        return result;
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId) {
        log.trace("Попытка обновить подборку");
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с eventId = " + compId + " не найдена"));

        if (updateCompilationRequest.hasEvents()) {
            log.trace("Необходимо обновить events");
            Set<EventShortDto> eventsInDb = eventFeign.getEventsByIds(updateCompilationRequest.getEvents()).getBody();
            List<Long> eventIds;

            if (eventsInDb != null) {
                eventIds = eventsInDb.stream().map(EventShortDto::getId).toList();
            } else {
                eventIds = new ArrayList<>();
            }

            long sizeEventsInDb = Stream.of(eventsInDb).count();
            log.debug("Количество events в запросе = {} найдено в БД = {}",
                    updateCompilationRequest.getEvents().size(),
                    sizeEventsInDb);

            if (updateCompilationRequest.getEvents().size() != sizeEventsInDb) {
                log.trace("Одно или более событий включенных в подборку не существует");
                throw new NotFoundException("Одно или более событий включенных в подборку не существует");
            }
            log.trace("Количество events совпало с количеством в базе, обновляется база");

            List<Long> events = new ArrayList<>(updateCompilationRequest.getEvents());
            compilation.setEvents(events);

            if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
                compilation.getEvents().clear();
                eventIds.forEach(compilation.getEvents()::add);
            }
        }

        if (updateCompilationRequest.hasTitle()
                && !compilation.getTitle().equals(updateCompilationRequest.getTitle())) {
            log.trace("Необходимо обновить title");
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        if (updateCompilationRequest.hasPinned()
                && !compilation.getPinned().equals(updateCompilationRequest.getPinned())) {
            log.trace("Необходимо обновить pinned");
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        log.trace("Compilation успешно обновлен");

        CompilationDto result = mapperCompilation.toCompilationDto(compilation);

        if (updateCompilationRequest.hasEvents()) {
            List<EventCompilation> eventCompilations = new ArrayList<>();
            for (Long event : updateCompilationRequest.getEvents()) {
                EventCompilation eventCompilation = new EventCompilation();
                eventCompilation.setEventCompilationId(new EventCompilationId(event, compilation.getId()));

                eventCompilations.add(eventCompilation);
            }

            eventCompilationRepository.saveAll(eventCompilations);
            Set<EventShortDto> events = eventFeign.getEventsByIds(updateCompilationRequest.getEvents()).getBody();
            result.setEvents(events);
        }

        return result;
    }

    @Transactional
    @Override
    public void deleteCompilation(Long compId) {
        log.trace("Попытка удалить подборку с eventId = {}", compId);
        compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с eventId = " + compId + " не найдена"));

        compilationRepository.deleteById(compId);
        log.trace("Подборка с eventId = {} успешно удалена", compId);
    }

    private Map<Long, Set<EventShortDto>> getEventsByCompilation(List<Compilation> compilations) {
        List<EventCompilation> eventCompilations = eventCompilationRepository.findAllByCompilationIdIn(compilations.stream()
                .map(Compilation::getId).collect(Collectors.toList()));

        Set<Long> allEventIds = eventCompilations.stream()
                .map(eventCompilation -> eventCompilation.getEventCompilationId().getEventId())
                .collect(Collectors.toSet());

        Map<Long, EventShortDto> eventMap = Objects.requireNonNull(eventFeign.getEventsByIds(allEventIds)
                        .getBody())
                .stream()
                .collect(Collectors.toMap(EventShortDto::getId, e -> e));

        return eventCompilations.stream()
                .collect(Collectors.groupingBy(
                        eventCompilation -> eventCompilation.getEventCompilationId().getCompilationId(),
                        Collectors.mapping(ec -> eventMap.get(ec.getEventCompilationId().getEventId()),
                                Collectors.toSet())
                ));
    }
}
