package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public List<Mpa> getAll() {
        return mpaStorage.getAll();
    }

    public Mpa getById(int id) {
        if (isIncorrectId(id)) {
            throw new WrongIdException("Param must be more then 0");
        }
        Optional<Mpa> mpaOpt = mpaStorage.getById(id);

        return mpaOpt.orElseThrow(() -> new WrongIdException("No such Mpa with id = " + id + " in DB was found"));
    }

    private boolean isIncorrectId(int id) {
        return id <= 0;
    }
}
