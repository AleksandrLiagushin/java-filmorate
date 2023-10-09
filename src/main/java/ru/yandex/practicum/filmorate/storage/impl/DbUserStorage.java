package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.WrongUserIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class DbUserStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final EventStorage eventStorage;

    @Override
    public User add(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlQuery = "insert into users (name, login, email, birthday) values (?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getEmail());
            stmt.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        Set<Long> friends = user.getFriends();
        if (friends != null) {
            friends.forEach(friendId -> jdbcTemplate.update(
                    "insert into friends (user_id, friend_id) values (?, ?)",
                    keyHolder.getKey().longValue(),
                    friendId));
        }
        eventStorage.addUser(user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        int userFound = jdbcTemplate.update("update users set name = ?, login = ?, email = ?, birthday = ?" +
                        "where id = ?",
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                java.sql.Date.valueOf(user.getBirthday()),
                user.getId());
        if (userFound == 0) {
            throw new WrongUserIdException("No user with id = " + user.getId() + " in DB was found.");
        }
        jdbcTemplate.update("delete from friends where user_id = ?", user.getId());
        Set<Long> friends = user.getFriends();
        if (friends != null) {
            friends.forEach(friendId -> jdbcTemplate.update(
                    "insert into friends (user_id, friend_id) values (?, ?)",
                    user.getId(),
                    friendId));
        }
        eventStorage.updateUser(user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User delete(User user) {
        jdbcTemplate.update("delete from users where id = ? cascade", user.getId());
        eventStorage.deleteUser(user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User getById(Long userID) {
        String sqlQuery = "select id, name, login, email, birthday from users where id=?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapper, userID);
        } catch (EmptyResultDataAccessException e) {
            throw new WrongUserIdException("No user with id = " + userID + " in DB was found.");
        }
    }

    @Override
    public List<User> getAll() {
        return jdbcTemplate.query(
                "select id, name, login, email, birthday from users",
                this::mapper
        );
    }

    private User mapper(ResultSet resultSet, int rowNum) throws SQLException {

        Set<Long> friendIds = new HashSet<>(jdbcTemplate.query(
                "select friend_id from friends where user_id = ?",
                (resultSetLike, rowNumLike) -> resultSetLike.getLong(1),
                resultSet.getLong(1)
        ));

        return User.builder()
                .id(resultSet.getLong("users.id"))
                .name(resultSet.getString("users.name"))
                .login(resultSet.getString("users.login"))
                .email(resultSet.getString("users.email"))
                .birthday(resultSet.getDate("users.birthday").toLocalDate())
                .friends(friendIds)
                .build();
    }
}
