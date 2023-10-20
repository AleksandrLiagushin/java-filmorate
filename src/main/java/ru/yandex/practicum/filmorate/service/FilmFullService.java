package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmFullService {
    private final FilmStorage filmStorage;
    private final DirectorStorage directorStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    public Film getFilmById(long filmId) {
        if (isIncorrectId(filmId)) {
            throw new WrongIdException("Param must be more then 0");
        }
        Optional<Film> filmOpt = filmStorage.getById(filmId);

        return addAttributesToFilm(
                filmOpt.orElseThrow(() -> new WrongIdException("No film with id = " + filmId + " in DB was found.")));
    }

    public List<Film> getAllFilms() {
        return addAttributesToFilms(filmStorage.getAllFilms());
    }

    public List<Film> getTopFilms(int count, Integer genreId, String year) {
        return addAttributesToFilms(filmStorage.getPopular(count, genreId, year));
    }

    public List<Film> getTopByDirector(int id, String sortBy) {
        return addAttributesToFilms(filmStorage.getTopByDirector(id, sortBy));
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        return addAttributesToFilms(filmStorage.getCommonFilms(userId, friendId));
    }

    public List<Film> searchFilms(String query, String by) {
        return addAttributesToFilms(filmStorage.searchFilms(query, by));
    }

    public List<Film> getRecommendations(long userId) {
        return addAttributesToFilms(filmStorage.getRecommendations(userId));
    }

    public Film update(Film film) {
        return addAttributesToFilm(filmStorage.update(film));
    }

    private boolean isIncorrectId(long id) {
        return id <= 0;
    }

    private Film addAttributesToFilm(Film film) {
        if (film != null) {
            film.setGenres(genreStorage.getByFilmId(film.getId()));
            film.setDirectors(directorStorage.getByFilmId(film.getId()));
            film.setMpa(mpaStorage.getMpaByFilmId(film.getId()).orElse(null));
        }
        return film;
    }

    private List<Film> addAttributesToFilms(List<Film> filmList) {
        List<Film> fullFilmList = new ArrayList<>();
        if (filmList.isEmpty()) {
            return fullFilmList;
        }
        for (Film film : filmList) {
            fullFilmList.add(addAttributesToFilm(film));
        }
        return fullFilmList;
    }
}



