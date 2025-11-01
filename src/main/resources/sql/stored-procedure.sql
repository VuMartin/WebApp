DELIMITER //

CREATE PROCEDURE add_movie (
    IN in_title VARCHAR(100),
    IN in_year INT,
    IN in_director VARCHAR(100),
    IN in_star_name VARCHAR(100),
    IN in_genre_name VARCHAR(100)
)
BEGIN
    DECLARE movie_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id VARCHAR(10);
    DECLARE existing_movie INT;
    DECLARE existing_star INT;
    DECLARE existing_genre INT;

    SELECT COUNT(*) INTO existing_movie
    FROM movies
    WHERE title = in_title AND year = in_year AND director = in_director;

    IF existing_movie > 0 THEN
        SELECT CONCAT('Movie "', in_title, '" already exists.') AS message;
        LEAVE BEGIN;
    END IF;

        -- Generate new movie ID (example using max + 1)
    SELECT CONCAT('tt', LPAD(IFNULL(MAX(CAST(SUBSTRING(id,3) AS UNSIGNED)),0) + 1, 7,'0')) INTO movie_id
    FROM movies;

    INSERT INTO movies (id, title, year, director)
    VALUES (movie_id, in_title, in_year, in_director);

    SELECT id INTO star_id
    FROM stars
    WHERE name = in_star_name AND (birthYear = in_star_birth OR in_star_birth IS NULL)
    LIMIT 1;
    IF star_id IS NULL THEN
        SELECT CONCAT('nm', LPAD(IFNULL(MAX(CAST(SUBSTRING(id,3) AS UNSIGNED)),0) + 1, 7,'0')) INTO star_id
        FROM stars;
        INSERT INTO stars (id, name, birthYear)
        VALUES (star_id, in_star_name, in_star_birth);
    END IF;

    INSERT INTO stars_in_movies (star_id, movie_id)
    VALUES (star_id, movie_id);

    SELECT id INTO genre_id
    FROM genres
    WHERE name = in_genre_name
    LIMIT 1;

    IF genre_id IS NULL THEN
        SELECT IFNULL(MAX(id), 0) + 1 INTO genre_id
        FROM genres;

        INSERT INTO genres (id, name)
        VALUES (genre_id, in_genre_name);
    END IF;

    INSERT INTO genres_in_movies (genre_id, movie_id)
    VALUES (genre_id, movie_id);

    SELECT CONCAT('Movie "', in_title, '" added successfully with star "', in_star_name, '" and genre "', in_genre_name, '".') AS message;
END //

DELIMITER ;