package main.java.org.example.servlets.customer.MovieList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import main.java.org.example.Pojo.GenrePojo;
import main.java.org.example.Pojo.MovieListPojo;
import main.java.org.example.Pojo.MoviePojo;
import main.java.org.example.Pojo.StarPojo;
import main.java.org.example.utils.Utils;

public class MySQLMovieListRetriever implements MovieListRetriever {

    private static final String DROP_TEMP =
            "DROP TEMPORARY TABLE IF EXISTS temp_movies";

    private static final String CREATE_TEMP =
            "CREATE TEMPORARY TABLE temp_movies ( " +
                    "sort_order INT AUTO_INCREMENT PRIMARY KEY, " +
                    "movie_id VARCHAR(10) NOT NULL, " +
                    "rating FLOAT )";

    private static final String INSERT_TEMP =
            "INSERT INTO temp_movies (movie_id, rating) " +
                    "SELECT m.id, r.rating " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movie_id " +
                    "WHERE 1=1 ";

    private static final String MOVIES_SQL =
            "SELECT m.id, m.title, m.year, m.director, t.rating " +
                    "FROM temp_movies t " +
                    "JOIN movies m ON m.id = t.movie_id " +
                    "ORDER BY t.sort_order";

    private static final String GENRES_SQL =
                        "SELECT gm.movie_id, " +
                            "(SELECT GROUP_CONCAT(CONCAT(g_sub.name, ', ', g_sub.id) SEPARATOR ', ') " +
                            "FROM " +
                                "(SELECT g.name, g.id " +
                                    "FROM genres g " +
                                    "JOIN genres_in_movies gm2 ON g.id = gm2.genre_id " +
                                    "WHERE gm2.movie_id = gm.movie_id " +
                                    "ORDER BY g.name " +
                                    "LIMIT 3) AS g_sub) AS genres " +
                        "FROM genres_in_movies gm " +
                        "JOIN temp_movies t ON t.movie_id = gm.movie_id " +
                        "GROUP BY gm.movie_id";
//    private static final String GENRES_SQL =
//            "SELECT t.movie_id, " +
//                    "GROUP_CONCAT(g.name ORDER BY g.name SEPARATOR ', ' LIMIT 3) AS genres " +
//                    "FROM temp_movies t " +  // Start with temp_movies
//                    "JOIN genres_in_movies gm ON t.movie_id = gm.movie_id " +
//                    "JOIN genres g ON gm.genre_id = g.id " +
//                    "GROUP BY t.movie_id";

    private static final String STARS_SQL =
                    "SELECT t.movie_id, " +
                            "       (SELECT GROUP_CONCAT(CONCAT(stars_sub.name, ', ', stars_sub.id) SEPARATOR ', ') " +
                            "        FROM (SELECT s.name, s.id " +
                            "              FROM stars s " +
                            "              JOIN stars_in_movies sm ON s.id = sm.star_id " +
                            "              JOIN (SELECT star_id, COUNT(*) as movie_count " +
                            "                    FROM stars_in_movies " +
                            "                    GROUP BY star_id) AS star_counts ON s.id = star_counts.star_id " +
                            "              WHERE sm.movie_id = t.movie_id " +
                            "              ORDER BY star_counts.movie_count DESC, s.name ASC " +
                            "              LIMIT 3) AS stars_sub) AS stars " +
                            "FROM temp_movies t";

//    String starsQuery =  // mariadb
//            "SELECT t.movie_id, " +
//                    "GROUP_CONCAT(CONCAT(s.name, ', ', s.id) " +
//                    "            ORDER BY star_counts.movie_count DESC, s.name ASC " +
//                    "            SEPARATOR ', ' LIMIT 3) AS stars " +
//                    "FROM temp_movies t " +
//                    "JOIN stars_in_movies sm ON t.movie_id = sm.movie_id " +
//                    "JOIN stars s ON sm.star_id = s.id " +
//                    "JOIN (SELECT star_id, COUNT(*) as movie_count " +
//                    "      FROM stars_in_movies " +
//                    "      GROUP BY star_id) AS star_counts ON s.id = star_counts.star_id " +
//                    "GROUP BY t.movie_id";

