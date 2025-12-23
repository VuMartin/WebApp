package main.java.org.example.servlets.customer.SingleMovie;

import com.google.gson.JsonObject;

public class StarPojo {

    private String id;
    private String name;
    private int movieCount;

    private StarPojo() {}

    public static StarPojo builder() {
        return new StarPojo();
    }

    public StarPojo setId(String id) {
        this.id = id;
        return this;
    }

    public StarPojo setName(String name) {
        this.name = name;
        return this;
    }

    public StarPojo setMovieCount(int movieCount) {
        this.movieCount = movieCount;
        return this;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        obj.addProperty("movieCount", movieCount);
        return obj;
    }
}