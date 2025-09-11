package ru.practicum.feign;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service")
@Validated
public interface UserFeign {

    @GetMapping("/admin/users/{userId}")
    UserShortDto getUser(@PathVariable Long userId);

    @GetMapping("/admin/users")
    List<UserDto> getUsers(@Nullable @RequestParam List<Long> ids,
                           @Min(0) @RequestParam(defaultValue = "0") Integer from,
                           @Min(1) @RequestParam(defaultValue = "10") Integer size);

    @GetMapping("/admin/users/short")
    List<UserShortDto> getUsersShort(@Nullable @RequestParam List<Long> ids);
}
