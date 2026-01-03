package main.java.org.example.servlets.customer.MovieList;

import com.google.gson.JsonObject;
import main.java.org.example.Pojo.MoviePojo;

import java.sql.SQLException;

import java.sql.Connection;
import java.util.List;

public interface MovieListRetriever {
    JsonObject getMovieList(
            String title,
            String year,
            String director,
            String genre,
            String star,
            String prefix,
            String sortPrimaryField,
            String sortPrimaryOrder,
            String sortSecondaryField,
            String sortSecondaryOrder,
            int currentPage,
            int pageSize,
            int offset) throws SQLException;

    JsonObject getPagination(
            String title,
            String year,
            String director,
            String genre,
            String star,
            String prefix,
            JsonObject jsonObject) throws SQLException;
}