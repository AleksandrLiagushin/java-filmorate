package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final LikeStorage likeStorage;
    private final DirectorStorage directorStorage;

    public Film addFilm(Film film) {
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public void addLike(long userId, long filmId) {
        likeStorage.addLike(userId, filmId);
    }

    public void deleteLike(long userId, long filmId) {
        likeStorage.deleteLike(userId, filmId);
    }

    public Film getFilmById(long filmId) {
        return filmStorage.getById(filmId);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public List<Film> getTopFilms(int count, Optional<Integer> genreId, Optional<String> year) {
        return filmStorage.getPopular(count, genreId, year);
    }

    public List<Film> getTopByDirector(int id, String sortBy) {
        directorStorage.getDirectorById(id);
        return filmStorage.getTopByDirector(id, sortBy);
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> searchFilms(String query, String by) {
        return filmStorage.searchFilms(query, by);
    }

    public void deleteFilmById(long id) {
        filmStorage.delete(id);
    }
}
