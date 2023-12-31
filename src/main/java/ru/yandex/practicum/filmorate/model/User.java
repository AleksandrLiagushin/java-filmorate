package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
public class User {
    private long id;

    @Email
    private String email;

    @NotNull
    @NotEmpty
    @NotBlank
    private String login;

    private String name;
    private LocalDate birthday;

}
