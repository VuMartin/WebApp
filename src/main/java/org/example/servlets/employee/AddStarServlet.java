package main.java.org.example.servlets.employee;


import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.bson.Document;

import javax.naming.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name="AddStarServlet", urlPatterns="/api/add_star")
public class AddStarServlet extends HttpServlet {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> countersCollection;
    private MongoCollection<Document> starsCollection;

    public void init(ServletConfig config) {
        try {
            mongoClient = MongoClients.create("mongodb://mytestuser:My6$Password@localhost:27017/moviedb?authSource=moviedb");
            database = mongoClient.getDatabase("moviedb");
            countersCollection = database.getCollection("counters");
            starsCollection = database.getCollection("stars");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject json = new JsonObject();

        String name = req.getParameter("name");
        String birth = req.getParameter("birthYear");

        Integer birthYear = null;
        if (birth != null && !birth.trim().isEmpty()) {
            try {
                birthYear = Integer.parseInt(birth.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        try {
            Document existingStar = starsCollection.find(Filters.eq("name", name.trim())).first();
            if (existingStar != null) {
                json.addProperty("status", "error");
                json.addProperty("message", "Star " + name + " already exists with ID: " + existingStar.getString("_id"));
                out.write(json.toString());
                return;
            }
            Document starCounter = countersCollection.findOneAndUpdate(
                    Filters.eq("_id", "star_id"),
                    Updates.inc("seq", 1),
                    new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
            );
            String newId = "nm" + String.format("%07d", starCounter.getInteger("seq"));

            Document newStar = new Document("_id", newId)
                    .append("name", name.trim())
                    .append("birth_year", birthYear);
            starsCollection.insertOne(newStar);
            json.addProperty("status","success");
            json.addProperty("message","Star added successfully.");
            json.addProperty("id", newId);
            out.write(json.toString());
        } catch (Exception e) {
            JsonObject err = new JsonObject();
            err.addProperty("status","error");
            err.addProperty("message","DB error: " + e.getMessage());
            out.write(err.toString());
        }
    }
    @Override
    public void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
