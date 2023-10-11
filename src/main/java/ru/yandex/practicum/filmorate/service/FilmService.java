package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.WrongFilmIdException;
import ru.yandex.practicum.filmorate.exception.WrongUserIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final LocalDate EARLIEST_FILM_RELEASE = LocalDate.of(1895, 12, 5);
    private final FilmStorage filmStorage;
    private final LikeStorage likeStorage;
    private final DirectorStorage directorStorage;

    public Film addFilm(Film film) {
        if (isNotValid(film)) {
            throw new ValidationException("Film validation has been failed");
        }

        log.info("Film added {}.", film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        if (isNotValid(film)) {
            log.warn("Film is not valid. {}", film);
            throw new ValidationException("Film validation has been failed");
        }

        log.info("Film updated {}", film);
        return filmStorage.update(film);
    }

    public void addLike(long userId, long filmId) {
        if (isIncorrectId(filmId)) {
            throw new WrongFilmIdException("Param must be more then 0");
        }

        if (isIncorrectId(userId)) {
            throw new WrongUserIdException("Param must be more then 0");
        }

        log.info("Like added to film {} from user {}", filmId, userId);
        likeStorage.addLike(userId, filmId);
    }

    public void deleteLike(long userId, long filmId) {
        if (isIncorrectId(userId)) {
            throw new WrongUserIdException("Param must be more then 0");
        }

        if (isIncorrectId(filmId)) {
            throw new WrongFilmIdException("Param must be more then 0");
        }

        log.info("Like deleted from film {} from user {}", filmId, userId);
        likeStorage.deleteLike(userId, filmId);
    }

    public Film getFilmById(long filmId) {
        if (isIncorrectId(filmId)) {
            throw new WrongFilmIdException("Param must be more then 0");
        }

        return filmStorage.getById(filmId);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public List<Film> getTopFilms(long count) {
        if (isIncorrectId(count)) {
            throw new WrongFilmIdException("Param must be more then 0");
        }

        return filmStorage.getPopular(count);
    }

    public List<Film> getTopByDirector(int id, String sortBy) {
        directorStorage.getDirectorById(id);
        return filmStorage.getTopByDirector(id, sortBy);
    }

    private boolean isNotValid(Film film) {
        return film.getReleaseDate().isBefore(EARLIEST_FILM_RELEASE);
    }

    private boolean isIncorrectId(long id) {
        return id <= 0;
    }

    public void deleteFilm(long id) {
        filmStorage.delete(id);
    }
}
