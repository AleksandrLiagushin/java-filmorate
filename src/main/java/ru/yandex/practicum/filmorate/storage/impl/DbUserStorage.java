package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
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
        user.setId(keyHolder.getKey().longValue());

        log.info("User {} added", keyHolder.getKey().intValue());

        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update("update users set name = ?, login = ?, email = ?, birthday = ?" +
                        "where id = ?",
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                java.sql.Date.valueOf(user.getBirthday()),
                user.getId());
        log.info("User {} updated", user.getId());
        return getById(user.getId()).orElse(null);
    }

    @Override
    public void delete(Long userId) {
        jdbcTemplate.update("delete from users where id = ?", userId);
    }

    @Override
    public Optional<User> getById(Long userId) {
        try {
            String sqlQuery = "select id, name, login, email, birthday from users where id=?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, this::mapper, userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
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
    public List<User> getCommonFriends(long userId, long otherId) {
        String sql = "select u.* " +
                "from friends fl1 join friends fl2 on fl1.friend_id = fl2.friend_id " +
                "join users u on fl2.friend_id = u.id " +
                "where fl1.user_id = ? and fl2.user_id = ?";
        return jdbcTemplate.query(sql, this::mapper, userId, otherId);
    }

    @Override
    public List<User> getFriendsByUserId(long userId) {
        String sql = "select u.* " +
                "from friends fl join users u on fl.friend_id = u.id " +
                "where fl.user_id = ?";
        return jdbcTemplate.query(sql, this::mapper, userId);
    }

    @Override
    public boolean existsById(long id) {
            Integer count = jdbcTemplate.queryForObject("select count(1) from users where id=?", Integer.class, id);
            return count == 1;
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