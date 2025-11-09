package main.java.org.example.utils.parsers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;

import org.xml.sax.helpers.DefaultHandler;

public class MovieParser extends DefaultHandler {
    private final Connection conn;
    private final GenreHelper genreHelper;
    private Movie tempMovie;
    private String tempVal;
    private String currentDirk;
    private String mainDirector;
    private final Map<String, String> movieIDMap;
    private final List<Movie> movieBatch;
    private final List<Integer> genreIdBatch;
    private final List<String> movieIdBatch;
    private int movieIdCounter;
    private static final int BATCH_SIZE = 1000;

    public MovieParser(Connection conn, GenreHelper genreHelper, Map<String, String> movieIDMap) {
        this.conn = conn;
        this.genreHelper = genreHelper;
        this.movieIDMap = movieIDMap;
        movieBatch = new ArrayList<>();
        genreIdBatch = new ArrayList<>();
        movieIdBatch = new ArrayList<>();
        initializeMovieIdCounter();
    }

    public void parse() throws Exception {
        // Create a factory that creates new SAX parers
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        // Build a new parser from the factory
        SAXParser saxParser = saxParserFactory.newSAXParser();
        // Parses the file using the overridden methods below
        saxParser.parse("src/main/resources/xml/mains243.xml", this);
    }

    private String generateMovieId() {
        movieIdCounter++;
        return String.format("tt%07d", movieIdCounter);
    }
    private void initializeMovieIdCounter() {
        String sql = "SELECT MAX(id) as max_id FROM movies";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String maxId = rs.getString("max_id");
                if (maxId != null && maxId.startsWith("tt")) {
                    movieIdCounter = Integer.parseInt(maxId.substring(2));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Called once at the beginning of parsing a new document
    @Override
    public void startDocument() {
        System.out.println("Starting to parse movies...");
    }

    // Overrides the method that is called whenever the parser encounters the starting tag of an XML element
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        // Reset the temporary value we are storing
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            // We encountered a new film tag and create a new instance of film
            tempMovie = new Movie();
            tempMovie.setGenres(new ArrayList<>());
        } else if (qName.equalsIgnoreCase("dirk")) {
            currentDirk = null;
        }
    }

    // Overrides the method that is called whenever the parser encounters the characters in between XML start and
    // end elements
    @Override
    public void characters(char[] ch, int start, int length) {
        tempVal = new String(ch, start, length);
    }

    // Overrides the method that is called whenever the parser encounters the ending tag of an XML element
    @Override
    public void endElement(String uri, String localName, String qName) {
        tempVal = tempVal.trim();
        if (qName.equalsIgnoreCase("film")) {
            // If no director found for this film, use the main director
            if (tempMovie.getDirector() == null) {
                tempMovie.setDirector(mainDirector);
            }
            insertMovieAndGenreToDatabase(tempMovie);
        } else if (qName.equalsIgnoreCase("dirname")) {
            mainDirector = tempVal; // Store main director for all films in this section
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                tempMovie.setYear(Integer.parseInt(tempVal));
            } catch (NumberFormatException e) {
                System.out.println("Invalid year: " + tempVal + " - using year 0");
            }
        } else if (qName.equalsIgnoreCase("dirk")) {
            currentDirk = tempVal;
        } else if (qName.equalsIgnoreCase("dirn")) {
            // Use FIRST director marked "R" (canonical)
            if (tempMovie.getDirector() == null && "R".equals(currentDirk)) {
                tempMovie.setDirector(tempVal);
            }
        } else if (qName.equalsIgnoreCase("fid") || qName.equalsIgnoreCase("filmed")) {
            tempMovie.setFid(tempVal);
        } else if (qName.equalsIgnoreCase("cat") || qName.equalsIgnoreCase("cattext")) {
            genreHelper.processAllGenres(tempMovie, tempVal);
        }
    }

    private void insertMovieAndGenreToDatabase(Movie movie) {
        if (movie.getFid() == null) {
            System.out.println("Movie missing fid: " + movie.getTitle());
            return;
        }
        if (movieIDMap.containsKey(movie.getFid().toLowerCase()) || movieExists(movie)) {
            return;
        }
        String movieId = generateMovieId();
        movie.setId(movieId);
        movieIDMap.put(movie.getFid().toLowerCase(), movieId);

        movieBatch.add(movie);

        for (int genreId : movie.getGenres()) {
            genreIdBatch.add(genreId);
            movieIdBatch.add(movieId);
        }

        if (movieBatch.size() >= BATCH_SIZE) {
            insertBatch();
        }
    }

    private void insertBatch() {
        try {
            String movieSql = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(movieSql)) {
                for (Movie movie : movieBatch) {
                    stmt.setString(1, movie.getId());
                    stmt.setString(2, movie.getTitle());
                    stmt.setInt(3, movie.getYear());
                    stmt.setString(4, movie.getDirector());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // Batch insert genres (your clean approach)
            String genreSql = "INSERT IGNORE INTO genres_in_movies (genre_id, movie_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(genreSql)) {
                for (int i = 0; i < genreIdBatch.size(); i++) {
                    stmt.setInt(1, genreIdBatch.get(i));
                    stmt.setString(2, movieIdBatch.get(i));
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

        } catch (SQLException e) {
            System.out.println("Batch insert error for " + movieBatch.size() + " movies");
            e.printStackTrace();
        } finally {
            movieBatch.clear();
            genreIdBatch.clear();
            movieIdBatch.clear();
        }
    }

    private boolean movieExists(Movie movie) {
        if (movie.getTitle() == null || movie.getDirector() == null ||  movie.getFid() == null) {
            System.out.println("Movie missing title or director or fid - skipping");
            return true;
        }

        String sql = "SELECT id FROM movies WHERE title = ? AND year = ? AND director = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, movie.getTitle());
            stmt.setInt(2, movie.getYear());
            stmt.setString(3, movie.getDirector());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                movieIDMap.put(movie.getFid().toLowerCase(), rs.getString("id"));
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    @Override
    public void endDocument() {
        if (!movieBatch.isEmpty()) {
            insertBatch();
            System.out.println("Flushed final batch of movies");
        }
    }
}