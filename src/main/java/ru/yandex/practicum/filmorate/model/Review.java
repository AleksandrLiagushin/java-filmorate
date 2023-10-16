package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Review {
    private long reviewId;

    @NotNull
    @NotBlank
    @Size(max = 500)
    private String content;

    @NotNull
    private Boolean isPositive;

    @NotNull
    private Long userId;

    @NotNull
    private Long filmId;
    private int useful;

    public boolean isIsPositive() {
        return isPositive;
    }

}
