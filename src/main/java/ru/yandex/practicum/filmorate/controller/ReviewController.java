package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Requested creation of review");
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Requested change of review");
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable long id) {
        log.info("Requested deletion of review {}", id);
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable long id) {
        log.info("Requested review {}", id);
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getReviewsByFilmId(@RequestParam(defaultValue = "-1") long filmId,
                                           @RequestParam(defaultValue = "10") int count) {
        log.info("Requested {} reviews for film {}", count, filmId);
        if (filmId == -1) {
            return reviewService.getAllReviews();
        }

        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("{id}/like/{userId}")
    public void addLikeToReview(@PathVariable long id, @PathVariable long userId) {
        log.info("Requested addition of like for review {} from user {}", id, userId);
        reviewService.addLikeToReview(id, userId);
    }

    @PutMapping("{id}/dislike/{userId}")
    public void addDislikeToReview(@PathVariable long id, @PathVariable long userId) {
        log.info("Requested addition of dislike for review {} from user {}", id, userId);
        reviewService.addDislikeToReview(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void deleteLikeFromReview(@PathVariable long id, @PathVariable long userId) {
        log.info("Requested deletion of like for review {} from user {}", id, userId);
        reviewService.deleteLikeOrDislike(id, userId);
    }

    @DeleteMapping("{id}/dislike/{userId}")
    public void deleteDislikeFromReview(@PathVariable long id, @PathVariable long userId) {
        log.info("Requested deletion of dislike for review {} from user {}", id, userId);
        reviewService.deleteLikeOrDislike(id, userId);
    }

}