    private static final String COUNT_BASE =
            "SELECT COUNT(DISTINCT m.id) AS total " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movie_id ";

    private DataSource dataSource;

    MySQLMovieListRetriever() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JsonObject getMovieList(
            String title,
            String year,
            String director,
            String genre,
            String star,
            String prefix,
            String sortPrimaryField,
            String sortPrimaryOrder,
            String sortSecondaryField,
            String sortSecondaryOrder,
            int currentPage,
            int pageSize,
            int offset
    ) throws SQLException {

        try (Connection conn = dataSource.getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.execute(DROP_TEMP);
            stmt.execute(CREATE_TEMP);
            StringBuilder topMoviesQuery = new StringBuilder(INSERT_TEMP);
            if (title != null && !title.isEmpty())
                topMoviesQuery.append("AND MATCH(m.title) AGAINST(? IN BOOLEAN MODE) ");
            if (year != null && !year.isEmpty()) topMoviesQuery.append("AND m.year = ? ");
            if (director != null && !director.isEmpty()) topMoviesQuery.append("AND m.director LIKE ? ");
            if (genre != null && !genre.isEmpty()) topMoviesQuery.append(
                    "AND m.id IN (SELECT movie_id " +
                            "FROM genres_in_movies gm JOIN genres g ON g.id = gm.genre_id " +
                            "WHERE g.name = ?) "
            );
            if (star != null && !star.isEmpty()) topMoviesQuery.append(
                    "AND m.id IN (SELECT movie_id " +
                            "FROM stars_in_movies sm JOIN stars s ON s.id = sm.star_id " +
                            "WHERE s.name LIKE ?) "
            );
            if (prefix != null && !prefix.isEmpty()) topMoviesQuery.append("AND m.title LIKE ? ");

            String primaryCol = "title".equals(sortPrimaryField) ? "m.title" : "COALESCE(r.rating, -1)";
            String primaryDir = "asc".equals(sortPrimaryOrder) ? "ASC" : "DESC";
            String secondaryCol = "title".equals(sortSecondaryField) ? "m.title" : "COALESCE(r.rating, -1)";
            String secondaryDir = "asc".equals(sortSecondaryOrder) ? "ASC" : "DESC";

            topMoviesQuery.append("ORDER BY ")
                    .append(primaryCol).append(" ").append(primaryDir)
                    .append(", ")
                    .append(secondaryCol).append(" ").append(secondaryDir)
                    .append(" ")
                    .append("LIMIT ? OFFSET ?");

            PreparedStatement statement = conn.prepareStatement(topMoviesQuery.toString());
            int index = 1;
            if (title != null && !title.isEmpty()) {
                String booleanQuery = Utils.convertToBooleanMode(title);
                statement.setString(index++, booleanQuery);
            }
            if (year != null && !year.isEmpty()) statement.setString(index++, year);
            if (director != null && !director.isEmpty()) statement.setString(index++, "%" + director + "%");
            if (genre != null && !genre.isEmpty()) statement.setString(index++, genre);
            if (star != null && !star.isEmpty()) statement.setString(index++, "%" + star + "%");
            if (prefix != null && !prefix.isEmpty()) statement.setString(index++, prefix + "%");
            statement.setInt(index++, pageSize);
            statement.setInt(index, offset);
            statement.executeUpdate();

            PreparedStatement movieStmt = conn.prepareStatement(MOVIES_SQL);
            ResultSet rs = movieStmt.executeQuery();

            PreparedStatement genreStmt = conn.prepareStatement(GENRES_SQL);
            ResultSet genresRs = genreStmt.executeQuery();

            PreparedStatement starStmt = conn.prepareStatement(STARS_SQL);
            ResultSet starsRs = starStmt.executeQuery();
            Map<String, MoviePojo> moviesMap = new LinkedHashMap<>();

            MovieListPojo movieList = MovieListPojo.builder()
                    .setCurrentPage(currentPage)
                    .setPageSize(pageSize)
                    .setSortPrimaryField(sortPrimaryField)
                    .setSortPrimaryOrder(sortPrimaryOrder)
                    .setSortSecondaryField(sortSecondaryField)
                    .setSortSecondaryOrder(sortSecondaryOrder)
                    .setPrefix(prefix);

            while (rs.next()) {
                // get a movie from result set
                String movieID = rs.getString("id");  // db column name from query
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String rating = rs.getString("rating");
                if (rating == null) rating = "N/A";

                MoviePojo movie = MoviePojo.builder()
                        .setId(movieID)
                        .setTitle(movieTitle)
                        .setYear(movieYear)
                        .setDirector(movieDirector)
                        .setRating(rating);
                moviesMap.put(movieID, movie);
            }

            while (genresRs.next()) {
                String movieID = genresRs.getString("movie_id");
                String genresStr = genresRs.getString("genres");
                MoviePojo movie = moviesMap.get(movieID);

                if (movie != null && genresStr != null) {
                    String[] parts = genresStr.split(", ");
                    for (int i = 0; i + 1 < parts.length; i += 2) {
                        movie.addGenre(
                                GenrePojo.builder()
                                        .setId(parts[i + 1])
                                        .setName(parts[i])
                        );
                    }
                }
            }

            while (starsRs.next()) {
                String movieID = starsRs.getString("movie_id");
                String stars = starsRs.getString("stars");
                MoviePojo movie = moviesMap.get(movieID);

                if (movie != null && stars != null) {
                    String[] parts = stars.split(", ");
                    for (int i = 0; i + 1 < parts.length; i += 2) {
                        movie.addStar(
                                StarPojo.builder()
                                        .setStarId(parts[i + 1])
                                        .setStarName(parts[i])
                        );
                    }
                }
            }

            for (MoviePojo movie : moviesMap.values()) {
                movieList.addMovie(movie);
            }
            return movieList.toJson();
        }
    }

