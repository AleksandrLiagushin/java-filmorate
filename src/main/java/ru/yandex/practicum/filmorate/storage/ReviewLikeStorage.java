package ru.yandex.practicum.filmorate.storage;

public interface ReviewLikeStorage {
    void addLike(long reviewId, long userId);

    void addDislike(long reviewId, long userId);

    void deleteLikeOrDislike(long reviewId, long userId);

    int getUsability(long reviewId);

}
