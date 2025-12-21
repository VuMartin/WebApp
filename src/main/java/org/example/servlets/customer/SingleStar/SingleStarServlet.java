package main.java.org.example.servlets.customer.SingleStar;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// http://localhost:8080/api/single-star?id=nm0000001
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/star.html
// http://localhost:8080/html/customer/star.html?id=nm0000001
// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private SingleStarRetriever  singleStarRetriever;

    public void init(ServletConfig config) {
        this.singleStarRetriever = new MySQLSingleStarRetriever();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type
        String id = request.getParameter("id");
        request.getServletContext().log("getting id: " + id);
        try {
            JsonObject jsonObject = singleStarRetriever.getSingleStar(id);
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