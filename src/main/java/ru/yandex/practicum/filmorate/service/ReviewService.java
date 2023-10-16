package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final ReviewLikeStorage reviewLikeStorage;

    public Review addReview(Review review) {
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(long id) {
        reviewStorage.deleteReview(id);
    }

    public Review getReviewById(long id) {
        return reviewStorage.getReviewById(id);
    }

    public List<Review> getAllReviews() {
        return reviewStorage.getAllReviews();
    }

    public List<Review> getReviewsByFilmId(long filmId, int count) {
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    public void addLikeToReview(long id, long userId) {
        reviewLikeStorage.addLike(id, userId);
    }

    public void addDislikeToReview(long id, long userId) {
        reviewLikeStorage.addDislike(id, userId);
    }

    public void deleteLikeOrDislike(long id, long userId) {
        reviewLikeStorage.deleteLikeOrDislike(id, userId);
    }
}

