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

// http://localhost:8080/2025_fall_cs_122b_marjoe_war/cart.html
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class ShoppingCartServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String movieId = request.getParameter("movieId");
        String title = request.getParameter("title"); // for adding
        double price = request.getParameter("price") != null ? Double.parseDouble(request.getParameter("price")) : 0;
        String action = request.getParameter("action");

        HttpSession session = request.getSession();
        Map<String, CartItem> cart = (Map<String, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        synchronized(cart) {
            if ("remove".equals(action)) {
                cart.remove(movieId);
            } else if ("update".equals(action)) {
                CartItem item = cart.get(movieId);
                if (item != null) {
                    int newQty = Integer.parseInt(request.getParameter("quantity"));
                    item.setQuantity(newQty);
                }
            } else {
                CartItem item = cart.getOrDefault(movieId, new CartItem(movieId, title, price, 0));
                item.setQuantity(item.getQuantity() + 1);
                cart.put(movieId, item);
            }
        }

        // Build JSON array to return
        JsonArray cartArray = new JsonArray();
        synchronized(cart) {
            for (CartItem item : cart.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("movieId", item.getMovieId());
                obj.addProperty("title", item.getTitle());
                obj.addProperty("quantity", item.getQuantity());
                obj.addProperty("price", item.getPrice());
                cartArray.add(obj);
            }
        }

        response.getWriter().write(cartArray.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
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