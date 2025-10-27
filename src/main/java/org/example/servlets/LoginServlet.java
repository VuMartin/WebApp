package main.java.org.example.servlets;

import com.google.gson.JsonObject;
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


// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/login
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/login.html
// Declaring a WebServlet called LoginServlet, which maps to url "/api/login"
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection dbCon = dataSource.getConnection()) {
            JsonObject jsonObject = new JsonObject();
            String query = "SELECT * " +
                           "FROM customers " +
                           "WHERE email = ? AND password = ?";
            try (PreparedStatement stmt = dbCon.prepareStatement(query)) {
                stmt.setString(1, email);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Login successful → create session
                        HttpSession session = request.getSession();
                        session.setAttribute("email", email);
                        session.setAttribute("customerID", rs.getInt("id"));
                        session.setAttribute("creditCardID", rs.getString("credit_card_id"));
                        session.setAttribute("firstName", rs.getString("first_name"));
                        jsonObject.addProperty("status", "success");
                        jsonObject.addProperty("username", rs.getString("first_name"));
                    } else {
                        jsonObject.addProperty("status", "error");
                        jsonObject.addProperty("message", "Invalid email or password.");
                    }
                }
            }
            // Write JSON string to output
            out.write(jsonObject.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<p>Database error: " + e.getMessage() + "</p>");
            response.setStatus(500);
        }
        catch (Exception e) {
            e.printStackTrace();
            out.println("<p>Unexpected error: " + e.getMessage() + "</p>");
            response.setStatus(500);
        }
    }
}