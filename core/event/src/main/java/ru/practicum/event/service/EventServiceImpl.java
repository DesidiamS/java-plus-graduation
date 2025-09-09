package ru.practicum.event.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.dto.UpdateEventParam;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.mapper.MapperCategory;
import ru.practicum.event.mapper.MapperEvent;
import ru.practicum.event.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.QEvent;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.event.service.param.GetEventAdminParam;
import ru.practicum.event.service.param.GetEventUserParam;
import ru.practicum.event.util.ResponseEventBuilder;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.RequestFeign;
import ru.practicum.feign.UserFeign;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.Constants.EVENT_NOT_FOUND;
import static ru.practicum.enums.EventState.CANCELED;
import static ru.practicum.enums.EventState.PENDING;
import static ru.practicum.enums.EventState.PUBLISHED;
import static ru.practicum.enums.EventState.REJECTED;
import static ru.practicum.event.dto.UpdateEventAdminRequest.StateAction.PUBLISH_EVENT;
import static ru.practicum.event.dto.UpdateEventUserRequest.StateAction.SEND_TO_REVIEW;
import static ru.practicum.event.util.ValidatorEventTime.isEventTimeBad;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final MapperEvent eventMapper;
    private final LocationRepository locationRepository;
    private final ResponseEventBuilder responseEventBuilder;
    private final CategoryService categoryService;
    private final MapperCategory mapperCategory;
    private final RequestFeign requestFeign;
    private final UserFeign userFeign;

    @Override
    public List<EventFullDto> getEventsByAdmin(GetEventAdminParam param) {
        QEvent event = QEvent.event;
        BooleanBuilder requestBuilder = new BooleanBuilder();
        if (param.hasUsers()) {
            requestBuilder.and(event.initiatorId.in(param.getUsers()));
        }

        if (param.hasStates()) {
            requestBuilder.and(event.state.in(param.getStates()));
        }

        if (param.hasCategories()) {
            requestBuilder.and(event.category.id.in(param.getCategories()));
        }

        if (param.hasRangeStart()) {
            requestBuilder.and(event.createdOn.gt(param.getRangeStart()));
        }

        if (param.hasRangeEnd()) {
            requestBuilder.and(event.createdOn.lt(param.getRangeEnd()));
        }

        List<Event> events = eventRepository.findAll(requestBuilder, param.getPage()).getContent();
        return responseEventBuilder.buildManyEventResponseDto(events, EventFullDto.class);
    }

    @Override
    public List<EventShortDto> getEventsByUser(GetEventUserParam param) {
        QEvent event = QEvent.event;

        BooleanBuilder requestBuilder = new BooleanBuilder();

        requestBuilder.and(event.state.eq(PUBLISHED));

        if (param.hasText()) {
            BooleanExpression descriptionExpression = event.description.like(param.getText());
            BooleanExpression annotationExpression = event.annotation.like(param.getText());
            requestBuilder.andAnyOf(descriptionExpression, annotationExpression);
        }

        if (param.hasCategories()) {
            requestBuilder.and(event.category.id.in(param.getCategories()));
        }

        if (param.hasPaid()) {
            requestBuilder.and(event.paid.eq(param.getPaid()));
        }

        requestBuilder.and(event.eventDate.gt(Objects.requireNonNullElseGet(param.getRangeStart(), LocalDateTime::now)));

        if (param.hasRangeEnd()) {
            requestBuilder.and(event.eventDate.lt(param.getRangeEnd()));
        }

        List<Event> events = eventRepository.findAll(requestBuilder, param.getPage()).getContent();
        List<EventShortDto> eventDtoList = responseEventBuilder.buildManyEventResponseDto(events, EventShortDto.class);

        if (param.getOnlyAvailable()) {
            eventDtoList.removeIf(dto -> dto.getConfirmedRequests() == dto.getParticipantLimit());
        }

        return eventDtoList;
    }

    @Override
    public List<EventShortDto> getAllUsersEvents(Long userId, Pageable page) {
        List<Event> events = eventRepository.findByInitiatorId(userId, page);
        if (!events.isEmpty()) {
            return responseEventBuilder.buildManyEventResponseDto(events, EventShortDto.class);
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public EventFullDto addNewEvent(Long userId, NewEventDto eventDto) {
        Event event = eventMapper.toEvent(eventDto);

        if (isEventTimeBad(eventDto.getEventDate(), 2)) {
            throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
        }

        Category category = mapperCategory.toCategory(categoryService.getById(eventDto.getCategory()));
        event.setCategory(category);

        UserShortDto initiator = userFeign.getUser(userId);
        event.setInitiatorId(initiator.getId());

        event.getLocation().setEvent(event);
        locationRepository.save(event.getLocation());

        event = eventRepository.save(event);
        return responseEventBuilder.buildOneEventResponseDto(event, EventFullDto.class);
    }

    @Override
    public EventFullDto getEventForUser(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));
        return responseEventBuilder.buildOneEventResponseDto(event, EventFullDto.class);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));

        EventState state = event.getState();
        if (state == PUBLISHED) {
            throw new ConflictException("Изменить можно только не опубликованные события, текущий статус " + state);
        }

        if (updateDto.hasStateAction()) {
            if (updateDto.getStateAction().equals(SEND_TO_REVIEW)) {
                event.setState(PENDING);
            } else {
                event.setState(CANCELED);
            }
        }

        if (updateDto.hasEventDate()) {
            if (isEventTimeBad(updateDto.getEventDate(), 2)) {
                throw new BadRequestException("Дата начала изменяемого события должна быть не ранее чем за 2 часа от даты публикации");
            }
            event.setEventDate(updateDto.getEventDate());
        }

        UpdateEventParam param = eventMapper.toUpdateParam(updateDto);
        updateEvent(event, param);

        return responseEventBuilder.buildOneEventResponseDto(event, EventFullDto.class);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        return requestFeign.getRequestsByEventId(eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));

        if (isPreModerationOff(event.getRequestModeration(), event.getParticipantLimit())) {
            return result;
        }

        List<ParticipationRequestDto> requestsAll = requestFeign.getRequestsByEventId(eventId);
        List<ParticipationRequestDto> requestsStatusPending = requestsAll.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .filter(r -> updateRequest.getRequestIds().contains(r.getId()))
                .toList();

        if (requestsStatusPending.size() != updateRequest.getRequestIds().size()) {
            throw new ConflictException("Один или более запросов не находится в статусе PENDING");
        }


        if (updateRequest.getStatus().equals(EventRequestStatusUpdateRequest.Status.REJECTED)) {
            for (ParticipationRequestDto request : requestsStatusPending) {
                request.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(request);
            }

            return result;
        }

        long participantCount = requestsAll.stream()
                .filter(r -> r.getStatus() == RequestStatus.CONFIRMED)
                .count();

        if (participantCount == event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит заявок на событие");
        }

        long limitLeft = event.getParticipantLimit() - participantCount;

        int idx = 0;
        while (idx < requestsStatusPending.size() && limitLeft > 0) {
            ParticipationRequestDto request = requestsStatusPending.get(idx);
            request.setStatus(RequestStatus.CONFIRMED);

            result.getConfirmedRequests().add(request);

            limitLeft--;
            idx++;

            requestFeign.confirmParticipationRequest(userId, request.getId());
        }

        while (idx < requestsStatusPending.size()) {
            ParticipationRequestDto request = requestsStatusPending.get(idx);
            request.setStatus(RequestStatus.CANCELED);

            result.getRejectedRequests().add(request);

            idx++;

            requestFeign.confirmParticipationRequest(userId, request.getId());
        }

        return result;
    }

    @Override
    public EventFullDto getEventById(Long eventId) {
        Event eventDomain = eventRepository.findByIdAndState(eventId, PUBLISHED)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));
        return responseEventBuilder.buildOneEventResponseDto(eventDomain, EventFullDto.class);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));

        if (event.getState() != EventState.PENDING) {
            throw new ConflictException("Изменить можно только события ожидающие модерацию, текущий статус " + event.getState());
        }

        if (updateDto.hasStateAction()) {
            EventState state;

            if (updateDto.getStateAction() == PUBLISH_EVENT) {
                state = PUBLISHED;
                event.setPublishedOn(LocalDateTime.now());
            } else {
                state = REJECTED;
            }

            event.setState(state);
        }

        if (updateDto.hasEventDate()) {
            if (isEventTimeBad(updateDto.getEventDate(), 1)) {
                throw new BadRequestException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
            event.setEventDate(updateDto.getEventDate());
        }

        UpdateEventParam param = eventMapper.toUpdateParam(updateDto);
        updateEvent(event, param);

        return responseEventBuilder.buildOneEventResponseDto(event, EventFullDto.class);
    }

    @Override
    public Set<EventShortDto> getEventsByIds(Set<Long> eventIds) {
        Set<Event> events = eventRepository.findAllByIdIn(new ArrayList<>(eventIds));

        List<UserShortDto> users = userFeign.getUsersShort(events.stream().map(Event::getInitiatorId).toList());

        /*Set<EventShortDto> result = events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toSet());*/

        Map<Long, UserShortDto> usersById = users.stream()
                .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event);
                    dto.setInitiator(usersById.get(event.getInitiatorId()));
                    return dto;
                })
                .collect(Collectors.toSet());
    }

    private void updateEvent(Event event, UpdateEventParam param) {
        if (param.hasCategory()) {
            Category category = mapperCategory.toCategory(categoryService.getById(param.getCategory()));
            event.setCategory(category);
        }

        if (param.hasAnnotation()) {
            event.setAnnotation(param.getAnnotation());
        }

        if (param.hasDescription()) {
            event.setDescription(param.getDescription());
        }

        if (param.hasLocation()) {
            event.getLocation().setLatitude(param.getLocation().getLatitude());
            event.getLocation().setLongitude(param.getLocation().getLongitude());
        }

        if (param.hasPaid()) {
            event.setPaid(param.getPaid());
        }

        if (param.hasParticipantLimit()) {
            event.setParticipantLimit(param.getParticipantLimit());
        }

        if (param.hasRequestModeration()) {
            event.setRequestModeration(param.getRequestModeration());
        }

        if (param.hasTitle()) {
            event.setTitle(param.getTitle());
        }
    }

    private boolean isPreModerationOff(boolean moderationStatus, int limit) {
        return !moderationStatus || limit == 0;
    }
}
