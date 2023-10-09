package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {

    public void addFilm(long film_id, String name);

    public void updateFilm(long film_id, String name);

    public void deleteFilm(long film_id, String name);

    public void addUser(long user_id, String login);

    public void updateUser(long user_id, String login);

    public void deleteUser(long user_id, String login);

    public void addLike(long film_id, String name, long user_id, String login);

    public void deleteLike(long film_id, String name, long user_id, String login);

    public void addFeedback(long film_id, String name, long user_id, String login);

    public void deleteFeedback(long film_id, String name, long user_id, String login);

    public void updateFeedback(long film_id, String name, long user_id, String login);

    public void addFriendRequest(long friend_id, String friend_login, long user_id, String login);

    public void deleteFriendRequest(long friend_id, String friend_login, long user_id, String login);

    public void acceptFriendRequest(long friend_id, String friend_login, long user_id, String login);

    public void addRecommendation(long film_id, String name, long friend_id, String friend_login, long user_id, String login);

    public void deleteRecommendation(long film_id, String name,  long friend_id, String friend_login, long user_id, String login);

    public List<Event> getEventsList(int cnt, Event.Operation operation, Event.Object object);

}
