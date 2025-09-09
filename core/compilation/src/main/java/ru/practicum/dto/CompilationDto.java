package ru.practicum.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CompilationDto {
    @Min(1)
    @NotNull
    private Long id;

    @NotNull
    private Boolean pinned;

    @NotBlank
    private String title;

    private Set<EventShortDto> events;
}
