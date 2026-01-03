package main.java.org.example.Pojo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class StarPojo {

    private String starId;
    private String starName;
    private String starBirthYear;
    private List<MoviePojo> movies = new ArrayList<>();
    private int movieCount;

    private StarPojo() {}

    public static StarPojo builder() {
        return new StarPojo();
    }

    public StarPojo setStarId(String starId) {
        this.starId = starId;
        return this;
    }

    public StarPojo setStarName(String starName) {
        this.starName = starName;
        return this;
    }

    public StarPojo setStarBirthYear(String starBirthYear) {
        this.starBirthYear = starBirthYear;
        return this;
    }

    public StarPojo addMovie(MoviePojo movie) {
        this.movies.add(movie);
        return this;
    }

    public StarPojo setMovieCount(int movieCount) {
        this.movieCount = movieCount;
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("star_id", this.starId);
        jsonObject.addProperty("star_name", this.starName);
        if (this.starBirthYear != null) jsonObject.addProperty("star_dob", this.starBirthYear);
        if (this.movieCount != 0) jsonObject.addProperty("star_movieCount", this.movieCount);
        JsonArray arr = new JsonArray();
        for (MoviePojo m : this.movies) {
            arr.add(m.toJson());
        }
        jsonObject.add("movies", arr);
        return jsonObject;
    }
}