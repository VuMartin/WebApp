package main.java.org.example.servlets.customer.SingleMovie;

import com.google.gson.JsonObject;

import java.sql.SQLException;

public interface SingleMovieRetriever {
    JsonObject getSingleMovie(String movieId) throws SQLException;
}