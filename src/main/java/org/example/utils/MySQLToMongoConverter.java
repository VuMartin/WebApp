package main.java.org.example.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Retrieves necessary info from one movie from MySQL and writes it as a document to MongoDB
public class MySQLToMongoConverter {
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/moviedb";
    private static final String MYSQL_USER = "mytestuser";
    private static final String MYSQL_PASS = "My6$Password";
    private static final String MONGO_URI = "mongodb://mytestuser:My6$Password@localhost:27017/moviedb?authSource=moviedb";
    private static final String QUERY =
            "SELECT m.id AS movie_id, m.title, m.year, m.director, " +
                    "s.id AS star_id, s.name AS star_name, s.birth_year AS star_birth, " +
                    "g.name AS genre_name, " +
                    "r.rating, " +
                    "cnt.movie_count AS movie_count " +
                    "FROM movies m " +
                    "LEFT JOIN stars_in_movies sim ON m.id = sim.movie_id " +
                    "LEFT JOIN stars s ON sim.star_id = s.id " +
                    "LEFT JOIN genres_in_movies gim ON m.id = gim.movie_id " +
                    "LEFT JOIN genres g ON gim.genre_id = g.id " +
                    "LEFT JOIN ratings r ON m.id = r.movie_id " +
                    "LEFT JOIN ( " +
                    "    SELECT star_id, COUNT(*) AS movie_count " +
                    "    FROM stars_in_movies " +
                    "    GROUP BY star_id " +
                    ") AS cnt ON cnt.star_id = s.id " +
                    "ORDER BY m.id";

    public static void main(String[] args) throws SQLException {
        List<Document> moviesDocument = readMoviesFromMySQL();
        writeMoviesToMongo(moviesDocument);

//        List<Document> customersDocument = readCustomersFromMySQL();
//        writeMoviesToMongo(customersDocument);
//
//        List<Document> employeesDocument = readEmployeesFromMySQL();
//        writeMoviesToMongo(employeesDocument);
//
//        List<Document> salesDocument = readSalesFromMySQL();
//        writeMoviesToMongo(salesDocument);
    }

    private static void writeMoviesToMongo(List<Document> movieDocument) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            // This reuses the myNewDB database from the Mongo tutorial. You may want to create a better named database
            MongoDatabase myNewDB = mongoClient.getDatabase("moviedb");
            MongoCollection<Document> moviesCollection = myNewDB.getCollection("movies");
            moviesCollection.insertMany(movieDocument);
            System.out.println("Inserted all movies in bulk");
        }
    }

    private static List<Document> readMoviesFromMySQL() throws SQLException {
        List<Document> moviesList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(QUERY)) {
            String lastMovieId = null;
            Document movieDoc = null;
            List<Document> stars = new ArrayList<>();
            List<String> genres = new ArrayList<>();
            while (resultSet.next()) {
                String movieId = resultSet.getString("movie_id");
                if (!movieId.equals(lastMovieId)) {
                    if (movieDoc != null) {
                        movieDoc.append("stars", stars)
                                .append("genres", genres);
                        moviesList.add(movieDoc);
                    }
                    movieDoc = new Document("movie_id", movieId)
                            .append("title", resultSet.getString("title"))
                            .append("year", resultSet.getInt("year"))
                            .append("director", resultSet.getString("director"))
                            .append("rating", resultSet.getFloat("rating"));
                    stars = new ArrayList<>();
                    genres = new ArrayList<>();
                    lastMovieId = movieId;
                }
                String starId = resultSet.getString("star_id");
                if (starId != null) {
                    stars.add(new Document("star_id", starId)
                            .append("name", resultSet.getString("star_name"))
                            .append("birth_year", resultSet.getObject("star_birth", Integer.class))
                            .append("movie_count", resultSet.getObject("movie_count", Integer.class)));
                }
                String genreName = resultSet.getString("genre_name");
                if (genreName != null && !genres.contains(genreName)) {
                    genres.add(genreName);
                }
            }
            if (movieDoc != null) {
                movieDoc.append("stars", stars)
                        .append("genres", genres);
                moviesList.add(movieDoc);
            }
            System.out.println("looked through all movie rows");
            return moviesList;
        }
    }
}