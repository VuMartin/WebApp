package main.java.org.example.servlets.customer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "AutoCompleteServlet", urlPatterns = "/api/autocomplete")
public class AutoCompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = dataSource.getConnection()) {
            String query = request.getParameter("query");

            if (query == null || query.trim().isEmpty() || query.length() < 3) {
                response.getWriter().write("{\"suggestions\": []}");
                return;
            }

            JsonArray suggestions = new JsonArray();
            String sql = "SELECT id, title FROM movies " +
                    "WHERE MATCH(title) AGAINST(? IN BOOLEAN MODE) " +
                    "LIMIT 10";

            PreparedStatement statement = conn.prepareStatement(sql);
            String booleanQuery = SearchUtils.convertToBooleanMode(query);
            statement.setString(1, booleanQuery);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                JsonObject suggestion = new JsonObject();
                suggestion.addProperty("value", rs.getString("title"));

                JsonObject data = new JsonObject();
                data.addProperty("movieId", rs.getString("id"));
                suggestion.add("data", data);

                suggestions.add(suggestion);
            }

            JsonObject result = new JsonObject();
            result.add("suggestions", suggestions);

            response.getWriter().write(result.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"suggestions\": []}");
        }
    }
}