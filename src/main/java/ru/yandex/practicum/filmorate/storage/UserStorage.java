package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User add(User user);

    User update(User user);

    void delete(Long userId);

    Optional<User> getById(Long userID);

    List<User> getAll();

    List<User> getCommonFriends(long userId, long otherId);

    List<User> getFriendsByUserId(long userId);

    boolean existsById(long id);
}
