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

        String movieID = request.getParameter("movieID");
        String title = request.getParameter("title");
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
                cart.remove(movieID);
            } else if ("update".equals(action)) {
                CartItem item = cart.get(movieID);
                if (item != null) {
                    int newQty = Integer.parseInt(request.getParameter("quantity"));
                    item.setQuantity(newQty);
                }
            } else {
                CartItem item = cart.getOrDefault(movieID, new CartItem(movieID, title, price, 0));
                item.setQuantity(item.getQuantity() + 1);
                cart.put(movieID, item);
            }
        }

        // Build JSON array to return
        JsonArray cartArray = new JsonArray();
        synchronized(cart) {
            for (CartItem item : cart.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("movieID", item.getMovieID());
                obj.addProperty("title", item.getTitle());
                obj.addProperty("quantity", item.getQuantity());
                obj.addProperty("price", item.getPrice());
                cartArray.add(obj);
            }
        }

        response.getWriter().write(cartArray.toString());
        response.setStatus(200);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("cart") == null) {
            response.getWriter().write("[]"); // empty array
            return;
        }

        Map<String, CartItem> cart = (Map<String, CartItem>) session.getAttribute("cart");
        JsonArray cartJson = new JsonArray();

        synchronized(cart) {
            for (CartItem item : cart.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("movieID", item.getMovieID());
                obj.addProperty("title", item.getTitle());
                obj.addProperty("quantity", item.getQuantity());
                obj.addProperty("price", item.getPrice());
                cartJson.add(obj);
            }
        }

        response.getWriter().write(cartJson.toString()); // send array directly
        response.setStatus(200);
    }
}