package main.java.org.example.servlets;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
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
        if (customerID == null) {
            // handle missing ID
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
            verifyStmt.setString(4, expiration + "-01"); // convert yyyy-MM → yyyy-MM-01
            ResultSet rs = verifyStmt.executeQuery();

            JsonObject responseJson = new JsonObject();

            if (rs.next()) {
                // Card is valid → insert sale
                String insertSaleQuery = "INSERT INTO sales (customer_id, movie_id, sale_date) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSaleQuery);
                insertStmt.setInt(1, customerID);
                insertStmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                insertStmt.executeUpdate();
                insertStmt.close();

                responseJson.addProperty("status", "success");
                responseJson.addProperty("message", "Payment processed successfully!");
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