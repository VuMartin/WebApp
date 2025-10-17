package main.java.org.example.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/topmovies
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/movies.html
// This annotation maps this Java Servlet Class to a URL
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/topmovies")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String genre = request.getParameter("genre");
        String star = request.getParameter("star");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            StringBuilder topMoviesQuery = new StringBuilder(
                    "SELECT m.id, m.title, m.year, m.director, " +
                            "(SELECT GROUP_CONCAT(DISTINCT genre_sub.name SEPARATOR ', ') " +  // takes multiple rows of a column into one
                            " FROM (SELECT g.name " +  // nested select to get limit 3 since it does not work directly with group concat
                            "       FROM genres g " +
                            "       JOIN genres_in_movies gm ON g.id = gm.genre_id " +  // gets genres for specific movie
                            "       WHERE gm.movie_id = m.id " +  // only genres for current movie
                            "       ORDER BY g.name " +
                            "       LIMIT 3) AS genre_sub) AS genres, " +  // genres is the column name, genre_sub is name of temp table
                            "(SELECT GROUP_CONCAT(DISTINCT CONCAT(stars_sub.name, ', ', stars_sub.id) SEPARATOR ', ') " +
                            " FROM (SELECT s.name, s.id " +
                            "       FROM stars s " +
                            "       JOIN stars_in_movies sm ON s.id = sm.star_id " +
                            "       WHERE sm.movie_id = m.id " +
                            "       LIMIT 3) AS stars_sub) AS stars, " +  // stars is the column name
                            "r.rating " +
                            "FROM movies m " +
                            "JOIN ratings r ON m.id = r.movie_id " +
                            "WHERE 1=1 "
            );

            if (title != null && !title.isEmpty()) topMoviesQuery.append("AND m.title LIKE ? ");
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
            topMoviesQuery.append("ORDER BY r.rating DESC ");
            topMoviesQuery.append("LIMIT 20");

            PreparedStatement statement = conn.prepareStatement(topMoviesQuery.toString());
            int index = 1;
            if (title != null && !title.isEmpty()) statement.setString(index++, "%" + title + "%");
            if (year != null && !year.isEmpty()) statement.setString(index++, year);
            if (director != null && !director.isEmpty()) statement.setString(index++, "%" + director + "%");
            if (genre != null && !genre.isEmpty()) statement.setString(index, "%" + genre + "%");
            if (star != null && !star.isEmpty()) statement.setString(index, "%" + star + "%");

            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                // get a movie from result set
                String movieID = rs.getString("id");  // db column name from query
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieGenres = rs.getString("genres");
                String movieStars = rs.getString("stars");
                String rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movieID", movieID);
                jsonObject.addProperty("movieTitle", movieTitle);
                jsonObject.addProperty("movieYear", movieYear);
                jsonObject.addProperty("movieDirector", movieDirector);
                jsonObject.addProperty("movieGenres", movieGenres);
                jsonObject.addProperty("movieStars", movieStars);
                jsonObject.addProperty("movieRating", rating);
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}