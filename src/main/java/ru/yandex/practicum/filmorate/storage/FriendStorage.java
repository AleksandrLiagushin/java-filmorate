package ru.yandex.practicum.filmorate.storage;

import java.util.Set;

public interface FriendStorage {
    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    Set<Long> getFriendsByUserId(long id);
}
