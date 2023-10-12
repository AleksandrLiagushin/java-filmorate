package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbFilmStorage implements FilmStorage {
    private static final LocalDate EARLIEST_FILM_RELEASE = LocalDate.of(1895, 12, 5);
    private static final int DEFAULT_FILMS_COUNT = 10;
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private final MpaStorage mpaStorage;
    private final UserStorage userStorage;

    @Override
    public Film add(Film film) {
        if (isNotValid(film)) {
            throw new ValidationException("Film validation has been failed");
        }
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

        if (film.getDirectors() != null) {
            film.setDirectors(film.getDirectors().stream().distinct().collect(Collectors.toList()));
            directorUpdate(film);
        }
        if (keyHolder.getKey() != null) {
            log.info("Film {} added", Objects.requireNonNull(keyHolder.getKey()).intValue());
        }
        return getById(film.getId());
    }

    @Override
    public Film update(Film film) {
        if (isNotValid(film)) {
            throw new ValidationException("Film validation has been failed");
        }
        int response = jdbcTemplate.update("update films set name = ?, description = ?, release_date = ?, duration = ?, rating = ?" +
                        "where id = ?",
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (response == 0) {
            throw new WrongIdException("No such film in DB with id = " + film.getId() + ". Update failed");
        }

        jdbcTemplate.update("delete from film_genre where film_id = ?", film.getId());

        if (film.getGenres() != null) {
            film.setGenres(film.getGenres().stream().distinct().collect(Collectors.toList()));
            genreUpdate(film);
        }

        jdbcTemplate.update("delete from film_director where film_id = ?", film.getId());

        if (film.getDirectors() != null) {
            film.setDirectors(film.getDirectors().stream().distinct().collect(Collectors.toList()));
            directorUpdate(film);
        }
        log.info("Film {} updated", film.getId());
        return getById(film.getId());
    }

    @Override
    public void delete(Long filmId) {
        if (isIncorrectId(filmId)) {
            throw new WrongIdException("Param must be more then 0");
        }
    }

    @Override
    public Film getById(Long filmId) {
        if (isIncorrectId(filmId)) {
            throw new WrongIdException("Param must be more then 0");
        }
        String sqlQuery = "select id, name, description, release_date, duration, rating from films where id=?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapper, filmId);
        } catch (EmptyResultDataAccessException e) {
            throw new WrongIdException("There is no film in DB with id = " + filmId);
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
    public List<Film> getPopular(int count, Optional<Integer> genreId, Optional<String> year)  {
        if (count <= 0) {
            count = DEFAULT_FILMS_COUNT;
        }
        return jdbcTemplate.query(
                "select res.id, res.name, res.description, res.release_date, res.duration, res.cnt, res.rating " +
                "from ( " +
                "select f.*, l.cnt " +
                "from films f " +
                "left join (select fl.film_id, count(fl.user_id) cnt from film_like fl group by fl.film_id) l " +
                "on f.id = l.film_id " +
                "where ? is null " +
                "and year(f.release_date) = decode(?, null, year(f.release_date), ?) " +
                "union " +
                "select f.*, l.cnt " +
                "from films f " +
                "left join (select fl.film_id, count(fl.user_id) cnt from film_like fl group by fl.film_id) l " +
                "on f.id = l.film_id " +
                "join (select fg.film_id from film_genre fg where fg.genre_id = nvl(?, fg.genre_id) group by fg.film_id) g " +
                "on f.id = g.film_id " +
                "where ? is not null " +
                "and year(f.release_date) = decode(?, null, year(f.release_date), ?) " +
                ") res " +
                "order by res.cnt desc " +
                "limit ? ", this::mapper,
                genreId.orElse(null),
                year.orElse(null),
                year.orElse(null),
                genreId.orElse(null),
                genreId.orElse(null),
                year.orElse(null),
                year.orElse(null),
                count);
    }

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
        return jdbcTemplate.query("select f.*, count(1) cnt " +
                "from films f join film_like fl on f.id = fl.film_id " +
                "where fl.user_id in (?, ?) " +
                "group by f.id " +
                "having cnt > 1", this::mapper, userId, friendId);
    }

    @Override
    public List<Film> getTopByDirector(int id, String sortBy) {
        directorStorage.getDirectorById(id);
        String sqlRequest = "SELECT f.* FROM films f LEFT JOIN " +
                "(SELECT fl.film_id, COUNT(fl.user_id) cnt FROM film_like fl GROUP BY fl.film_id) l " +
                "on f.id = l.film_id " +
                "WHERE f.id IN (SELECT film_id FROM film_director WHERE director_id = ?)";
        switch (sortBy) {
            case "year":
                sqlRequest = sqlRequest + "ORDER BY f.release_date";
                break;
            case "likes":
                sqlRequest = sqlRequest + "ORDER BY cnt";
                break;
            default:
                throw new ValidationException("No such sort was found");
        }

        return jdbcTemplate.query(sqlRequest, this::mapper, id);
    }

    @Override
    public List<Film> searchFilms(String query, String by) {
        query = "%" + query + "%";
        String sqlRequest = "SELECT f.* FROM films f " +
                "LEFT JOIN (SELECT fl.film_id, COUNT(fl.user_id) cnt FROM film_like fl GROUP BY fl.film_id) l " +
                "on f.id = l.film_id ";
        switch (by) {
            case "title":
                sqlRequest = sqlRequest + "WHERE lower(f.name) LIKE lower(?) ORDER BY cnt DESC";
                return jdbcTemplate.query(sqlRequest, this::mapper, query);
            case "director":
                sqlRequest = "SELECT * FROM directors d " +
                        "JOIN film_director fd ON d.id = fd.director_id " +
                        "JOIN films f ON fd.film_id = f.id " +
                        "LEFT JOIN (SELECT fl.film_id, COUNT(fl.user_id) cnt FROM film_like fl GROUP BY fl.film_id) l " +
                        "on f.id = l.film_id " +
                        "WHERE lower(d.name) LIKE lower(?) " +
                        "ORDER BY cnt DESC";
                return jdbcTemplate.query(sqlRequest, this::mapper, query);
            case "title,director":
            case "director,title":
                sqlRequest = sqlRequest + "LEFT JOIN (SELECT * FROM directors d JOIN film_director fd " +
                        "ON d.id=fd.director_id) dn ON f.id=dn.film_id " +
                        "WHERE lower(dn.name) LIKE lower(?) OR lower(f.name) LIKE lower(?) " +
                        "ORDER BY cnt DESC";
                return jdbcTemplate.query(sqlRequest, this::mapper, query, query);
        }
        throw new ValidationException("No such sort was found");
    }

    private Film mapper(ResultSet resultSet, int rowNum) {
        try {
            Mpa mpa = mpaStorage.getById(resultSet.getInt("rating"));
            List<Genre> genres = genreStorage.getByFilmId(resultSet.getLong("id"));
            List<Director> directors = directorStorage.getByFilmId(resultSet.getLong("id"));

            return Film.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .description(resultSet.getString("description"))
                    .releaseDate(resultSet.getDate("release_date").toLocalDate())
                    .duration(resultSet.getInt("duration"))
                    .mpa(mpa)
                    .genres(genres)
                    .directors(directors)
                    .build();
        } catch (SQLException e) {
            throw new WrongIdException("Can't unwrap film from DB response");
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

    private void directorUpdate(Film film) {
        jdbcTemplate.batchUpdate("INSERT INTO film_director (film_id, director_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        long directorId = film.getDirectors().get(i).getId();
                        ps.setLong(1, film.getId());
                        ps.setLong(2, directorId);
                    }

                    @Override
                    public int getBatchSize() {
                        return film.getDirectors().size();
                    }
                }
        );
    }

    private boolean isIncorrectId(Long id) {
        return id == null || id <= 0;
    }

    private boolean isNotValid(Film film) {
        return film.getReleaseDate().isBefore(EARLIEST_FILM_RELEASE);
    }
}