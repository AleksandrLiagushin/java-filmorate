package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbFeedStorage implements FeedStorage {
    private static final String INSERT_SQL =
            "insert into events(timestamp, operation, event_type, user_id, entity_id) values(?, ?, ?, ?, ?)";
    private final JdbcTemplate jdbcTemplate;

    private void addFeed(Operation operation, EventType eventType, long userId, long entityId) {
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

        log.info("User {} has got an event {} {}.{} with object {}.",
                userId, keyHolder.getKey().longValue(), operation, eventType, entityId);
    }

    @Override
    public void addReview(long userId, long entityId) {
        addFeed(Operation.ADD, EventType.REVIEW, userId, entityId);
    }

    @Override
    public void deleteReview(long userId, long entityId) {
        addFeed(Operation.REMOVE, EventType.REVIEW, userId, entityId);
    }

    @Override
    public void updateReview(long userId, long entityId) {
        addFeed(Operation.UPDATE, EventType.REVIEW, userId, entityId);
    }

    @Override
    public void addMark(long userId, long entityId) {
        addFeed(Operation.ADD, EventType.MARK, userId, entityId);
    }

    @Override
    public void deleteMark(long userId, long entityId) {
        addFeed(Operation.REMOVE, EventType.MARK, userId, entityId);
    }

    @Override
    public void addFriendRequest(long userId, long entityId) {
        addFeed(Operation.ADD, EventType.FRIEND, userId, entityId);
    }

    @Override
    public void deleteFriendRequest(long userId, long entityId) {
        addFeed(Operation.REMOVE, EventType.FRIEND, userId, entityId);
    }

    @Override
    public void acceptFriendRequest(long userId, long entityId) {
        addFeed(Operation.UPDATE, EventType.FRIEND, userId, entityId);
    }

    @Override
    public List<Feed> getFeed(long userId) {
        String sql = "select e.* " +
                "from events e " +
                "where e.user_id = ? " +
                "order by e.id asc";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapper(rs), userId);
    }

    private Feed mapper(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong("id");
        long timestamp = resultSet.getLong("timestamp");
        Operation operation = Operation.valueOf(resultSet.getString("operation"));
        EventType eventType = EventType.valueOf(resultSet.getString("event_type"));
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
