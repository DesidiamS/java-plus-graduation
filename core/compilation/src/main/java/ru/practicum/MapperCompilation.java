package ru.practicum;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.model.Compilation;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MapperCompilation {
    @Mapping(source = "events", target = "events", ignore = true)
    Compilation toCompilation(NewCompilationDto newCompilationDto);

    @Mapping(source = "events", target = "events", ignore = true)
    Compilation toCompilation(UpdateCompilationRequest updateCompilationRequest);

    @Mapping(source = "events", target = "events", ignore = true)
    CompilationDto toCompilationDto(Compilation compilation);
}
