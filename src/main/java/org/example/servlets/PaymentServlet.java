package main.java.org.example.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (
        NamingException e) {
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
        String expiration = request.getParameter("expiration");
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in first.");
            return;
        }

        Integer customerID = (Integer) session.getAttribute("customerID");
//        Integer customerID = 490001;
        if (customerID == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in first.");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            // 1. Verify credit card exists
            String verifyCardQuery = "SELECT id " +
                                     "FROM credit_cards " +
                                     "WHERE first_name = ? AND last_name = ? AND id = ? AND expiration = ?";
            PreparedStatement verifyStmt = conn.prepareStatement(verifyCardQuery);
            verifyStmt.setString(1, firstName);
            verifyStmt.setString(2, lastName);
            verifyStmt.setString(3, cardNumber);
            verifyStmt.setString(4, expiration); // convert yyyy-MM → yyyy-MM-01
            ResultSet rs = verifyStmt.executeQuery();

            JsonObject responseJson = new JsonObject();

            if (rs.next()) {
                String insertSaleQuery = "INSERT INTO sales (customer_id, movie_id, sale_date) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSaleQuery);
                java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());
                Map<String, CartItem> cart = (Map<String, CartItem>) session.getAttribute("cart");
                for (CartItem item : cart.values()) {
                    insertStmt.setInt(1, customerID);
                    insertStmt.setString(2, item.getMovieID());
                    insertStmt.setDate(3, today);
                    insertStmt.executeUpdate();
                }

                insertStmt.close();
                // Build confirmation info
                JsonArray itemsJson = new JsonArray();
                double total = 0;

                for (CartItem item : cart.values()) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("title", item.getTitle());
                    obj.addProperty("quantity", item.getQuantity());
                    total += item.getPrice() * item.getQuantity();
                    itemsJson.add(obj);
                }

// Add everything to the response JSON
                responseJson.addProperty("status", "success");
                responseJson.addProperty("message", "Payment processed successfully!");
                responseJson.addProperty("firstName", (String) session.getAttribute("firstName"));
                responseJson.addProperty("cardNumber", (String) session.getAttribute("creditCardID"));
                responseJson.addProperty("total", total);
                responseJson.add("items", itemsJson);
            } else {
                responseJson.addProperty("status", "fail");
                responseJson.addProperty("message", "Invalid payment information. Please try again.");
            }

            rs.close();
            verifyStmt.close();

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
}