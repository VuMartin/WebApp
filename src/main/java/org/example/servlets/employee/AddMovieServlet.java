package main.java.org.example.servlets.employee;

import com.google.gson.JsonObject;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

// http://localhost:8080/2025_fall_cs_122b_marjoe_war/html/employee/employee.html
// This annotation maps this Java Servlet Class to a URL
@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add_movie")
public class AddMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> moviesCollection;
    private MongoCollection<Document> starsCollection;
    private MongoCollection<Document> countersCollection;

    public void init(ServletConfig config) {
        try {
            mongoClient = MongoClients.create("mongodb://mytestuser:My6$Password@localhost:27017/moviedb?authSource=moviedb");
            database = mongoClient.getDatabase("moviedb");
            moviesCollection = database.getCollection("movies");
            starsCollection = database.getCollection("stars");
            countersCollection = database.getCollection("counters");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        String title = request.getParameter("title");
        String yearStr = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String genre = request.getParameter("genre");

        int year = Integer.parseInt(yearStr);

        JsonObject jsonResponse = new JsonObject();
        PrintWriter out = response.getWriter();

        try {
            String message = addMovie(title, year, director, star, genre);
            jsonResponse.addProperty("message", message);
            if (message.contains("already exists"))
                jsonResponse.addProperty("status", "error");
            else jsonResponse.addProperty("status", "success");
            response.setStatus(200);
        } catch (Exception e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", e.getMessage());
            response.setStatus(500);
        }
        out.write(jsonResponse.toString());
    }
    public String addMovie(String title, int year, String director,
                           String starName, String genreName) {
        Document existingMovie = moviesCollection.find(
                Filters.and(
                        Filters.eq("title", title),
                        Filters.eq("year", year),
                        Filters.eq("director", director)
                )
        ).first();
        if (existingMovie != null) {
            return "Movie \"" + title + "\" already exists.";
        }

        Document updated = countersCollection.findOneAndUpdate(
                Filters.eq("_id", "movie_id"),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        );
        int newId = updated.getInteger("seq");
        String movieId = "tt" + String.format("%07d", newId);

        Document starDoc = starsCollection.find(Filters.eq("name", starName)).first();
        String starId;

        if (starDoc == null) {
            updated = countersCollection.findOneAndUpdate(
                    Filters.eq("_id", "star_id"),
                    Updates.inc("seq", 1),
                    new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
            );
            newId = updated.getInteger("seq");
            starId = "nm" + String.format("%07d", newId);
            starDoc = new Document("_id", starId).append("name", starName).append("birth_year", null);
            starsCollection.insertOne(starDoc);
            starDoc = new Document("star_id", starId).append("name", starName).append("movie_count", 1);
        } else {
            starId = starDoc.getString("_id");
            moviesCollection.updateMany(
                    Filters.eq("stars.star_id", starId),
                    Updates.inc("stars.$.movie_count", 1)
            );

            Document firstMovieStarIsIn = moviesCollection.find(Filters.eq("stars.star_id", starId)).first();
            int newMovieCount = 0;
            List<Document> starsArray = (List<Document>) firstMovieStarIsIn.get("stars");
            for (Document s : starsArray) {
                if (s.getString("star_id").equals(starId)) {
                    newMovieCount = s.getInteger("movie_count");
                    break;
                }
            }
            starDoc = new Document("star_id", starId)
                    .append("name", starDoc.getString("name"))
                    .append("movie_count", newMovieCount);
        }
        Document movieDoc = new Document("_id", movieId)
                .append("title", title)
                .append("year", year)
                .append("director", director)
                .append("rating", null)
                .append("stars", Arrays.asList(starDoc))
                .append("genres", Arrays.asList(genreName));

        moviesCollection.insertOne(movieDoc);

        return "Movie \"" + title + "\" (Movie ID: " + movieId + "), "
                + "Star \"" + starName + "\" (Star ID: " + starId + "), "
                + "Genre \"" + genreName + "\" added.";
    }
    @Override
    public void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}