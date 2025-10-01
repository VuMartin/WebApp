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
import java.sql.ResultSet;
import java.sql.Statement;

// http://localhost:8080/2025_fall_cs_122b_marjoe_war/topmovies
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/movies.html
// This annotation maps this Java Servlet Class to a URL
@WebServlet(name = "MovieListServlet", urlPatterns = "/topmovies")
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

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String topMoviesQuery = "SELECT m.id, m.title, m.year, m.director, " +
                                    "(SELECT GROUP_CONCAT(genre_sub.name SEPARATOR ', ') " +
                                    " FROM (SELECT g.name " +
                                    "       FROM genres g " +
                                    "       JOIN genres_in_movies gm ON g.id = gm.genre_id " +
                                    "       WHERE gm.movie_id = m.id " +
                                    "       LIMIT 3) AS genre_sub) AS genres, " +
                                    "(SELECT GROUP_CONCAT(stars_sub.name SEPARATOR ', ') " +
                                    " FROM (SELECT s.name " +
                                    "       FROM stars s " +
                                    "       JOIN stars_in_movies sm ON s.id = sm.star_id " +
                                    "       WHERE sm.movie_id = m.id " +
                                    "       LIMIT 3) AS stars_sub) AS stars " +
                                    "FROM movies m " +
                                    "LIMIT 20";


            // Perform the query
            ResultSet rs = statement.executeQuery(topMoviesQuery);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                // get a movie from result set
                String movieID = rs.getString("id");  // db column name
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieGenres = rs.getString("genres");
                String movieStars = rs.getString("stars");
//                String rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movieID", movieID);
                jsonObject.addProperty("movieTitle", movieTitle);
                jsonObject.addProperty("movieYear", movieYear);
                jsonObject.addProperty("movieDirector", movieDirector);
                jsonObject.addProperty("movieGenres", movieGenres);
                jsonObject.addProperty("movieStars", movieStars);
//                jsonObject.addProperty("movieRating", rating);
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