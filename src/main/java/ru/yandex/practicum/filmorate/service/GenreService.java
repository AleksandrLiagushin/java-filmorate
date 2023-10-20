package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public List<Genre> getAll() {
        return genreStorage.getAll();
    }

    public Genre getById(int id) {
        if (isIncorrectId(id)) {
            throw new WrongIdException("Param must be more then 0");
        }
        Optional<Genre> genreOpt = genreStorage.getById(id);

        return genreOpt.orElseThrow(() -> new WrongIdException("No such genre in DB with id = " + id + " was found."));
    }

    private boolean isIncorrectId(int id) {
        return id <= 0;
    }

}
