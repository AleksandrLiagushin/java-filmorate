package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbFeedStorage implements FeedStorage {
    private static final String INSERT_SQL =
            "insert into events(timestamp, operation, event_type, user_id, entity_id) values(?, ?, ?, ?, ?)";
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    private void addFeed(Feed.Operation operation, Feed.EventType eventType, long userId, long entityId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            stmt.setLong(1, Instant.now().toEpochMilli());
            stmt.setString(2, operation.toString());
            stmt.setString(3, eventType.toString());
            stmt.setLong(4, userId);
            stmt.setLong(5, entityId);
            return stmt;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            log.info("User {} has got an event {} {}.{} with object {}.",
                    userId, Objects.requireNonNull(keyHolder.getKey()).longValue(), operation, eventType, entityId);
        }
    }

    @Override
    public void addReview(long userId, long entityId) {
        userStorage.getById(userId);
        addFeed(Feed.Operation.ADD, Feed.EventType.REVIEW, userId, entityId);
    }

    @Override
    public void deleteReview(long userId, long entityId) {
        userStorage.getById(userId);
        addFeed(Feed.Operation.REMOVE, Feed.EventType.REVIEW, userId, entityId);
    }

    @Override
    public void updateReview(long userId, long entityId) {
        userStorage.getById(userId);
        addFeed(Feed.Operation.UPDATE, Feed.EventType.REVIEW, userId, entityId);
    }

    @Override
    public void addLike(long userId, long entityId) {
        userStorage.getById(userId);
        filmStorage.getById(entityId);
        addFeed(Feed.Operation.ADD, Feed.EventType.LIKE, userId, entityId);
    }

    @Override
    public void deleteLike(long userId, long entityId) {
        userStorage.getById(userId);
        filmStorage.getById(entityId);
        addFeed(Feed.Operation.REMOVE, Feed.EventType.LIKE, userId, entityId);
    }

    @Override
    public void addFriendRequest(long userId, long entityId) {
        userStorage.getById(userId);
        userStorage.getById(entityId);
        addFeed(Feed.Operation.ADD, Feed.EventType.FRIEND, userId, entityId);
    }

    @Override
    public void deleteFriendRequest(long userId, long entityId) {
        userStorage.getById(userId);
        userStorage.getById(entityId);
        addFeed(Feed.Operation.REMOVE, Feed.EventType.FRIEND, userId, entityId);
    }

    @Override
    public void acceptFriendRequest(long userId, long entityId) {
        userStorage.getById(userId);
        userStorage.getById(entityId);
        addFeed(Feed.Operation.UPDATE, Feed.EventType.FRIEND, userId, entityId);
    }

    @Override
    public List<Feed> getFeedList(long userId) {
        userStorage.getById(userId);
        String sql = "select e.* " +
                "from events e " +
                "where e.user_id = ? " +
                "order by e.id asc";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapper(rs), userId);
    }

    private Feed mapper(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong("id");
        long timestamp = resultSet.getLong("timestamp");
        Feed.Operation operation = Feed.Operation.valueOf(resultSet.getString("operation"));
        Feed.EventType eventType = Feed.EventType.valueOf(resultSet.getString("event_type"));
        long userId = resultSet.getLong("user_id");
        long entityId = resultSet.getLong("entity_id");

        return Feed.builder()
                .eventId(id)
                .timestamp(timestamp)
                .operation(operation)
                .eventType(eventType)
                .userId(userId)
                .entityId(entityId)
                .build();
    }
}
