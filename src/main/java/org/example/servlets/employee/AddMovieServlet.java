package main.java.org.example.servlets.employee;

import com.google.gson.JsonObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

// http://localhost:8080/2025_fall_cs_122b_marjoe_war/html/employee/employee.html
// This annotation maps this Java Servlet Class to a URL
@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add_movie")
public class AddMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
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

        try (Connection conn = dataSource.getConnection()) {

            String sql = "{ CALL add_movie(?, ?, ?, ?, ?) }";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, title);
                stmt.setInt(2, year);
                stmt.setString(3, director);
                stmt.setString(4, star);
                stmt.setString(5, genre);

                boolean hasResults = stmt.execute();
                if (hasResults) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        if (rs.next()) {
                            String message = rs.getString("message");
                            jsonResponse.addProperty("message", message);
                            if (message.contains("already exists")) {
                                jsonResponse.addProperty("status", "error");
                            } else {
                                jsonResponse.addProperty("status", "success");
                            }
                            response.setStatus(200);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", e.getMessage());
            response.setStatus(500);
        }
        out.write(jsonResponse.toString());
    }
}