package main.java.org.example.servlets;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

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

// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/single-star
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/star.html
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/star.html?id=nm0000001
// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query =
                            "SELECT s.id AS sid, s.name, s.birth_year, m.id AS mid, m.title " +
                            "FROM stars s " +
                            "LEFT JOIN stars_in_movies sim ON sim.star_id = s.id " +
                            "LEFT JOIN movies m ON m.id = sim.movie_id " +
                            "WHERE s.id = ? " +
                            "ORDER BY m.year DESC";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject jsonObject = new JsonObject();
            JsonArray movies = new JsonArray();

            String starName = null;
            String birthYearOut = "N/A";
            boolean found = false;

            while (rs.next()) {
                if (!found) {
                    starName = rs.getString("name");
                    int by = rs.getInt("birth_year");
                    if (!rs.wasNull()) birthYearOut = String.valueOf(by);
                    found = true;
                }
                String movieID = rs.getString("mid");
                String title   = rs.getString("title");
                if (movieID != null && title != null) {
                    JsonObject m = new JsonObject();
                    m.addProperty("id", movieID);
                    m.addProperty("title", title);
                    movies.add(m);
                }
            }
            rs.close();
            statement.close();

            if (!found) {
                response.setStatus(404);
                JsonObject err = new JsonObject();
                err.addProperty("errorMessage", "Star not found");
                out.write(err.toString());
                return;
            }

            // Keep your existing response-writing style:
            jsonObject.addProperty("id", id);              // reuse request param
            jsonObject.addProperty("name", starName);
            jsonObject.addProperty("birthYear", birthYearOut);
            jsonObject.add("movies", movies);

            out.write(jsonObject.toString());
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}