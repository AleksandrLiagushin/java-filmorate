package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.impl.DbFilmStorage;
import ru.yandex.practicum.filmorate.storage.impl.DbUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbUserStorageTest {
    private final DbFilmStorage filmStorage;
    private final DbUserStorage userStorage;

    Mpa mpa1 = new Mpa(1, "G");
    Mpa mpa2 = new Mpa(4, "R");
    Genre genre1 = new Genre(1, "Комедия");

    User user1 = User.builder()
            .name("dolore")
            .login("NickName")
            .email("mail@mail.ru")
            .birthday(LocalDate.parse("1946-08-20"))
            .build();
    User updateUser1 = User.builder()
            .id(1)
            .name("Newdolore")
            .login("NickName")
            .email("mail@mail.ru")
            .birthday(LocalDate.parse("1946-08-20"))
            .build();
    User user2 = User.builder()
            .id(1)
            .name("Newdolore2")
            .login("NickName2")
            .email("mail2@mail.ru")
            .birthday(LocalDate.parse("1946-08-20"))
            .build();
    User user3 = User.builder()
            .name("Newdolore3")
            .login("NickName3")
            .email("mail3@mail.ru")
            .birthday(LocalDate.parse("1946-08-20"))
            .build();

    Film film1 = Film.builder()
            .name("nisi eiusmod")
            .description("adipisicing")
            .releaseDate(LocalDate.parse("1946-08-20"))
            .duration(100)
            .mpa(mpa1)
            .build();
    Film film2 = Film.builder()
            .name("New film")
            .description("New film about friends")
            .releaseDate(LocalDate.parse("1946-08-20"))
            .duration(120)
            .mpa(mpa2)
            .genres(List.of(genre1))
            .build();

    @Test
    public void addTest() {
        Film film = filmStorage.add(film1);
        filmStorage.add(film2);
        film1.setId(1);
        film2.setId(2);

        assertNotNull(film);
        assertEquals(1, film.getId());

        Optional<Film> filmId1 = filmStorage.getById(1L);

        assertNotNull(filmId1.orElse(null));
        assertEquals(1, filmId1.get().getId());

        List<Film> filmList = filmStorage.getAllFilms();

        assertNotNull(filmList);
        assertEquals(2, filmList.size());

        User user = userStorage.add(user1);
        userStorage.add(user2);
        userStorage.add(user3);
        user1.setId(1);
        user2.setId(2);
        user3.setId(3);

        assertNotNull(user);
        assertEquals(user.getName(), user1.getName());

        User userUpdate = userStorage.update(updateUser1);

        assertNotNull(userUpdate);
        assertEquals(userUpdate.getName(), updateUser1.getName());

        Optional<User> userId1 = userStorage.getById(1L);

        assertNotNull(userId1.orElse(null));
        assertEquals(userId1.get().getId(), 1);

        List<User> userListAll = userStorage.getAll();

        assertNotNull(userListAll);
        assertEquals(userListAll.size(), 3);

    }
}