package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> getUsers() {
        log.info("Requested all users");
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable long id) {
        log.info("Requested user {}", id);
        return userService.getById(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        log.info("Requested common friend list between user {} and other user {}", id, otherId);
        return userService.findCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable long id) {
        log.info("Requested user {} friends list", id);
        return userService.getFriends(id);
    }

    @PostMapping
    public User post(@Valid @RequestBody User user) {
        log.info("Requested creation of user");
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Requested change of user");
        return userService.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Requested creation of friend {} to user {}", friendId, id);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Requested deletion of friend {} from user {}", friendId, id);
        userService.deleteFriend(id, friendId);
    }

    @PostMapping("/{id}/friends/{friendId}")
    public void acceptFriendRequest(@PathVariable long id, @PathVariable long friendId) {
        log.info("Accept request for friendship with user {} from user {}", friendId, id);
        userService.updateFriendRequest(id, friendId);
    }

    @GetMapping("/{id}/feed")
    public List<Feed> getEventsList(@PathVariable long id) {
        log.info("Get events for user {}", id);
        return userService.getEventsList(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable long id) {
        log.info("Requested deleting user with id = {}", id);
        userService.deleteUserById(id);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable long id) {
        log.info("Requested recommendations for user {}", id);
        return userService.getRecommendations(id);
    }
}
