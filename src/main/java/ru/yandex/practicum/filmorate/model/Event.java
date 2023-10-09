package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Event {
    private long id;
    private LocalDateTime date;
    private Object object;
    private Operation operation;
    private String description;

    public enum Object {
        USERS,
        FILMS,
        FILM_LIKE,
        FRIENDSHIP_REQUEST,
        RECOMMENDATIONS,
        FEEDBACKS
    };

    public enum Operation {
        INSERT,
        UPDATE,
        DELETE
    };
}
