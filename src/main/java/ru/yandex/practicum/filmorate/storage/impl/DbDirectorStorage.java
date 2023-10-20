package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DbDirectorStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Director> getDirectorById(int id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("select * from directors where id = ?",
                    (rs, RowNum) -> new Director(rs.getInt("id"), rs.getString("name")), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Director addDirector(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlQuery = "insert into directors (name) values (?)";

        jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(sqlQuery, new String[]{"id"});
            statement.setString(1, director.getName());
            return statement;
        }, keyHolder);

        log.info("director {} added", keyHolder.getKey());
        director.setId(keyHolder.getKey().intValue());

        return director;
    }

    @Override
    public List<Director> getAllDirectors() {
        return jdbcTemplate.query("select * from directors",
                (rs, RowNum) -> new Director(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public Director updateDirector(Director director) {
        jdbcTemplate.update("update directors set name = ? where id = ?",
                director.getName(),
                director.getId());
        log.info("Director {} updated", director.getId());
        return getDirectorById(director.getId()).orElse(null);
    }

    @Override
    public long deleteDirector(long id) {
        jdbcTemplate.update("delete from directors where id = ?", id);
        return id;
    }

    @Override
    public List<Director> getByFilmId(long filmId) {
        return jdbcTemplate.query(
                "select * from directors where id in (select director_id from film_director where film_id = ?)",
                (rs, RowNum) -> mapper(rs),
                filmId);
    }

    @Override
    public boolean existsById(long id) {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(1) from directors where id=?", Integer.class, id);
            return count == 1;
    }

    private Director mapper(ResultSet resultSet) throws SQLException {
        return new Director(resultSet.getInt("id"), resultSet.getString("name"));
    }
}