package main.java.org.example.servlets.customer;

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
        response.setContentType("application/json"); // JSON output

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
            switch (action) {
                case "remove":
                    cart.remove(movieID);
                    break;
                case "update":
                    CartItem itemToUpdate = cart.get(movieID);
                    if (itemToUpdate != null) {
                        int newQty = Integer.parseInt(request.getParameter("quantity"));
                        itemToUpdate.setQuantity(newQty);
                    }
                    break;
                case "empty":
                    cart.clear();
                    break;
                default: // add
                    CartItem item = cart.getOrDefault(movieID, new CartItem(movieID, title, price, 0));
                    item.setQuantity(item.getQuantity() + 1);
                    cart.put(movieID, item);
                    break;
            }

            // Build JSON response
            JsonArray cartJson = new JsonArray();
            double total = 0;
            int totalCount = 0;
            for (CartItem cItem : cart.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("movieID", cItem.getMovieID());
                obj.addProperty("title", cItem.getTitle());
                obj.addProperty("quantity", cItem.getQuantity());
                obj.addProperty("price", cItem.getPrice());
                total += cItem.getPrice() * cItem.getQuantity();
                totalCount += cItem.getQuantity();
                cartJson.add(obj);
            }

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("firstName", (String) session.getAttribute("firstName"));
            responseJson.addProperty("cardNumber", (String) session.getAttribute("creditCardID"));
            responseJson.add("items", cartJson);
            responseJson.addProperty("total", total);
            responseJson.addProperty("totalCount", totalCount);

            response.getWriter().write(responseJson.toString());
            response.setStatus(200);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // JSON output

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("cart") == null) {
            response.getWriter().write("[]"); // empty cart
            return;
        }

        Map<String, CartItem> cart = (Map<String, CartItem>) session.getAttribute("cart");

        JsonArray cartJson = new JsonArray();
        double total = 0;
        int totalCount = 0;

        synchronized(cart) {
            for (CartItem item : cart.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("movieID", item.getMovieID());
                obj.addProperty("title", item.getTitle());
                obj.addProperty("quantity", item.getQuantity());
                obj.addProperty("price", item.getPrice());
                total += item.getPrice() * item.getQuantity();
                totalCount += item.getQuantity();
                cartJson.add(obj);
            }
        }

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("firstName", (String) session.getAttribute("firstName"));
        responseJson.addProperty("orderNumber", (Integer) session.getAttribute("orderNumber"));
        responseJson.addProperty("cardNumber", (String) session.getAttribute("creditCardID"));
        responseJson.add("items", cartJson);
        responseJson.addProperty("total", total);
        responseJson.addProperty("totalCount", totalCount);

        response.getWriter().write(responseJson.toString());
        response.setStatus(200);
    }
}