package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbReviewLikeStorage implements ReviewLikeStorage {
    private final JdbcTemplate jdbcTemplate;
    private final ReviewStorage reviewStorage;

    @Override
    public void addLike(long reviewId, long userId) {
        jdbcTemplate.update(
                "insert into review_like (review_id, user_id, useful) values (?, ?, 1)",
                reviewId,
                userId
        );
    }

    @Override
    public void addDislike(long reviewId, long userId) {
        jdbcTemplate.update(
                "insert into review_like (review_id, user_id, useful) values (?, ?, -1)",
                reviewId,
                userId
        );
    }

    @Override
    public void deleteLikeOrDislike(long reviewId, long userId) {
        jdbcTemplate.update(
                "delete from review_like where review_id = ? and user_id = ?",
                reviewId,
                userId
        );
    }
}