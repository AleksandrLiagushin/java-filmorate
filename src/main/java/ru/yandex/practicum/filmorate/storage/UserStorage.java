package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User add(User user);

    User update(User user);

    void delete(long id);

    User getById(Long userID);

    List<User> getAll();

    List<User> getCommonFriendsByUserId(long userId, long otherId);

}
