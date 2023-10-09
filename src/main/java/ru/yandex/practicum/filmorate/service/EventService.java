package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidPathVariableException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.util.List;

@Slf4j
@Service
public class EventService {
    private final EventStorage eventStorage;

    @Autowired
    public EventService(EventStorage eventStorage) {
        this.eventStorage = eventStorage;
    }

    public List<Event> getEventsList(int count, String operation, String object) {
        try {
            Event.Operation operation_enum = Event.Operation.valueOf(operation);
            Event.Object object_enum = Event.Object.valueOf(object);
            return eventStorage.getEventsList(count, operation_enum, object_enum);
        } catch (RuntimeException re) {
            throw new InvalidPathVariableException("Неверно заданы параметры для просмотра ленты событий.");
        }
    }
}
