package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.WrongUserIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class DbUserStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

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

        return user;
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("delete from users where id = ?", id);
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

    @Override
    public List<User> getCommonFriendsByUserId(long userId, long otherId) {
        String sql = "select u.* " +
                "from friends fl1 join friends fl2 on fl1.friend_id = fl2.friend_id " +
                "join users u on fl2.friend_id = u.id " +
                "where fl1.user_id = ? and fl2.user_id = ?";
        return jdbcTemplate.query(sql, this::mapper, userId, otherId);
    }

    private User mapper(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .login(resultSet.getString("login"))
                .email(resultSet.getString("email"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
    }
}
