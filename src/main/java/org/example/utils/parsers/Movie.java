package main.java.org.example.utils.parsers;

import java.util.List;

public class Movie {
    private String id;        // Generated movie ID (tt...)
    private String fid;       // XML film ID from <fid>
    private String title;     // From <t>
    private int year;         // From <year>
    private String director;  // From <dir>
    private List<Integer> genres;  // From <cat>

    public Movie() {}

    public Movie(String fid, String title, int year, String director, List<Integer> genres) {
        this.fid = fid;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFid() { return fid; }
    public void setFid(String fid) { this.fid = fid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public List<Integer> getGenres() { return genres; }
    public void setGenres(List<Integer> genres) { this.genres = genres; }
    public void addGenreID(int genre) { genres.add(genre); }

    @Override
    public String toString() {
        return "Movie [id=" + id + ", fid=" + fid + ", title=" + title +
                ", year=" + year + ", director=" + director + ", " +
                "genres=" + genres + "]";
    }
}