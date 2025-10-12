-- /usr/local/mysql/bin/mysql -u mytestuser -p < /Users/martinvu/2025-fall-cs-122b-marjoe/src/main/resources/sql/script.sql
CREATE DATABASE IF NOT EXISTS moviedb;
USE moviedb;

SET GLOBAL autocommit = 0;

SOURCE /Users/martinvu/2025-fall-cs-122b-marjoe/src/main/resources/sql/createtable.sql;
SOURCE /Users/martinvu/2025-fall-cs-122b-marjoe/src/main/resources/sql/credit_cards.sql;
SOURCE /Users/martinvu/2025-fall-cs-122b-marjoe/src/main/resources/sql/customers.sql;
SOURCE /Users/martinvu/2025-fall-cs-122b-marjoe/src/main/resources/sql/genres.sql;
SOURCE /Users/martinvu/2025-fall-cs-122b-marjoe/src/main/resources/sql/movies.sql;
SOURCE /Users/martinvu/2025-fall-cs-122b-marjoe/src/main/resources/sql/ratings.sql;
SOURCE /Users/martinvu/2025-fall-cs-122b-marjoe/src/main/resources/sql/sales.sql;
SOURCE /Users/martinvu/2025-fall-cs-122b-marjoe/src/main/resources/sql/stars.sql;
SOURCE /Users/martinvu/2025-fall-cs-122b-marjoe/src/main/resources/sql/genres_in_movies.sql;
SOURCE /Users/martinvu/2025-fall-cs-122b-marjoe/src/main/resources/sql/stars_in_movies.sql;

COMMIT;
SET GLOBAL autocommit = 1;