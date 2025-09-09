package ru.practicum.event.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.dto.ParticipationRequestDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();

    private List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
}
