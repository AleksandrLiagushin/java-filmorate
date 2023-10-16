package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FriendStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendStorage friendStorage;
    private final LikeStorage likeStorage;
    private final FilmStorage filmStorage;
    private final FeedStorage feedStorage;
    private final JdbcTemplate jdbcTemplate;

    public User create(User user) {
        userStorage.add(user);
        return user;
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public void deleteUserById(long id) {
        userStorage.delete(id);
    }

    public void addFriend(long userId, long friendId) {
        friendStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        friendStorage.deleteFriend(userId, friendId);
    }

    public void updateFriendRequest(long userId, long friendId) {
        friendStorage.acceptFriendRequest(userId, friendId);
    }

    public List<User> findCommonFriends(long userId, long otherId) {
        return userStorage.getCommonFriendsByUserId(userId, otherId);
    }

    public List<Feed> getEventsList(long userId) {
        return feedStorage.getFeedList(userId);
    }

    public User getById(long userId) {
        return userStorage.getById(userId);
    }

    public List<User> getFriends(long userId) {
        return userStorage.getFriendsByUserId(userId);
    }

    public List<Film> getRecommendations(long userId) {
        return filmStorage.getRecommendations(userId);
    }

    public List<Film> getRecommendations(long id) {
        User user = userStorage.getById(id);
        Set<Long> likedFilms = likeStorage.getLikesByUserId(id);
        List<User> commonUsers = new ArrayList<>();

        if (likedFilms.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = "select user_id from film_like group by user_id";
        List<Long> userIds = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"));

        for (Long userId : userIds) {
            User anotherUser = userStorage.getById(userId);
            if (!getCommonFilmLikes(user, anotherUser).isEmpty() && !anotherUser.equals(user)) {
                commonUsers.add(anotherUser);
            }
        }

        List<Film> recommendedFilms = new ArrayList<>();

        for (User u : commonUsers) {
            String sqlLikes = "select film_id from film_like where user_id = ? and film_id not in (" + likedFilms
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",")) + ")";
            recommendedFilms.addAll(jdbcTemplate.query(sqlLikes, this::mapper, u.getId()));
        }

        return new ArrayList<>(new HashSet<>(recommendedFilms));
    }

    private Set<Long> getCommonFilmLikes(User user, User anotherUser) {
        Set<Long> likedFilms = likeStorage.getLikesByUserId(user.getId());
        likedFilms.retainAll(likeStorage.getLikesByUserId(anotherUser.getId()));
        return likedFilms;
    }

    private boolean isNotValid(User user) {
        return user.getLogin().contains(" ")
                || user.getBirthday().isAfter(LocalDate.now());
    }

    private void changeNameToLogin(User user) {
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            log.info("Changed user name to user login");
            user.setName(user.getLogin());
        }
    }

    private boolean isIncorrectId(long id) {
        return id <= 0;
    }

    private Film mapper(ResultSet resultSet, int rowNum) throws SQLException {
        return filmStorage.getById(resultSet.getLong("film_id"));
    }

}
