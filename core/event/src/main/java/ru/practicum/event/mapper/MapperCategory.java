package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.CategoryDto;
import ru.practicum.event.dto.NewCategoryDto;
import ru.practicum.event.model.Category;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MapperCategory {
    Category toCategory(CategoryDto categoryDto);

    Category toCategory(NewCategoryDto newCategoryDto);

    CategoryDto toCategoryDto(Category category);

    List<CategoryDto> toCategoryDtoList(List<Category> categoryList);
}
