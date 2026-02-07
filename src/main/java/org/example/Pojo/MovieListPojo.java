package main.java.org.example.Pojo;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.LinkedHashSet;
import java.util.Set;

public class MovieListPojo {

    private int currentPage;
    private int pageSize;
    private String sortPrimaryField;
    private String sortPrimaryOrder;
    private String sortSecondaryField;
    private String sortSecondaryOrder;
    private String prefix;

    private Set<MoviePojo> movies = new LinkedHashSet<>();

    // Builder pattern
    public static MovieListPojo builder() {
        return new MovieListPojo();
    }

    public MovieListPojo setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        return this;
    }

    public MovieListPojo setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public MovieListPojo setSortPrimaryField(String sortPrimaryField) {
        this.sortPrimaryField = sortPrimaryField;
        return this;
    }

    public MovieListPojo setSortPrimaryOrder(String sortPrimaryOrder) {
        this.sortPrimaryOrder = sortPrimaryOrder;
        return this;
    }

    public MovieListPojo setSortSecondaryField(String sortSecondaryField) {
        this.sortSecondaryField = sortSecondaryField;
        return this;
    }

    public MovieListPojo setSortSecondaryOrder(String sortSecondaryOrder) {
        this.sortSecondaryOrder = sortSecondaryOrder;
        return this;
    }

    public MovieListPojo setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public MovieListPojo addMovie(MoviePojo movie) {
        this.movies.add(movie);
        return this;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("currentPage", currentPage);
        obj.addProperty("pageSize", pageSize);
        obj.addProperty("sortPrimaryField", sortPrimaryField);
        obj.addProperty("sortPrimaryOrder", sortPrimaryOrder);
        obj.addProperty("sortSecondaryField", sortSecondaryField);
        obj.addProperty("sortSecondaryOrder", sortSecondaryOrder);
        obj.addProperty("prefix", prefix);

        JsonArray moviesArray = new JsonArray();
        for (MoviePojo movie : movies) {
            moviesArray.add(movie.toJson());
        }
        obj.add("movies", moviesArray);
        return obj;
    }
}