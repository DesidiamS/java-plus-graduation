package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;

import static ru.practicum.Constants.DATE_PATTERN;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto implements ResponseEvent {
    Long id;

    @NotBlank
    String title;

    @NotNull
    UserShortDto initiator;

    @NotNull
    LocationDto location;

    @NotNull
    Boolean paid;

    @NotBlank
    String annotation;

    @NotNull
    CategoryDto category;

    int confirmedRequests;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    LocalDateTime createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    LocalDateTime publishedOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    LocalDateTime eventDate;

    int participantLimit = 0;

    String description;

    Boolean requestModeration = true;

    EventState state;

    long views;
}
