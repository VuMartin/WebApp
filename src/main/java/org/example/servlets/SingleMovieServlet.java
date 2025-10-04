package main.java.org.example.servlets;

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

// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/movie
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/movie?id=tt0112912
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/movie.html
// This annotation maps this Java Servlet Class to a URL
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/movie")
public class SingleMovieServlet extends HttpServlet {
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

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);
        System.out.println("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            String movieQuery = "SELECT m.id, m.title, m.year, m.director, " +
                                    "GROUP_CONCAT(DISTINCT g.name SEPARATOR ', ') AS genres, " +
                                    "GROUP_CONCAT(DISTINCT s.name SEPARATOR ', ') AS stars " +
                                "FROM movies m " +
//                                "LEFT JOIN ratings r ON m.id = r.movie_id " +
                                "LEFT JOIN genres_in_movies gm ON m.id = gm.movie_id " +  // left join to include movies with no genres, stars, ratings
                                "LEFT JOIN genres g ON gm.genre_id = g.id " +
                                "LEFT JOIN stars_in_movies sm ON m.id = sm.movie_id " +
                                "LEFT JOIN stars s ON sm.star_id = s.id " +
                                "WHERE m.id = ? " +
                                "GROUP BY m.id";


            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(movieQuery);
            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject jsonObject = new JsonObject();

            if (rs.next()) {
                // get a movie from result set
                String movieID = rs.getString("id");  // db column name
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieGenres = rs.getString("genres");
                String movieStars = rs.getString("stars");
//                String rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                jsonObject.addProperty("movieID", movieID);
                jsonObject.addProperty("movieTitle", movieTitle);
                jsonObject.addProperty("movieYear", movieYear);
                jsonObject.addProperty("movieDirector", movieDirector);
                jsonObject.addProperty("movieGenres", movieGenres);
                jsonObject.addProperty("movieStars", movieStars);
//                jsonObject.addProperty("movieRating", rating);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonObject.size() + " results");

            // Write JSON string to output
            out.write(jsonObject.toString());
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