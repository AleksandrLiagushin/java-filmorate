package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbReviewStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review addReview(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlQuery = "insert into reviews (content, is_positive, user_id, film_id) values (?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getPositive());
            stmt.setLong(3, review.getUserId());
            stmt.setLong(4, review.getFilmId());
            return stmt;
        }, keyHolder);

        log.info("Review {} from user {} on film {} added",
                Objects.requireNonNull(keyHolder.getKey()).longValue(), review.getUserId(), review.getFilmId());

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        getReviewById(review.getReviewId());
        jdbcTemplate.update("update reviews set content = ?, is_positive = ? where id = ?",
                review.getContent(),
                review.getPositive(),
                review.getReviewId());
        Optional<Review> reviewUpdated = getReviewById(review.getReviewId());
        log.info("Review {} from user {} on film {} updated",
                review.getReviewId(), review.getUserId(), review.getFilmId());
        return reviewUpdated.orElse(null);
    }

    @Override
    public void deleteReview(long id) {
        jdbcTemplate.update("delete from reviews where id = ?", id);
    }

    @Override
    public Optional<Review> getReviewById(long id) {
        try {
            String sqlQuery = "select r.*, u.cnt from reviews r left join (select review_id, sum(useful) cnt " +
                    "from review_like group by review_id) u on r.id = u.review_id " +
                    "where r.id = ?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, this::mapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
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

    @Override
    public boolean existsById(long id) {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(1) from reviews where id=?", Integer.class, id);
            return count == 1;
    }

    private Review mapper(ResultSet resultSet, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(resultSet.getLong("id"))
                .content(resultSet.getString("content"))
                .positive(resultSet.getBoolean("is_positive"))
                .userId(resultSet.getLong("user_id"))
                .filmId(resultSet.getLong("film_id"))
                .useful(resultSet.getInt("u.cnt"))
                .build();
    }
}

