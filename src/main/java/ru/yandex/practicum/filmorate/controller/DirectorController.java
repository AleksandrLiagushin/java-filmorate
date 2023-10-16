package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director) {
        log.info("Requested creation of director");
        return directorService.addDirector(director);
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable int id) {
        log.info("Requested director {}", id);
        return directorService.getDirectorById(id);
    }

    @GetMapping
    public List<Director> getDirectors() {
        log.info("Requested all directors");
        return directorService.getDirectors();
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Requested change of director");
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public long deleteDirector(@PathVariable long id) {
        log.info("Requested deletion of director {}", id);
        return directorService.deleteDirector(id);
    }
}
