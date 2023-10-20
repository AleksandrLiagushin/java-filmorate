package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FriendStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendStorage friendStorage;
    private final FeedStorage feedStorage;
    private final FilmFullService filmFullService;

    public User create(User user) {
        changeNameToLogin(user);
        if (isNotValid(user)) {
            throw new ValidationException("Wrong user data");
        }

        return userStorage.add(user);
    }

    public User update(User user) {
        changeNameToLogin(user);
        if (isNotValid(user)) {
            throw new ValidationException("Wrong user data");
        }
        if (!existsById(user.getId())) {
            throw new WrongIdException("No users with id = " + user.getId() + " in DB were found.");
        }

        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public void deleteUserById(long id) {
        if (isIncorrectId(id)) {
            throw new WrongIdException("Param must be more then 0");
        }
        userStorage.delete(id);
    }

    public void addFriend(long userId, long friendId) {
        if (!existsById(userId) || !existsById(friendId)) {
            throw new WrongIdException("No users with id = " + userId + " or " + friendId + " in DB were found.");
        }
        friendStorage.addFriend(userId, friendId);
        feedStorage.addFriendRequest(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        if (!existsById(userId) || !existsById(friendId)) {
            throw new WrongIdException("No users with id = " + userId + " or " + friendId + " in DB were found.");
        }

        friendStorage.deleteFriend(userId, friendId);
        feedStorage.deleteFriendRequest(userId, friendId);
    }

    public void updateFriendRequest(long userId, long friendId) {
        if (!existsById(userId) || !existsById(friendId)) {
            throw new WrongIdException("No users with id = " + userId + " or " + friendId + " in DB were found.");
        }

        friendStorage.acceptFriendRequest(userId, friendId);
        feedStorage.acceptFriendRequest(userId, friendId);
    }

    public List<User> findCommonFriends(long userId, long otherId) {
        if (!existsById(userId) || !existsById(otherId)) {
            throw new WrongIdException("No users with id = " + userId + " or " + otherId + " in DB were found.");
        }

        return userStorage.getCommonFriends(userId, otherId);
    }

    public List<Feed> getEventsList(long userId) {
        if (!existsById(userId)) {
            throw new WrongIdException("No user with id = " + userId + " in DB was found.");
        }

        return feedStorage.getFeed(userId);
    }

    public User getById(long userId) {
        if (isIncorrectId(userId)) {
            throw new WrongIdException("Param must be more then 0");
        }
        Optional<User> userOpt = userStorage.getById(userId);

        return userOpt.orElseThrow(() -> new WrongIdException("No user with id = " + userId + " in DB was found."));
    }

    public List<User> getFriends(long userId) {
        if (!existsById(userId)) {
            throw new WrongIdException("No user with id = " + userId + " in DB was found.");
        }

        return userStorage.getFriendsByUserId(userId);
    }

    public List<Film> getRecommendations(long userId) {
        if (!existsById(userId)) {
            throw new WrongIdException("No user with id = " + userId + " in DB was found.");
        }

        return filmFullService.getRecommendations(userId);
    }

    public boolean existsById(long userId) {
        return !isIncorrectId(userId) && userStorage.existsById(userId);
    }

    private boolean isIncorrectId(long id) {
        return id <= 0;
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