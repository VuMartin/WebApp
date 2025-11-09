package main.java.org.example.servlets.employee;

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
import main.java.org.example.utils.recaptcha.RecaptchaVerify;
import org.jasypt.util.password.StrongPasswordEncryptor;


// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/login
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/login.html
// Declaring a WebServlet called LoginServlet, which maps to url "/api/login"
@WebServlet(name = "_dashboard", urlPatterns = "/api/_dashboard")
public class EmployeeLoginServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
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

        final String query = "SELECT * " +
                "FROM employees " +
                "WHERE email = ? LIMIT 1";

        try (Connection dbCon = dataSource.getConnection();
             PreparedStatement stmt = dbCon.prepareStatement(query)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                StrongPasswordEncryptor enc = new StrongPasswordEncryptor();
                boolean valid = (rs.next() && enc.checkPassword(password, rs.getString("password")));
                if (!valid) {
                    // Email not found
                    jsonObject.addProperty("status", "error");
                    jsonObject.addProperty("message", "Invalid email or password.");
                    writeResponse(out, response, jsonObject);
                    return;
                }
                // Login successful → create session
                HttpSession session = request.getSession();
                session.setAttribute("email", email);
                session.setAttribute("fullname", rs.getString("fullname"));
                jsonObject.addProperty("status", "success");
                jsonObject.addProperty("username", rs.getString("fullname"));
                writeResponse(out, response, jsonObject);
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