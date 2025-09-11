package ru.practicum.event.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.validator.SizeAfterTrim;

@Getter
@Setter
public class NewCategoryDto {
    @NotBlank
    @SizeAfterTrim(min = 1, max = 50)
    private String name;
}
