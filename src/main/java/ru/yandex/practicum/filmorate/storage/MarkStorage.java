package ru.yandex.practicum.filmorate.storage;

import java.util.Map;

public interface MarkStorage {
    void addMark(long userId, long filmId, int mark);

    void deleteMark(long userId, long filmId);

    Map<Long, Integer> getMarksByFilmId(Long filmId);

    Map<Long, Integer> getMarksByUserId(Long userId);
}
