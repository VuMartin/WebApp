package main.java.org.example.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class ShoppingCartServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String movieId = request.getParameter("movieId");

        // Cart = {movieId -> quantity}
        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        synchronized (cart) {
            cart.put(movieId, cart.getOrDefault(movieId, 0) + 1);
        }

        response.getWriter().write("{\"message\": \"Added to cart\"}");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("cart") == null) {
            response.getWriter().write("{\"cart\": []}");
            return;
        }

        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
        JsonArray cartJson = new JsonArray();

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            JsonObject item = new JsonObject();
            item.addProperty("movieId", entry.getKey());
            item.addProperty("quantity", entry.getValue());
            cartJson.add(item);
        }

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.add("cart", cartJson);
        response.getWriter().write(jsonResponse.toString());
    }
}