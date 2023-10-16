package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbUserStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User add(User user) {
        changeNameToLogin(user);
        if (isNotValid(user)) {
            throw new ValidationException("Can't create new user. Check your data.");
        }
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
        if (keyHolder.getKey() != null) {
            log.info("User {} added", Objects.requireNonNull(keyHolder.getKey()).intValue());
        }
        return user;
    }

    @Override
    public User update(User user) {
        changeNameToLogin(user);
        if (isNotValid(user)) {
            throw new ValidationException("Can't create new user. Check your data.");
        }
        int userFound = jdbcTemplate.update("update users set name = ?, login = ?, email = ?, birthday = ?" +
                        "where id = ?",
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                java.sql.Date.valueOf(user.getBirthday()),
                user.getId());
        if (userFound == 0) {
            throw new WrongIdException("No user with id = " + user.getId() + " in DB was found.");
        }
        log.info("User {} updated", user.getId());
        return user;
    }

    @Override
    public void delete(Long userId) {
        if (isIncorrectId(userId))  {
            throw new WrongIdException("Param must be more then 0");
        }
        jdbcTemplate.update("delete from users where id = ?", userId);
    }

    @Override
    public User getById(Long userID) {
        if (isIncorrectId(userID))  {
            throw new WrongIdException("Param must be more then 0");
        }
        String sqlQuery = "select id, name, login, email, birthday from users where id=?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapper, userID);
        } catch (EmptyResultDataAccessException e) {
            throw new WrongIdException("No user with id = " + userID + " in DB was found.");
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
        getById(userId);
        getById(otherId);
        String sql = "select u.* " +
                "from friends fl1 join friends fl2 on fl1.friend_id = fl2.friend_id " +
                "join users u on fl2.friend_id = u.id " +
                "where fl1.user_id = ? and fl2.user_id = ?";
        return jdbcTemplate.query(sql, this::mapper, userId, otherId);
    }

    @Override
    public List<User> getFriendsByUserId(long userId) {
        getById(userId);
        String sql = "select u.* " +
                "from friends fl join users u on fl.friend_id = u.id " +
                "where fl.user_id = ?";
        return jdbcTemplate.query(sql, this::mapper, userId);
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

    private boolean isIncorrectId(Long id) {
        return id == null || id <= 0;
    }

    private boolean isNotValid(User user) {
        return user.getLogin().contains(" ")
                || user.getBirthday().isAfter(LocalDate.now());
    }

    private void changeNameToLogin(User user) {
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            log.info("Changed blank user name to user login {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}