package main.java.org.example.servlets.customer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "GenresServlet", urlPatterns = "/api/genres")
public class GenresServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init() {
        try { dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb"); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter();
             Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, name FROM genres WHERE id <> ?")) {
            ps.setInt(1, 35);
            try (ResultSet rs = ps.executeQuery()) {
                JsonArray arr = new JsonArray();
                while (rs.next()) {
                    JsonObject g = new JsonObject();
                    g.addProperty("id", rs.getInt("id"));
                    g.addProperty("name", rs.getString("name"));
                    arr.add(g);
                }
                out.write(arr.toString());
                resp.setStatus(200);
            }
        } catch (Exception e) {
            try {
                JsonObject err = new JsonObject();
                err.addProperty("errorMessage", e.getMessage());
                resp.getWriter().write(err.toString());
                resp.setStatus(500);
            } catch (Exception ignored) {}
        }
    }
}