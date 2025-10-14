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
@WebServlet(name = "LogoutServlet", urlPatterns = "/api/logout")
public class LogoutServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        response.setStatus(200);
    }
}