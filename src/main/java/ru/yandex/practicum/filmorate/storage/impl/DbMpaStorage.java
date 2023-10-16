package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DbMpaStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getAll() {
        return jdbcTemplate.query(
                "select id, name from ratings",
                (resultSetGenre, rowNumGenre) -> {
                    Mpa mpa = new Mpa();
                    mpa.setId(resultSetGenre.getInt("ratings.id"));
                    mpa.setName(resultSetGenre.getString("ratings.name"));
                    return mpa;
                });
    }

    @Override
    public Mpa getById(int id) {
        try {
            return jdbcTemplate.queryForObject(
                    "select id, name from ratings where id = ?",
                    (resultSetMpa, rowNumMpa) -> {
                        Mpa mpa = new Mpa();
                        mpa.setId(resultSetMpa.getInt("ratings.id"));
                        mpa.setName(resultSetMpa.getString("ratings.name"));
                        return mpa;
                    }, id);
        } catch (EmptyResultDataAccessException e) {
            throw new WrongIdException("No such Mpa with id = " + id + " in DB was found");
        }
    }
}
