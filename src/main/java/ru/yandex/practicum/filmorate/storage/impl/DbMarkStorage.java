package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.MarkStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DbMarkStorage implements MarkStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addMark(long userId, long filmId, int mark) {
        String sql = "insert into film_mark(user_id, film_id, mark) values(?, ?, ?)";
        jdbcTemplate.update(sql, userId, filmId, mark);
    }

    @Override
    public void deleteMark(long userId, long filmId) {
        String sql = "delete film_mark where user_id = ? and film_id = ?";
        jdbcTemplate.update(sql, userId, filmId);
    }

    @Override
    public Map<Long, Integer> getMarksByFilmId(Long filmId) {
        String sql = "select user_id, mark from film_mark where film_id = ?";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmId);

        Map<Long, Integer> filmMarks = new HashMap<>();

        for (Map<String, Object> row : rows) {
            long userId = (long) row.get("user_id");
            int mark = (int) row.get("mark");
            filmMarks.put(userId, mark);
        }

        return filmMarks;
    }

    @Override
    public Map<Long, Integer> getMarksByUserId(Long userId) {
        String sql = "select film_id, mark from film_mark where user_id = ?";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId);

        Map<Long, Integer> filmMarks = new HashMap<>();

        for (Map<String, Object> row : rows) {
            long filmId = (long) row.get("film_id");
            int mark = (int) row.get("mark");
            filmMarks.put(filmId, mark);
        }

        return filmMarks;
    }
}
