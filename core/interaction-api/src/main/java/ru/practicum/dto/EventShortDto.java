package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static ru.practicum.Constants.DATE_PATTERN;


@Getter
@Setter
public class EventShortDto implements ResponseEvent {
    @NotBlank
    private String annotation;

    @NotNull
    private CategoryDto category;

    private int confirmedRequests;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    private LocalDateTime eventDate;

    private Long id;

    @NotNull
    private UserShortDto initiator;

    @NotNull
    private Boolean paid;

    @NotBlank
    private String title;

    private long views;

    @JsonIgnore
    private int participantLimit;
}
