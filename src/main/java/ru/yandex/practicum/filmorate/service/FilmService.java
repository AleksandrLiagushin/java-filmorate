package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.MarkStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final LocalDate EARLIEST_FILM_RELEASE = LocalDate.of(1895, 12, 5);
    private static final int DEFAULT_FILMS_COUNT = 10;
    private final FilmStorage filmStorage;
    private final MarkStorage markStorage;
    private final FeedStorage feedStorage;
    private final UserService userService;
    private final DirectorService directorService;
    private final FilmFullService filmFullService;

    public Film addFilm(Film film) {
        if (isNotValid(film)) {
            throw new ValidationException("Film validation has been failed");
        }
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        if (isNotValid(film)) {
            throw new ValidationException("Film validation has been failed");
        }
        if (!existsById(film.getId())) {
            throw new WrongIdException("No film with id = " + film.getId() + " in DB was found.");
        }
        return filmFullService.update(film);
    }

    public void addMark(long userId, long filmId, int mark) {
        isLegalMark(mark);
        if (!existsById(filmId)) {
            throw new WrongIdException("No film with id = " + filmId + " in DB was found.");
        }
        if (!userService.existsById(userId)) {
            throw new WrongIdException("No user with id = " + userId + " in DB was found.");
        }

        Map<Long, Integer> marksByFilm = markStorage.getMarksByFilmId(filmId);
        if (marksByFilm.get(userId) == mark) {
            return;
        }
        if (marksByFilm.containsKey(userId)) {
            markStorage.deleteMark(userId, filmId);
            feedStorage.deleteMark(userId, filmId);
        }

        markStorage.addMark(userId, filmId, mark);
        feedStorage.addMark(userId, filmId);
    }

    public void deleteMark(long userId, long filmId) {
        if (!existsById(filmId)) {
            throw new WrongIdException("No film with id = " + filmId + " in DB was found.");
        }
        if (!userService.existsById(userId)) {
            throw new WrongIdException("No user with id = " + userId + " in DB was found.");
        }

        markStorage.deleteMark(userId, filmId);
        feedStorage.deleteMark(userId, filmId);
    }

    public void deleteFilmById(long id) {
        if (isIncorrectId(id)) {
            throw new WrongIdException("Param must be more then 0");
        }

        filmStorage.delete(id);
    }

    public Film getFilmById(long filmId) {
        return filmFullService.getFilmById(filmId);
    }

    public List<Film> getAllFilms() {
        return filmFullService.getAllFilms();
    }

    public List<Film> getTopFilms(int count, Integer genreId, String year) {
        if (count <= 0) {
            count = DEFAULT_FILMS_COUNT;
        }
        return filmFullService.getTopFilms(count, genreId, year);
    }

    public List<Film> getTopByDirector(int id, String sortBy) {
        if (!directorService.existsById(id)) {
            throw new WrongIdException("No director with id = " + id + " in DB was found.");
        }
        return filmFullService.getTopByDirector(id, sortBy);
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        if (!existsById(userId) || !existsById(friendId)) {
            throw new WrongIdException("No users with id = " + userId + " or " + friendId + " in DB was found.");
        }
        return filmFullService.getCommonFilms(userId, friendId);
    }

    public List<Film> searchFilms(String query, String by) {
        return filmFullService.searchFilms(query, by);
    }

    public boolean existsById(long filmId) {
        return !isIncorrectId(filmId) && filmStorage.existsById(filmId);
    }

    private boolean isIncorrectId(long id) {
        return id <= 0;
    }

    private boolean isNotValid(Film film) {
        return film.getReleaseDate().isBefore(EARLIEST_FILM_RELEASE);
    }

    private void isLegalMark(int mark) {
        if (mark < 1 || mark > 10) {
            throw new ValidationException("Mark must be between 1 and 10");
        }
    }
}