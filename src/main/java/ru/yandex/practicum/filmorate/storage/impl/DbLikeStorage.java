package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.util.HashSet;
import java.util.Set;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DbLikeStorage implements LikeStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FeedStorage feedStorage;

    @Override
    public void addLike(long userId, long filmId) {
        if (getLikesByFilmId(filmId).contains(userId)) {
            feedStorage.addLike(userId, filmId);
            return;
        }

        String sql = "insert into film_like(user_id, film_id) values(?, ?)";
        jdbcTemplate.update(sql, userId, filmId);
        feedStorage.addLike(userId, filmId);
    }

    @Override
    public void deleteLike(long userId, long filmId) {
        String sql = "delete film_like where user_id = ? and film_id = ?";
        jdbcTemplate.update(sql, userId, filmId);
        feedStorage.deleteLike(userId, filmId);
    }

    @Override
    public Set<Long> getLikesByFilmId(Long filmId) {
        return new HashSet<>(jdbcTemplate.query(
                "select user_id from film_like where film_id = ?",
                (resultSetLike, rowNumLike) -> resultSetLike.getLong("film_like.user_id"),
                filmId));
    }

    @Override
    public Set<Long> getLikesByUserId(Long userId) {

        return new HashSet<>(jdbcTemplate.query(
                "select film_id from film_like where user_id = ?",
                (rs, rowNum) -> rs.getLong("film_like.film_id"),
                userId));
    }
}
