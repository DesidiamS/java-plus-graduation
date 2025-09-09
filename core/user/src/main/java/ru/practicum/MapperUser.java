package ru.practicum;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MapperUser {
    User toUser(NewUserRequest newUserRequest);

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);
}
