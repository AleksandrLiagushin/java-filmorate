package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FriendStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@Repository
@RequiredArgsConstructor
public class DbFriendStorage implements FriendStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final FeedStorage feedStorage;

    @Override
    public void addFriend(long userId, long friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
        jdbcTemplate.update(
                "insert into friends (user_id, friend_id) values (?, ?)", userId, friendId);
        feedStorage.addFriendRequest(userId, friendId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
        jdbcTemplate.update(
                "delete from friends where user_id = ? and friend_id = ?", userId, friendId);
        feedStorage.deleteFriendRequest(userId, friendId);
    }

    @Override
    public void acceptFriendRequest(long userId, long friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
        jdbcTemplate.update("update friends set status = true where user_id = ? and friend_id = ?", userId, friendId);
        feedStorage.acceptFriendRequest(userId, friendId);
    }
}