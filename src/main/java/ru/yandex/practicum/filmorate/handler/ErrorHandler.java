package ru.yandex.practicum.filmorate.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.InvalidPathVariableException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.WrongIdException;

import java.util.Map;

@Slf4j
@RestControllerAdvice("ru.yandex.practicum.filmorate.controller")
public class ErrorHandler {
    @ExceptionHandler({InvalidPathVariableException.class, ValidationException.class,})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidPathVariableException(final Exception e) {
        log.error(e.getMessage(), e);
        return Map.of("Error", e.getMessage());
    }

    @ExceptionHandler({WrongIdException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> wrongUserIdException(final Exception e) {
        log.error(e.getMessage(), e);
        return Map.of("Error", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> serverInternalError(final Exception e) {
        log.error(e.getMessage(), e);
        return Map.of("Internal server error", e.getMessage());
    }

}
