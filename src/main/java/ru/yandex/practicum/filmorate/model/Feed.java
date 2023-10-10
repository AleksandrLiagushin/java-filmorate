package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class Feed {
    private long eventId;
    private Timestamp timestamp;
    private EventType eventType;
    private Operation operation;
    private long userId;
    private long entityId;

    public enum EventType {
        LIKE,
        FRIEND,
        REVIEW
    };

    public enum Operation {
        ADD,
        UPDATE,
        REMOVE
    };
}
