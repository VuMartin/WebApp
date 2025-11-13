package main.java.org.example.servlets.customer;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/movie
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/movie?id=tt0112912
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/html/customer/movie.html?id=tt0112912
// This annotation maps this Java Servlet Class to a URL
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> moviesCollection;

    public void init(ServletConfig config) {
        try {
            mongoClient = MongoClients.create("mongodb://mytestuser:My6$Password@localhost:27017/moviedb?authSource=moviedb");
            database = mongoClient.getDatabase("moviedb");
            moviesCollection = database.getCollection("movies");
        } catch (Exception e) {
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
        try {
            Document movie = moviesCollection.find(Filters.eq("movie_id", id)).first();
            if (movie == null) {
                response.setStatus(404);
                JsonObject err = new JsonObject();
                err.addProperty("errorMessage", "Movie not found");
                out.write(err.toString());
                return;
            }
            JsonObject jsonObject = new JsonObject();

                String movieID = movie.getString("movie_id");  // db column name
                String movieTitle = movie.getString("title");
                int movieYear = movie.getInteger("year");
                String movieDirector = movie.getString("director");
                Double ratingNum = movie.getDouble("rating");
                String rating = ratingNum != null ? String.format("%.2f", ratingNum) : "N/A";

                // Create a JsonObject based on the data we retrieve from rs
                jsonObject.addProperty("movieID", movieID);
                jsonObject.addProperty("movieTitle", movieTitle);
                jsonObject.addProperty("movieYear", movieYear);
                jsonObject.addProperty("movieDirector", movieDirector);
                jsonObject.addProperty("movieRating", rating);

            JsonArray genreArr = new JsonArray();
            List<String> genres = (List<String>) movie.get("genres");
            if (genres != null) {
                genres.sort(String::compareTo); // ascending by name
                for (String g : genres) {
                    JsonObject gObj = new JsonObject();
                    gObj.addProperty("name", g);
                    genreArr.add(gObj);
                }
            }
            jsonObject.add("movieGenres", genreArr);

            JsonArray starArr = new JsonArray();
            List<Document> stars = (List<Document>) movie.get("stars");
            if (stars != null) {
                // Sort by movie_count DESC, then name ASC
                stars.sort((a, b) -> {
                    int cmp = Integer.compare(b.getInteger("movie_count", 0), a.getInteger("movie_count", 0));
                    return cmp!= 0 ? cmp : a.getString("name").compareTo(b.getString("name"));
                });

                for (Document s : stars) {
                    JsonObject sObj = new JsonObject();
                    sObj.addProperty("id", s.getString("star_id"));
                    sObj.addProperty("name", s.getString("name"));
                    starArr.add(sObj);
                }
            }

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
            out.close();
        }
    }
    @Override
    public void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}