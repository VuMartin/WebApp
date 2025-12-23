package main.java.org.example.servlets.customer.SingleMovie;

import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLSingleMovieRetriever implements SingleMovieRetriever {

    private static final String MOVIE_SQL =
            "SELECT m.id, m.title, m.year, m.director, MAX(r.rating) AS rating " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movie_id " +
                    "WHERE m.id = ? " +
                    "GROUP BY m.id";

    private static final String GENRES_SQL =
            "SELECT g.id, g.name " +
                    "FROM genres g " +
                    "JOIN genres_in_movies gim ON gim.genre_id = g.id " +
                    "WHERE gim.movie_id = ? " +
                    "ORDER BY g.name ASC";

    private static final String STARS_SQL =
            "SELECT s.id, s.name, cnt.movie_count " +
                    "FROM stars s " +
                    "JOIN stars_in_movies sim ON sim.star_id = s.id " +
                    "JOIN ( " +
                    "  SELECT star_id, COUNT(*) AS movie_count " +
                    "  FROM stars_in_movies " +
                    "  GROUP BY star_id " +
                    ") cnt ON cnt.star_id = s.id " +
                    "WHERE sim.movie_id = ? " +
                    "ORDER BY cnt.movie_count DESC, s.name ASC";

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    MySQLSingleMovieRetriever() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JsonObject getSingleMovie(String movieId) throws SQLException {
        SingleMoviePojo movie = SingleMoviePojo.builder()
                .setId(movieId);
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(MOVIE_SQL)) {
                ps.setString(1, movieId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        movie
                                .setTitle(rs.getString("title"))
                                .setYear(rs.getString("year"))
                                .setDirector(rs.getString("director"));
                        String rating = rs.getString("rating");
                        movie.setRating(rating == null ? "N/A" : rating);
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(GENRES_SQL)) {
                ps.setString(1, movieId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        movie.addGenre(
                                GenrePojo.builder()
                                        .setId(rs.getString("id"))
                                        .setName(rs.getString("name"))
                        );
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(STARS_SQL)) {
                ps.setString(1, movieId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        movie.addStar(
                                StarPojo.builder()
                                        .setId(rs.getString("id"))
                                        .setName(rs.getString("name"))
                                        .setMovieCount(rs.getInt("movie_count"))
                        );
                    }
                }
            }
        }

        return movie.toJson();
    }
}