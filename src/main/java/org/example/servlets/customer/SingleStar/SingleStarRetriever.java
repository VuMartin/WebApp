package main.java.org.example.servlets.customer.SingleStar;

import com.google.gson.JsonObject;

import java.sql.SQLException;

public interface SingleStarRetriever {
    JsonObject getSingleStar(String starId) throws SQLException;
}