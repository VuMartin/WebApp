package main.java.org.example.servlets.customer;

import com.google.gson.JsonObject;
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
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import main.java.org.example.utils.recaptcha.RecaptchaVerify;
import org.bson.Document;
import org.jasypt.util.password.StrongPasswordEncryptor;


// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/login
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/login.html
// Declaring a WebServlet called LoginServlet, which maps to url "/api/login"
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> customersCollection;

    public void init(ServletConfig config) {
        try {
            mongoClient = MongoClients.create("mongodb://mytestuser:My6$Password@localhost:27017/moviedb?authSource=moviedb");
            database = mongoClient.getDatabase("moviedb");
            customersCollection = database.getCollection("customers");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeResponse(PrintWriter out, HttpServletResponse response, JsonObject json) {
        out.write(json.toString());
        response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject jsonObject = new JsonObject();
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        if (!RecaptchaVerify.verify(gRecaptchaResponse)) {
            jsonObject.addProperty("status", "error");
            jsonObject.addProperty("message", "reCAPTCHA verification failed.");
            writeResponse(out, response, jsonObject);
            return;
        }
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            jsonObject.addProperty("status", "error");
            jsonObject.addProperty("message", "Email and password are required.");
            writeResponse(out, response, jsonObject);
            return;
        }

        try {
            Document customer = customersCollection.find(Filters.eq("email", email)).first();
            StrongPasswordEncryptor enc = new StrongPasswordEncryptor();
            if (customer == null || !enc.checkPassword(password, customer.getString("password"))) {
                jsonObject.addProperty("status", "error");
                jsonObject.addProperty("message", "Invalid email or password.");
                writeResponse(out, response, jsonObject);
                return;
            }
            // Login successful → create session
            HttpSession session = request.getSession();
            session.setAttribute("email", email);
            session.setAttribute("customerID", customer.getInteger("_id"));
            session.setAttribute("creditCardID", customer.getString("credit_card_id"));
            session.setAttribute("firstName", customer.getString("first_name"));
            jsonObject.addProperty("status", "success");
            jsonObject.addProperty("username", customer.getString("first_name"));
            writeResponse(out, response, jsonObject);
        } catch (Exception e) {
            jsonObject.addProperty("status", "error");
            jsonObject.addProperty("message", e.getMessage());
            writeResponse(out, response, jsonObject);
            request.getServletContext().log("Error:", e);
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