CREATE TABLE movies (
    id VARCHAR(10) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    director VARCHAR(100) NOT NULL
);

CREATE TABLE stars (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    birth_year INT
);

CREATE TABLE stars_in_movies (
    star_id VARCHAR(10),
    movie_id VARCHAR(10),
    PRIMARY KEY (star_id, movie_id),
    FOREIGN KEY (star_id) REFERENCES stars(id),
    FOREIGN KEY (movie_id) REFERENCES movies(id)
);

CREATE TABLE genres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name varchar(32) NOT NULL
);

CREATE TABLE genres_in_movies (
    genre_id INT,
    movie_id VARCHAR(10),
    PRIMARY KEY (genre_id, movie_id),
    FOREIGN KEY (genre_id) REFERENCES genres(id),
    FOREIGN KEY (movie_id) REFERENCES movies(id)
);
