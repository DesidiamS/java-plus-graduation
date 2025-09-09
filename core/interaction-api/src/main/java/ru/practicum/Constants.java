package ru.practicum;

import java.time.LocalDateTime;

public interface Constants {

    String EVENT_NOT_FOUND = "Событие не найдено";
    String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    String DUPLICATE_USER = "Пользователь с таким Email уже существует";
    String USER_NOT_FOUND = "Пользователь не найден";
    LocalDateTime MIN_START_DATE = LocalDateTime.of(1970, 1, 1, 0, 0);
}