    @Override
    public JsonObject getPagination(
            String title,
            String year,
            String director,
            String genre,
            String star,
            String prefix,
            JsonObject jsonObject
    ) throws SQLException {
        StringBuilder countQuery = new StringBuilder(
                "SELECT COUNT(DISTINCT m.id) AS total " +
                        "FROM movies m " +
                        "LEFT JOIN ratings r ON m.id = r.movie_id "
        );

        if (genre != null && !genre.isEmpty()) {
            countQuery.append(
                    "LEFT JOIN genres_in_movies gm ON m.id = gm.movie_id " +
                    "LEFT JOIN genres g ON gm.genre_id = g.id "
            );
        }

        if (star != null && !star.isEmpty()) {
            countQuery.append(
                    "LEFT JOIN stars_in_movies sm ON m.id = sm.movie_id " +
                    "LEFT JOIN stars s ON sm.star_id = s.id "
            );
        }

        countQuery.append("WHERE 1=1 ");

        if (title != null && !title.isEmpty())
            countQuery.append("AND MATCH(m.title) AGAINST(? IN BOOLEAN MODE) ");
        if (year != null && !year.isEmpty())
            countQuery.append("AND m.year = ? ");
        if (director != null && !director.isEmpty())
            countQuery.append("AND m.director LIKE ? ");
        if (genre != null && !genre.isEmpty())
            countQuery.append("AND g.name = ? ");
        if (star != null && !star.isEmpty())
            countQuery.append("AND s.name LIKE ? ");
        if (prefix != null && !prefix.isEmpty())
            countQuery.append("AND m.title LIKE ? ");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(countQuery.toString())) {

            int idx = 1;
            if (title != null && !title.isEmpty()) {
                String booleanQuery = Utils.convertToBooleanMode(title);
                ps.setString(idx++, booleanQuery);
            }
            if (year != null && !year.isEmpty())
                ps.setString(idx++, year);
            if (director != null && !director.isEmpty())
                ps.setString(idx++, "%" + director + "%");
            if (genre != null && !genre.isEmpty())
                ps.setString(idx++, genre);
            if (star != null && !star.isEmpty())
                ps.setString(idx++, "%" + star + "%");
            if (prefix != null && !prefix.isEmpty())
                ps.setString(idx, prefix + "%");

            ResultSet countRs = ps.executeQuery();
            int totalCount = 0;
            if (countRs.next()) {
                totalCount = countRs.getInt("total");
            }
            jsonObject.addProperty("totalCount", totalCount);
            return jsonObject;
        }
    }
}