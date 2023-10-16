package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Feed {
    private final long eventId;
    private final long timestamp;
    private final EventType eventType;
    private final Operation operation;
    private final long userId;
    private final long entityId;

    public enum EventType {
        LIKE,
        FRIEND,
        REVIEW
    }

    public enum Operation {
        ADD,
        UPDATE,
        REMOVE
    }
}