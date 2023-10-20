package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(long id);

    Optional<Review> getReviewById(long id);

    List<Review> getAllReviews();

    List<Review> getReviewsByFilmId(long filmId, int count);

    boolean existsById(long id);
}
