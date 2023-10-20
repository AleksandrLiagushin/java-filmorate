package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.Optional;

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
    public Optional<Mpa> getById(int id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "select id, name from ratings where id = ?",
                    (resultSetMpa, rowNumMpa) -> {
                        Mpa mpa = new Mpa();
                        mpa.setId(resultSetMpa.getInt("ratings.id"));
                        mpa.setName(resultSetMpa.getString("ratings.name"));
                        return mpa;
                    }, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Mpa> getMpaByFilmId(long filmId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "select r.id, r.name from films f left join ratings r on f.rating = r.id where f.id = ?",
                    (resultSetMpa, rowNumMpa) -> {
                        Mpa mpa = new Mpa();
                        mpa.setId(resultSetMpa.getInt("ratings.id"));
                        mpa.setName(resultSetMpa.getString("ratings.name"));
                        return mpa;
                    }, filmId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
