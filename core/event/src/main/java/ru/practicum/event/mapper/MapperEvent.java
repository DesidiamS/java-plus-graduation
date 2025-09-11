package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.enums.EventState;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.dto.UpdateEventParam;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.model.Event;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MapperEvent {

    @Mapping(source = "category", target = "category.id", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    @Mapping(source = "category", target = "category.id", ignore = true)
    @Mapping(source = "stateAction", target = "state", qualifiedByName = "stateFromAdminAction")
    Event toEvent(UpdateEventAdminRequest updateEventAdminRequest);

    @Mapping(source = "category", target = "category.id", ignore = true)
    @Mapping(source = "stateAction", target = "state", qualifiedByName = "stateFromUserAction")
    Event toEvent(UpdateEventUserRequest updateEventUserRequest);

    EventShortDto toEventShortDto(Event event);

    @Mapping(source = "location.latitude", target = "location.lat")
    @Mapping(source = "location.longitude", target = "location.lon")
    EventFullDto toEventFullDto(Event event);

    UpdateEventParam toUpdateParam(UpdateEventAdminRequest request);

    UpdateEventParam toUpdateParam(UpdateEventUserRequest request);

    @Named("stateFromAdminAction")
    default EventState stateFromAdminAction(UpdateEventAdminRequest.StateAction action) {
        if (action == null) {
            return null;
        }

        if (action == UpdateEventAdminRequest.StateAction.PUBLISH_EVENT) {
            return EventState.PUBLISHED;
        } else {
            return EventState.REJECTED;
        }
    }

    @Named("stateFromUserAction")
    default EventState stateFromUserAction(UpdateEventUserRequest.StateAction action) {
        if (action == null) {
            return null;
        }

        if (action == UpdateEventUserRequest.StateAction.SEND_TO_REVIEW) {
            return EventState.PENDING;
        } else {
            return EventState.CANCELED;
        }
    }
}
