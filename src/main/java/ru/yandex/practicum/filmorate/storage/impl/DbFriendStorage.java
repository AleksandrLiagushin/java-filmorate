package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.WrongUserIdException;
import ru.yandex.practicum.filmorate.storage.FriendStorage;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class DbFriendStorage implements FriendStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addFriend(long userId, long friendId) {
        jdbcTemplate.update(
                "insert into friends (user_id, friend_id) values (?, ?)",
                userId,
                friendId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        jdbcTemplate.update(
                "delete from friends where user_id = ? and friend_id = ?",
                userId,
                friendId);
    }

    @Override
    public Set<Long> getFriendsByUserId(long id) {
        try {
            jdbcTemplate.queryForObject("select id from users where id = ?", Long.class, id);
        } catch (EmptyResultDataAccessException e) {
            throw new WrongUserIdException("No user found with id = " + id);
        }
        return new HashSet<>(jdbcTemplate.query(
                "select friend_id from friends where user_id = ?",
                (resultSetLike, rowNumLike) -> resultSetLike.getLong("friends.friend_id"),
                id));
    }
}
