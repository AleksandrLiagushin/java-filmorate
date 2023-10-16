package ru.yandex.practicum.filmorate.storage;

public interface FriendStorage {
    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    void acceptFriendRequest(long userId, long friendId);
}
