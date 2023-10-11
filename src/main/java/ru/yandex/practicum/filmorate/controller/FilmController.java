package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping("")
    public List<Film> getAllFilms() throws SQLException {
        log.info("Get list of films.");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable long id) {
        log.info("Requested film {}", id);
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") long count) {
        log.info("Requested most popular {} films", count);
        return filmService.getTopFilms(count);
    }

    @PostMapping("")
    public Film post(@Valid @RequestBody Film film) {
        log.info("Requested add film {}", film);
        return filmService.addFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Requested add like to film {} from user {}", id, userId);
        filmService.addLike(userId, id);
        return filmService.getFilmById(id);
    }

    @PutMapping("")
    public Film update(@Valid @RequestBody Film film) {
        log.info("Requested update film {}", film);
        return filmService.update(film);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Requested delete like to film {} from user {}", id, userId);
        filmService.deleteLike(userId, id);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam("userId") long userId, @RequestParam("friendId") long friendId) {
       return filmService.getCommonFilms(userId, friendId);
    }
}
