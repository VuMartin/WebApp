package main.java.org.example.servlets.customer.SingleMovie;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.java.org.example.servlets.customer.SingleStar.MySQLSingleStarRetriever;
import main.java.org.example.servlets.customer.SingleStar.SingleStarRetriever;
import main.java.org.example.utils.Utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

// http://localhost:8080/api/movie
// http://localhost:8080/api/movie?id=tt0112912
// http://localhost:8080/html/customer/movie.html?id=tt0112912
// This annotation maps this Java Servlet Class to a URL
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private SingleMovieRetriever singleMovieRetriever;
    public void init(ServletConfig config) {
        this.singleMovieRetriever = new MySQLSingleMovieRetriever();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        // Retrieve parameter id from url request.
        String id = request.getParameter("id");
        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);
        try {
            JsonObject jsonObject = singleMovieRetriever.getSingleMovie(id);
            try (PrintWriter out = response.getWriter()) {
                out.write(jsonObject.toString());
            }
            response.setStatus(200);
        }  catch (SQLException e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            try (PrintWriter out = response.getWriter()) {
                out.write(jsonObject.toString());
            }
            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        }
    }
}