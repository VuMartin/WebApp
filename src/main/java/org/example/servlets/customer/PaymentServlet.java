package main.java.org.example.servlets.customer;

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
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.bson.Document;

import java.time.LocalDate;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> creditCardsCollection;
    private MongoCollection<Document> salesCollection;
    private MongoCollection<Document> countersCollection;

    public void init(ServletConfig config) {
        try {
            mongoClient = MongoClients.create("mongodb://mytestuser:My6$Password@localhost:27017/moviedb?authSource=moviedb");
            database = mongoClient.getDatabase("moviedb");
            creditCardsCollection = database.getCollection("credit_cards");
            salesCollection = database.getCollection("sales");
            countersCollection = database.getCollection("counters");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String cardNumber = request.getParameter("cardNumber");
        String expStr = request.getParameter("expiration"); // "2005-11-01"
        LocalDate localDate = LocalDate.parse(expStr);
        java.util.Date expiration = java.util.Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in first.");
            return;
        }

        Integer customerID = (Integer) session.getAttribute("customerID");
        if (customerID == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in first.");
            return;
        }
        JsonObject responseJson = new JsonObject();
        try {
            // 1. Verify credit card exists
            Document card = creditCardsCollection.find(Filters.and(
                    Filters.eq("first_name", firstName),
                    Filters.eq("last_name", lastName),
                    Filters.eq("_id", cardNumber),
                    Filters.eq("expiration", expiration)
            )).first();
            if (card != null) {
                Map<String, CartItem> cart = (Map<String, CartItem>) session.getAttribute("cart");
                List<Document> items = new ArrayList<>();
                for (CartItem item : cart.values()) {
                    items.add(new Document("movie_id", item.getMovieID())
                            .append("quantity", item.getQuantity()));
                }
                int orderNumber = getNextOrderID();
                Document order = new Document("_id", orderNumber)
                        .append("customer_id", customerID)
                        .append("sale_date", LocalDate.now().toString())
                        .append("items", items);

                salesCollection.insertOne(order);
                session.setAttribute("orderNumber", orderNumber);
                responseJson.addProperty("status", "success");
                responseJson.addProperty("message", "Payment processed successfully!");
            } else {
                responseJson.addProperty("status", "fail");
                responseJson.addProperty("message", "Invalid payment information. Please try again.");
            }
            out.write(responseJson.toString());
            response.setStatus(200);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    private int getNextOrderID() {
        Document updated = countersCollection.findOneAndUpdate(
                Filters.eq("_id", "order_id"),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        );
        return updated.getInteger("seq");
    }

    @Override
    public void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}