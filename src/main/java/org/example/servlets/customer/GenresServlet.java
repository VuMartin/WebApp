package main.java.org.example.servlets.customer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.bson.Document;

import javax.naming.InitialContext;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "GenresServlet", urlPatterns = "/api/genres")
public class GenresServlet extends HttpServlet {
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        try {
            PrintWriter out = resp.getWriter();
            List<String> genres = moviesCollection.distinct("genres", String.class).into(new ArrayList<>());
            JsonArray arr = new JsonArray();
            for (String g : genres) {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", g);
                arr.add(obj);
            }
            out.write(arr.toString());
            resp.setStatus(200);
        } catch (Exception e) {
            try {
                JsonObject err = new JsonObject();
                err.addProperty("errorMessage", e.getMessage());
                resp.getWriter().write(err.toString());
                resp.setStatus(500);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
