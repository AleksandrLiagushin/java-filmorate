package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

public interface MpaStorage {
    List<Mpa> getAll();

    Optional<Mpa> getById(int id);

    Optional<Mpa> getMpaByFilmId(long filmId);
}
