package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private static String INSERT_SQL = "insert into events(date, operation, object, description) values(?, ?, ?, ?)";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addFilm(long film_id, String name) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.INSERT, Event.Object.FILMS,
                String.format("Добавлен фильм %d: %s.", film_id, name));
    }

    @Override
    public void updateFilm(long film_id, String name) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.UPDATE, Event.Object.FILMS,
                String.format("Изменен фильм %d: %s.", film_id, name));
    }

    @Override
    public void deleteFilm(long film_id, String name) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.DELETE, Event.Object.FILMS,
                String.format("Удален фильм %d: %s.", film_id, name));
    }

    @Override
    public void addUser(long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.INSERT, Event.Object.USERS,
                String.format("Добавлен пользователь %d_%s.", user_id, login));
    }

    @Override
    public void updateUser(long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.UPDATE, Event.Object.USERS,
                String.format("Изменен пользователь %d_%s.", user_id, login));
    }

    @Override
    public void deleteUser(long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.DELETE, Event.Object.USERS,
                String.format("Удален пользователь %d_%s.", user_id, login));
    }

    @Override
    public void addFeedback(long film_id, String name, long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.INSERT, Event.Object.FEEDBACKS,
                String.format("Добавлен отзыв пользователь %d_%s на фильм %d: %s.", user_id, login, film_id, name));
    }

    @Override
    public void deleteFeedback(long film_id, String name, long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.DELETE, Event.Object.FEEDBACKS,
                String.format("Удален отзыв пользователь %d_%s на фильм %d: %s.", user_id, login, film_id, name));
    }

    @Override
    public void updateFeedback(long film_id, String name, long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.UPDATE, Event.Object.FEEDBACKS,
                String.format("Изменен отзыв пользователь %d_%s на фильм %d: %s.", user_id, login, film_id, name));
    }

    @Override
    public void addLike(long film_id, String name, long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.INSERT, Event.Object.FILM_LIKE,
                String.format("Добавлен лайк от пользователя %d_%s фильму %d: %s.", user_id, login, film_id, name));
    }

    @Override
    public void deleteLike(long film_id, String name, long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.DELETE, Event.Object.FILM_LIKE,
                String.format("Удален лайк от пользователя %d_%s фильму %d: %s.", user_id, login, film_id, name));
    }

    @Override
    public void addFriendRequest(long friend_id, String friend_login, long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.INSERT, Event.Object.FRIENDSHIP_REQUEST,
                String.format("Добавлен запрос пользователя %d_%s в друзья пользователю %d_%s.", user_id, login,
                        friend_id, friend_login));
    }

    @Override
    public void deleteFriendRequest(long friend_id, String friend_login, long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.DELETE, Event.Object.FRIENDSHIP_REQUEST,
                String.format("Удален запрос пользователя %d_%s в друзья пользователю %d_%s.", user_id, login,
                        friend_id, friend_login));
    }

    @Override
    public void acceptFriendRequest(long friend_id, String friend_login, long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, LocalDateTime.now(), Event.Operation.UPDATE, Event.Object.FRIENDSHIP_REQUEST,
                String.format("Принят запрос пользователя %d_%s в друзья пользователю %d_%s.", user_id, login,
                        friend_id, friend_login));
    }

    @Override
    public void addRecommendation(long film_id, String name,
                                  long friend_id, String friend_login,
                                  long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, Event.Operation.INSERT, Event.Object.RECOMMENDATIONS,
                String.format("Добалена рекомендация пользователя %d_%s пользователю %d_%s фильма %d: %s.",
                        user_id, login, friend_id, friend_login, film_id, name));
    }

    @Override
    public void deleteRecommendation(long film_id, String name,
                                     long friend_id, String friend_login,
                                     long user_id, String login) {
        jdbcTemplate.update(INSERT_SQL, Event.Operation.DELETE, Event.Object.RECOMMENDATIONS,
                String.format("Удалена рекомендация пользователя %d_%s пользователю %d_%s фильма %d: %s.",
                        user_id, login, friend_id, friend_login, film_id, name));
    }

    @Override
    public List<Event> getEventsList(int count, Event.Operation operation, Event.Object object) {
        String str_operation = operation.toString();
        String str_object = object.toString();
        String sql = "select e.* " +
                "from events " +
                "where e.operation = nvl(?, e.operation) and e.object = nvl(?, e.object) " +
                "order by e.date desc ";
        if (count > 0) {
            sql += "limit ?";
        }
        return jdbcTemplate.query(sql, (ResultSet, rowNum) -> mapper(ResultSet), str_operation, str_object, count);
    }

    private Event mapper(ResultSet resultSet) throws SQLException {
        long id = resultSet.getInt("id");
        Event.Operation operation = Event.Operation.valueOf(resultSet.getString("operation"));
        Event.Object object = Event.Object.valueOf(resultSet.getString("object"));
        LocalTime dbSqlTime = resultSet.getTime("date").toLocalTime();
        LocalDate dbSqlDate = resultSet.getDate("date").toLocalDate();
        LocalDateTime date = LocalDateTime.of(dbSqlDate, dbSqlTime);
        String description = resultSet.getString("description");

        return Event.builder()
                .id(id)
                .date(date)
                .operation(operation)
                .object(object)
                .description(description)
                .build();
    }
}
