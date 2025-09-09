package ru.practictum;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.practictum.model.Request;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.enums.RequestStatus;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MapperRequest {
    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    ParticipationRequestDto toParticipationRequestDto(Request request);

    @Named("stateFromEventRequestStatusUpdateRequest")
    default RequestStatus statusFromUpdateRequestStatus(EventRequestStatusUpdateRequest.Status status) {
        if (status == EventRequestStatusUpdateRequest.Status.REJECTED) {
            return RequestStatus.REJECTED;
        } else {
            return RequestStatus.CONFIRMED;
        }
    }
}
