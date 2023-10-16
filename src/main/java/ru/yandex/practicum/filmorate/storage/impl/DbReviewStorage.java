package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InvalidPathVariableException;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbReviewStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FeedStorage feedStorage;

    @Override
    public Review addReview(Review review) {
        filmStorage.getById(review.getFilmId());
        userStorage.getById(review.getUserId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlQuery = "insert into reviews (content, is_positive, user_id, film_id) values (?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setLong(3, review.getUserId());
            stmt.setLong(4, review.getFilmId());
            return stmt;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            log.info("Review {} from user {} on film {} added",
                    Objects.requireNonNull(keyHolder.getKey()).longValue(), review.getUserId(), review.getFilmId());
        }

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        feedStorage.addReview(review.getUserId(), review.getReviewId());
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        getReviewById(review.getReviewId());
        int response = jdbcTemplate.update("update reviews set content = ?, is_positive = ? where id = ?",
                review.getContent(),
                review.isIsPositive(),
                review.getReviewId());

        if (response == 0) {
            throw new WrongIdException("No such review in DB with id = " + review.getReviewId() + ". Update failed");
        }

        Review reviewUpdated = getReviewById(review.getReviewId());
        log.info("Review {} from user {} on film {} updated",
                review.getReviewId(), review.getUserId(), review.getFilmId());
        feedStorage.updateReview(reviewUpdated.getUserId(), review.getReviewId());
        return reviewUpdated;
    }

    @Override
    public void deleteReview(long id) {
        if (isIncorrectId(id)) {
            throw new InvalidPathVariableException("Param must be more then 0");
        }

        Review review = getReviewById(id);
        jdbcTemplate.update("delete from reviews where id = ?", id);
        feedStorage.deleteReview(review.getUserId(), id);
    }

    @Override
    public Review getReviewById(long id) {
        if (isIncorrectId(id)) {
            throw new WrongIdException("Id must be more than 0");
        }
        String sqlQuery = "select r.*, u.cnt from reviews r left join (select review_id, sum(useful) cnt " +
                "from review_like group by review_id) u on r.id = u.review_id " +
                "where r.id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new WrongIdException("There is no review in DB with id = " + id);
        }
    }

    @Override
    public List<Review> getAllReviews() {
        return jdbcTemplate.query(
                "select r.*, u.cnt from reviews r left join (select review_id, nvl(sum(useful),0) cnt " +
                        "from review_like group by review_id) u on r.id = u.review_id order by nvl(u.cnt,0) desc",
                this::mapper
        );
    }

    @Override
    public List<Review> getReviewsByFilmId(long filmId, int count) {
        filmStorage.getById(filmId);
        return jdbcTemplate.query(
                "select r.*, u.cnt from reviews r left join (select review_id, nvl(sum(useful),0) cnt " +
                        "from review_like group by review_id) u " +
                        "on r.id = u.review_id " +
                        "where r.film_id = ? " +
                        "order by nvl(u.cnt,0) desc " +
                        "limit ?",
                this::mapper,
                filmId,
                count
        );
    }

    private Review mapper(ResultSet resultSet, int rowNum) {
        try {
            return Review.builder()
                    .reviewId(resultSet.getLong("id"))
                    .content(resultSet.getString("content"))
                    .isPositive(resultSet.getBoolean("is_positive"))
                    .userId(resultSet.getLong("user_id"))
                    .filmId(resultSet.getLong("film_id"))
                    .useful(resultSet.getInt("u.cnt"))
                    .build();
        } catch (SQLException e) {
            throw new WrongIdException(e.getMessage());
        }
    }

    private boolean isIncorrectId(long id) {
        return id <= 0;
    }

    private boolean isIsPositiveValid(Review review) {
        return review.getIsPositive();
    }
}

