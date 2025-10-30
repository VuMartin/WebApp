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
import main.java.org.example.utils.RecaptchaVerify;
import org.jasypt.util.password.StrongPasswordEncryptor;


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

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        if (!RecaptchaVerify.verify(gRecaptchaResponse)) {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "error");
            jsonObject.addProperty("message", "reCAPTCHA verification failed.");
            out.write(jsonObject.toString());
            response.setStatus(200);
            return;
        }
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        JsonObject jsonObject = new JsonObject();

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            jsonObject.addProperty("status", "error");
            jsonObject.addProperty("message", "Email and password are required.");
            out.write(jsonObject.toString());
            response.setStatus(200);
            return;
        }

        final String query = "SELECT id, first_name, credit_card_id, password FROM customers WHERE email = ? LIMIT 1";

        try (Connection dbCon = dataSource.getConnection();
             PreparedStatement stmt = dbCon.prepareStatement(query)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    // Email not found
                    jsonObject.addProperty("status", "error");
                    jsonObject.addProperty("message", "Invalid email or password.");
                    out.write(jsonObject.toString());
                    response.setStatus(200);
                    return;
                }

                String storedHash = rs.getString("password");
                StrongPasswordEncryptor enc = new StrongPasswordEncryptor();
                boolean ok = enc.checkPassword(password, storedHash);  // compare plaintext vs hash

                if (!ok) {
                    jsonObject.addProperty("status", "error");
                    jsonObject.addProperty("message", "Invalid email or password.");
                    out.write(jsonObject.toString());
                    response.setStatus(200);
                    return;
                }

                // Login successful → create session
                HttpSession session = request.getSession();
                session.setAttribute("email", email);
                session.setAttribute("customerID", rs.getInt("id"));
                session.setAttribute("creditCardID", rs.getString("credit_card_id"));
                session.setAttribute("firstName", rs.getString("first_name"));
                jsonObject.addProperty("status", "success");
                jsonObject.addProperty("username", rs.getString("first_name"));

                out.write(jsonObject.toString());
                response.setStatus(200);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<p>Database error: " + e.getMessage() + "</p>");
            response.setStatus(500);
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<p>Unexpected error: " + e.getMessage() + "</p>");
            response.setStatus(500);
        }
    }
}