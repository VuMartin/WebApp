package main.java.org.example.servlets.customer;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.java.org.example.utils.Utils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// http://localhost:8080/api/movie
// http://localhost:8080/api/movie?id=tt0112912
// http://localhost:8080/html/customer/movie.html?id=tt0112912
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
        long startTime = System.nanoTime();
        long totalDbTime = 0;
        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);
        System.out.println("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        long dbStartConn = System.nanoTime();
        try (Connection conn = dataSource.getConnection()) {
            long dbEndConn = System.nanoTime();
            totalDbTime += (dbEndConn - dbStartConn);
            String movieQuery =
                    "SELECT m.id, m.title, m.year, m.director, MAX(r.rating) AS rating " +
                            "FROM movies m " +
                            "LEFT JOIN ratings r ON m.id = r.movie_id " +
                            "WHERE m.id = ? " +
                            "GROUP BY m.id";

            long dbStart1 = System.nanoTime();
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(movieQuery);
            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            long dbEnd1 = System.nanoTime();
            totalDbTime += (dbEnd1 - dbStart1);

            JsonObject jsonObject = new JsonObject();

            if (rs.next()) {
                // get a movie from result set
                String movieID = rs.getString("id");  // db column name
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String rating = rs.getString("rating");
                if (rating == null) rating = "N/A";

                // Create a JsonObject based on the data we retrieve from rs
                jsonObject.addProperty("movieID", movieID);
                jsonObject.addProperty("movieTitle", movieTitle);
                jsonObject.addProperty("movieYear", movieYear);
                jsonObject.addProperty("movieDirector", movieDirector);
                jsonObject.addProperty("movieRating", rating);
            }
            rs.close();
            statement.close();

            JsonArray genreArr = new JsonArray();

            String sqlGenres =
                    "SELECT g.id, g.name " +
                            "FROM genres g " +
                            "JOIN genres_in_movies gim ON gim.genre_id = g.id " +
                            "WHERE gim.movie_id = ? " +
                            "ORDER BY g.name ASC";
            long dbStart2 = System.nanoTime();
            PreparedStatement psGenres = conn.prepareStatement(sqlGenres);
            psGenres.setString(1, id);

            ResultSet rs2 = psGenres.executeQuery();
            long dbEnd2 = System.nanoTime();
            totalDbTime += (dbEnd2 - dbStart2);
            while (rs2.next()) {
                JsonObject g = new JsonObject();
                g.addProperty("id", rs2.getInt("id"));
                g.addProperty("name", rs2.getString("name"));
                genreArr.add(g);
            }
            rs2.close();
            psGenres.close();

            jsonObject.add("movieGenres", genreArr);

// --- STARS (by movie_count DESC, then name ASC) ---
            JsonArray starArr = new JsonArray();

            String sqlStars =
                    "SELECT s.id, s.name, cnt.movie_count " +
                            "FROM stars s " +
                            "JOIN stars_in_movies sim ON sim.star_id = s.id " +
                            "JOIN ( " +
                            "  SELECT star_id, COUNT(*) AS movie_count " +
                            "  FROM stars_in_movies " +
                            "  GROUP BY star_id " +
                            ") AS cnt ON cnt.star_id = s.id " +
                            "WHERE sim.movie_id = ? " +
                            "ORDER BY cnt.movie_count DESC, s.name ASC";
            long dbStart3 = System.nanoTime();
            PreparedStatement psStars = conn.prepareStatement(sqlStars);
            psStars.setString(1, id);

            ResultSet rs3 = psStars.executeQuery();
            long dbEnd3 = System.nanoTime();
            totalDbTime += (dbEnd3 - dbStart3);
            while (rs3.next()) {
                JsonObject s = new JsonObject();
                s.addProperty("id", rs3.getString("id"));
                s.addProperty("name", rs3.getString("name"));
                s.addProperty("movieCount", rs3.getInt("movie_count"));
                starArr.add(s);
            }
            rs3.close();
            psStars.close();

            jsonObject.add("movieStars", starArr);

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
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            long dbTime = totalDbTime;
            Utils.writeTimingToFile(totalTime, dbTime, "SingleMovieServlet", id);
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}