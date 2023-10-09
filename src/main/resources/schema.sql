CREATE TABLE films (
    id int PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name varchar(200),
    description varchar(200),
    release_date date,
    duration int,
    rating int
);

CREATE TABLE users (
    id int PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name varchar(200) NOT NULL,
    login varchar(200) NOT NULL,
    email varchar(200) NOT NULL,
    birthday date
);

CREATE TABLE ratings (
    id int PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name varchar(200)
);

CREATE TABLE genres (
    id int PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name varchar(200)
);

CREATE TABLE film_genre (
    film_id int NOT NULL,
    genre_id int NOT NULL,
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE film_like (
    film_id int NOT NULL,
    user_id int NOT NULL,
    PRIMARY KEY (film_id, user_id)
);

CREATE TABLE friends (
    user_id int NOT NULL,
    friend_id int NOT NULL,
    PRIMARY KEY (user_id, friend_id)
);

ALTER TABLE film_genre ADD CONSTRAINT fk_film_id FOREIGN KEY(film_id) REFERENCES films(id);
ALTER TABLE film_genre ADD CONSTRAINT fk_genre_id FOREIGN KEY(genre_id) REFERENCES genres(id);

ALTER TABLE film_like ADD CONSTRAINT fk_film_id_like FOREIGN KEY(film_id) REFERENCES films(id);
ALTER TABLE film_like ADD CONSTRAINT fk_user_id_like FOREIGN KEY(user_id) REFERENCES users(id);

ALTER TABLE friends ADD CONSTRAINT fk_user_id FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE friends ADD CONSTRAINT fk_friend_id FOREIGN KEY(friend_id) REFERENCES users(id);
