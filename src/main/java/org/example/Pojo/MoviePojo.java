package main.java.org.example.Pojo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MoviePojo {

    private String id;
    private String title;
    private String year;
    private String director;
    private String rating;

    private final JsonArray genres = new JsonArray();
    private final JsonArray stars = new JsonArray();

    private MoviePojo() {}

    public static MoviePojo builder() {
        return new MoviePojo();
    }

    public MoviePojo setId(String id) {
        this.id = id;
        return this;
    }

    public MoviePojo setTitle(String title) {
        this.title = title;
        return this;
    }

    public MoviePojo setYear(String year) {
        this.year = year;
        return this;
    }

    public MoviePojo setDirector(String director) {
        this.director = director;
        return this;
    }

    public MoviePojo setRating(String rating) {
        this.rating = rating;
        return this;
    }

    public void addGenre(GenrePojo genre) {
        this.genres.add(genre.toJson());
    }

    public void addStar(StarPojo star) {
        this.stars.add(star.toJson());
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("movieID", id);
        obj.addProperty("movieTitle", title);
        obj.addProperty("movieYear", year);
        obj.addProperty("movieDirector", director);
        obj.addProperty("movieRating", rating);
        obj.add("movieGenres", genres);
        obj.add("movieStars", stars);
        return obj;
    }
}