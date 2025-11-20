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
            List<Document> starMovies = moviesCollection.find(Filters.elemMatch("stars", Filters.eq("star_id", id)))
                    .sort(Sorts.orderBy(Sorts.descending("year"), Sorts.ascending("title")))
                    .into(new java.util.ArrayList<>());
            if (starMovies.isEmpty()) {
                response.setStatus(404);
                JsonObject err = new JsonObject();
                err.addProperty("errorMessage", "Star not found");
                out.write(err.toString());
                return;
            }
            JsonObject jsonObject = new JsonObject();
            JsonArray movies = new JsonArray();
            String starName = null;
            String birthYearOut = "N/A";

            for (Document movie : starMovies) {
                List<Document> stars = (List<Document>) movie.get("stars");
                for (Document star : stars) {
                    if (star.getString("star_id").equals(id)) {
                        starName = star.getString("name");
                        Integer dob = star.getInteger("birth_year");
                        if (dob != null) birthYearOut = String.valueOf(dob);
                        break;
                    }
                }
                JsonObject m = new JsonObject();
                m.addProperty("id", movie.getString("_id"));
                m.addProperty("title", movie.getString("title"));
                movies.add(m);
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
    }
    @Override
    public void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}