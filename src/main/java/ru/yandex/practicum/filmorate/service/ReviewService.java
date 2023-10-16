package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    private static final int DUMMY_PARAM_VALUE = -1;
    private final ReviewStorage reviewStorage;
    private final ReviewLikeStorage reviewLikeStorage;
    private final FeedStorage feedStorage;
    private final FilmService filmService;
    private final UserService userService;

    public Review addReview(Review review) {
        if (!filmService.isLegalFilmId(review.getFilmId())) {
            throw new WrongIdException("No film with id = " + review.getFilmId() + " in DB was found.");
        }
        if (!userService.isLegalUserId(review.getUserId())) {
            throw new WrongIdException("No user with id = " + review.getUserId() + " in DB was found.");
        }
        Review reviewAdded = reviewStorage.addReview(review);
        feedStorage.addReview(reviewAdded.getUserId(), reviewAdded.getReviewId());
        return reviewAdded;
    }

    public Review updateReview(Review review) {
        if (!filmService.isLegalFilmId(review.getFilmId())) {
            throw new WrongIdException("No film with id = " + review.getFilmId() + " in DB was found.");
        }
        if (!userService.isLegalUserId(review.getUserId())) {
            throw new WrongIdException("No user with id = " + review.getUserId() + " in DB was found.");
        }
        if (!isLegalReviewId(review.getReviewId())) {
            throw new WrongIdException("No review with id = " + review.getReviewId() + " in DB was found.");
        }
        Review reviewUpdated = reviewStorage.updateReview(review);
        if (reviewUpdated != null) {
            feedStorage.updateReview(reviewUpdated.getUserId(), reviewUpdated.getReviewId());
        }
        return reviewUpdated;
    }

    public void deleteReview(long id) {
        Review review = getReviewById(id);
        if (userService.isLegalUserId(review.getUserId())) {
            reviewStorage.deleteReview(id);
            feedStorage.deleteReview(review.getUserId(), id);
        }
    }

    public Review getReviewById(long id) {
        if (isIncorrectId(id)) {
            throw new WrongIdException("Param must be more then 0");
        }
        Optional<Review> reviewOpt = reviewStorage.getReviewById(id);
        if (reviewOpt.isEmpty()) {
            throw new WrongIdException("No review with id = " + id + " in DB was found.");
        }
        return reviewOpt.get();
    }

    public List<Review> getAllReviews() {
        return reviewStorage.getAllReviews();
    }

    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        if (filmId == DUMMY_PARAM_VALUE || !filmService.isLegalFilmId(filmId)) {
            return getAllReviews();
        }
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    public void addLikeToReview(long id, long userId) {
        if (!userService.isLegalUserId(userId)) {
            throw new WrongIdException("No user with id = " + userId + " in DB was found.");
        }
        if (!isLegalReviewId(id)) {
            throw new WrongIdException("No review with id = " + id + " in DB was found.");
        }
        reviewLikeStorage.addLike(id, userId);
    }

    public void addDislikeToReview(long id, long userId) {
        if (!userService.isLegalUserId(userId)) {
            throw new WrongIdException("No user with id = " + userId + " in DB was found.");
        }
        if (!isLegalReviewId(id)) {
            throw new WrongIdException("No review with id = " + id + " in DB was found.");
        }
        reviewLikeStorage.addDislike(id, userId);
    }

    public void deleteLikeOrDislike(long id, long userId) {
        if (!userService.isLegalUserId(userId)) {
            throw new WrongIdException("No user with id = " + userId + " in DB was found.");
        }
        if (!isLegalReviewId(id)) {
            throw new WrongIdException("No review with id = " + id + " in DB was found.");
        }
        reviewLikeStorage.deleteLikeOrDislike(id, userId);
    }

    public boolean isLegalReviewId(Long reviewId) {
        return !isIncorrectId(reviewId) && reviewStorage.isLegalId(reviewId);
    }

    private boolean isIncorrectId(long id) {
        return id <= 0;
    }
}

