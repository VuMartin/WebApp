package main.java.org.example.servlets.customer;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/single-star
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/star.html
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/html/customer/star.html?id=nm0000001
// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> moviesCollection;
    private MongoCollection<Document> starsCollection;

    public void init(ServletConfig config) {
        try {
            mongoClient = MongoClients.create("mongodb://mytestuser:My6$Password@localhost:27017/moviedb?authSource=moviedb");
            database = mongoClient.getDatabase("moviedb");
            moviesCollection = database.getCollection("movies");
            starsCollection = database.getCollection("stars");
        } catch (Exception e) {
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

        try {
            Document starDoc = starsCollection.find(Filters.eq("_id", id)).first();
            if (starDoc == null) {
                response.setStatus(404);
                JsonObject err = new JsonObject();
                err.addProperty("errorMessage", "Star not found");
                out.write(err.toString());
                return;
            }
            String starName = starDoc.getString("name");
            Integer birthYear = starDoc.getInteger("birth_year");
            String birthYearOut = birthYear != null ? String.valueOf(birthYear) : "N/A";

            List<Document> starMovies = moviesCollection.find(Filters.elemMatch("stars", Filters.eq("star_id", id)))
                    .sort(Sorts.orderBy(Sorts.descending("year"), Sorts.ascending("title")))
                    .into(new java.util.ArrayList<>());
            JsonObject jsonObject = new JsonObject();
            JsonArray movies = new JsonArray();

            for (Document movie : starMovies) {
                JsonObject m = new JsonObject();
                m.addProperty("id", movie.getString("movie_id"));
                m.addProperty("title", movie.getString("title"));
                movies.add(m);
            }

            jsonObject.addProperty("id", id);
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
    }
    @Override
    public void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}