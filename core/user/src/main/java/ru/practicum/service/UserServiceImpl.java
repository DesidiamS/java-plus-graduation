package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.Constants;
import ru.practicum.MapperUser;
import ru.practicum.UserRepository;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.exception.DuplicateException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MapperUser mapperUser;

    @Override
    public List<UserDto> getUsers(GetUserParam getUserParam) {
        Page<User> users = userRepository.findUsersByIds(getUserParam.getIds(), getUserParam.getPageable());
        return users.map(mapperUser::toUserDto).getContent();
    }

    @Override
    public List<UserShortDto> getUsersShort(List<Long> ids) {
        Page<User> users = userRepository.findUsersByIds(ids, null);
        return users.map(mapperUser::toUserShortDto).getContent();
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        if (userRepository.findByEmailIgnoreCase(newUserRequest.getEmail()).isPresent()) {
            throw new DuplicateException(Constants.DUPLICATE_USER);
        }

        User user = mapperUser.toUser(newUserRequest);

        user = userRepository.save(user);

        return mapperUser.toUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(Constants.USER_NOT_FOUND);
        }

        userRepository.deleteById(userId);
    }

    @Override
    public UserShortDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(Constants.USER_NOT_FOUND));

        return mapperUser.toUserShortDto(user);
    }
}
