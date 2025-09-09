package ru.practicum.service;

import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(GetUserParam getUserParam);

    UserDto createUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);

    UserShortDto getUserById(Long userId);

    List<UserShortDto> getUsersShort(@RequestParam List<Long> ids);
}
