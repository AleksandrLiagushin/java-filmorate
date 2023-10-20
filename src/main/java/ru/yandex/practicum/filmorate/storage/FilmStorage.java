package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);

    Film update(Film film);

    void delete(Long filmId);

    Optional<Film> getById(Long filmId);

    List<Film> getAllFilms();

    List<Film> getTopByDirector(int id, String sortBy);

    List<Film> getPopular(int count, Integer genreId, String year);

    List<Film> getCommonFilms(long userId, long friendId);

    List<Film> searchFilms(String query, String by);

    List<Film> getRecommendations(long userId);

    boolean existsById(long id);
}
