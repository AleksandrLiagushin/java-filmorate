package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.WrongFilmIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film add(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlQuery = "insert into films (name, description, release_date, duration, rating) values (?, ?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getGenres() != null) {
            film.setGenres(film.getGenres().stream().distinct().collect(Collectors.toList()));
            genreUpdate(film);
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        int response = jdbcTemplate.update("update films set name = ?, description = ?, release_date = ?, duration = ?, rating = ?" +
                        "where id = ?",
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (response == 0) {
            throw new WrongFilmIdException("No such film in DB with id = " + film.getId() + ". Update failed");
        }

        jdbcTemplate.update("delete from film_genre where film_id = ?", film.getId());

        if (film.getGenres() != null) {
            film.setGenres(film.getGenres().stream().distinct().collect(Collectors.toList()));
            genreUpdate(film);
        }

        return film;
    }

    @Override
    public Film delete(Film film) {
        jdbcTemplate.update("delete from films where id = ? cascade", film.getId());
        return film;
    }

    @Override
    public Film getById(Long filmId) {
        String sqlQuery = "select id, name, description, release_date, duration, rating from films where id=?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapper, filmId);
        } catch (EmptyResultDataAccessException e) {
            throw new WrongFilmIdException("There is no film in DB with id = " + filmId);
        }
    }

    @Override
    public List<Film> getAllFilms() {
        return jdbcTemplate.query(
                "select id, name, description, release_date, duration, rating from films",
                this::mapper
        );
    }

    @Override
    public List<Film> getPopular(long count) {
        return jdbcTemplate.query("select f.* from films f left join " +
                        "(select fl.film_id, count(fl.user_id) cnt from film_like fl group by fl.film_id) l " +
                        "on f.id = l.film_id " +
                        "order by l.cnt desc " +
                        "limit ?",
                this::mapper,
                count);
    }

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
       return jdbcTemplate.query("SELECT f.* " +
               "FROM films f join film_like fl ON f.id = fl.film_id " +
               "where USER_id in (?, ?) group by f.id", this::mapper, userId, friendId);
    }

    private Film mapper(ResultSet resultSet, int rowNum) {
        try {
            Mpa mpa = jdbcTemplate.queryForObject(
                    "select id, name from ratings where id = ?",
                    (resultSetMpa, rowNumMpa) -> {
                        Mpa mpa1 = new Mpa();
                        mpa1.setId(resultSetMpa.getInt("ratings.id"));
                        mpa1.setName(resultSetMpa.getString("ratings.name"));
                        return mpa1;
                    }, resultSet.getInt(6));

            return Film.builder()
                    .id(resultSet.getLong("films.id"))
                    .name(resultSet.getString("films.name"))
                    .description(resultSet.getString("films.description"))
                    .releaseDate(resultSet.getDate("films.release_date").toLocalDate())
                    .duration(resultSet.getInt("films.duration"))
                    .mpa(mpa)
                    .build();
        } catch (SQLException e) {
            throw new WrongFilmIdException("Can't unwrap film from DB response");
        }
    }

    private void genreUpdate(Film film) {
        jdbcTemplate.batchUpdate("insert into film_genre (film_id, genre_id) values (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        int genreId = film.getGenres().get(i).getId();
                        ps.setLong(1, film.getId());
                        ps.setInt(2, genreId);
                    }

                    @Override
                    public int getBatchSize() {
                        return film.getGenres().size();
                    }
                }
        );
    }

}
