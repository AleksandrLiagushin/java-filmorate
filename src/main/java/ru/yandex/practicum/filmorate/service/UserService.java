package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.WrongUserIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendStorage friendStorage;

    public User create(User user) {

        changeNameToLogin(user);

        if (isNotValid(user)) {
            throw new ValidationException("Can't create new user. Check your data.");
        }

        userStorage.add(user);
        log.info("New user added {}", user);
        return user;
    }

    public User update(User user) {

        changeNameToLogin(user);

        if (isNotValid(user)) {
            throw new ValidationException("Can't update user. Check your data.");
        }

        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public void delete(long id) {
        if (isIncorrectId(id)) {
            throw new WrongUserIdException("Bad user id = " + id + ". Must be more than 0");
        }

        userStorage.delete(id);
    }

    public void addFriend(long userId, long friendId) {
        if (isIncorrectId(userId) || isIncorrectId(friendId)) {
            throw new WrongUserIdException("Param must be more then 0");
        }

        friendStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        if (isIncorrectId(userId) || isIncorrectId(friendId)) {
            throw new WrongUserIdException("Param must be more then 0");
        }

        friendStorage.deleteFriend(userId, friendId);
    }

    public List<User> findCommonFriends(long userId, long otherId) {
        if (isIncorrectId(userId) || isIncorrectId(otherId)) {
            throw new WrongUserIdException("Param must be more then 0");
        }

        return userStorage.getCommonFriendsByUserId(userId, otherId);
    }

    public User getById(long userId) {
        if (isIncorrectId(userId)) {
            throw new WrongUserIdException("Param must be more then 0");
        }

        return userStorage.getById(userId);
    }

    public List<User> getFriends(long userId) {
        if (isIncorrectId(userId)) {
            throw new WrongUserIdException("Param must be more then 0");
        }

        return friendStorage.getFriendsByUserId(userId).stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    private boolean isNotValid(User user) {
        return user.getLogin().contains(" ")
                || user.getBirthday().isAfter(LocalDate.now());
    }

    public void deleteUser(long id) {
        userStorage.delete(id);
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
}
