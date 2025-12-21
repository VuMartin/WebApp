package main.java.org.example.servlets.customer.SingleStar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SingleStarPojo {

    private String starId;
    private String starName;
    private String starBirthYear;
    private JsonArray movies = new JsonArray();

    private SingleStarPojo() {}

    static SingleStarPojo builder() {
        return new SingleStarPojo();
    }

    public SingleStarPojo setStarId(String starId) {
        this.starId = starId;
        return this;
    }

    public SingleStarPojo setStarName(String starName) {
        this.starName = starName;
        return this;
    }

    public SingleStarPojo setStarBirthYear(String starBirthYear) {
        this.starBirthYear = starBirthYear;
        return this;
    }

    public SingleStarPojo addMovie(String movieId, String movieTitle) {
        JsonObject movie = new JsonObject();
        movie.addProperty("id", movieId);
        movie.addProperty("title", movieTitle);
        this.movies.add(movie);
        return this;
    }

    JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("star_id", this.starId);
        jsonObject.addProperty("star_name", this.starName);
        jsonObject.addProperty("star_dob", this.starBirthYear);
        jsonObject.add("movies", this.movies);
        return jsonObject;
    }
}