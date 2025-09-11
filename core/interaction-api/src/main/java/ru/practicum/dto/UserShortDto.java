package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserShortDto {
    private Long id;

    @NotBlank
    private String name;
}
