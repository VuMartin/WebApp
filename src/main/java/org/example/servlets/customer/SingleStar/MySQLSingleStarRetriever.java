package main.java.org.example.servlets.customer.SingleStar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLSingleStarRetriever implements SingleStarRetriever {

    private static final String SELECT_MOVIE_BY_ID =
            "SELECT s.id AS sid, s.name, s.birth_year, m.id AS mid, m.title " +
                    "FROM stars s " +
                    "LEFT JOIN stars_in_movies sim ON sim.star_id = s.id " +
                    "LEFT JOIN movies m ON m.id = sim.movie_id " +
                    "WHERE s.id = ? " +
                    "ORDER BY m.year DESC, m.title ASC";

    private DataSource dataSource;
    // Create a dataSource which registered in web.xml
    MySQLSingleStarRetriever() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JsonObject getSingleStar(String starId) throws SQLException {
        JsonObject result;
        // Get a connection from dataSource and let resource manager close the connection after usage
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(SELECT_MOVIE_BY_ID);) {
            statement.setString(1, starId);
            try (ResultSet rs = statement.executeQuery()) {
                SingleStarPojo singleStarPojo = SingleStarPojo.builder()
                        .setStarId(starId); // from request

                String birthYearOut = "N/A";
                boolean found = false;

                while (rs.next()) {
                    if (!found) {
                        singleStarPojo
                                .setStarName(rs.getString("name"));
                        int by = rs.getInt("birth_year");
                        if (!rs.wasNull()) birthYearOut = String.valueOf(by);
                        singleStarPojo.setStarBirthYear(birthYearOut);
                        found = true;
                    }
                    String movieID = rs.getString("mid");
                    String title = rs.getString("title");
                    if (movieID != null && title != null) {
                        singleStarPojo.addMovie(movieID, title);
                    }
                }

                result = singleStarPojo.toJson();
            }
        }
        return result;
    }
}