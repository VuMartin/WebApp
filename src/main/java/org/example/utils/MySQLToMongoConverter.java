package main.java.org.example.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.*;
import java.util.*;

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
    private static final String SALES_QUERY =
            "SELECT customer_id, sale_date, movie_id, COUNT(*) AS quantity " +
            "FROM sales " +
            "GROUP BY customer_id, sale_date, movie_id " +
            "ORDER BY customer_id, sale_date";

    public static void main(String[] args) throws SQLException {
        List<Document> moviesDocument = readMoviesFromMySQL();
        writeMoviesToMongo(moviesDocument);

//        List<Document> customersDocument = readCustomersFromMySQL();
//        writeMoviesToMongo(customersDocument);

//        List<Document> creditCardsDocument = readCreditCardsFromMySQL();
//        writeCreditCardsToMongo(creditCardsDocument);
//
//        List<Document> employeesDocument = readEmployeesFromMySQL();
//        writeEmployeesToMongo(employeesDocument);
//
//        Map<String, Object> salesDocuments = readSalesFromMySQL();
//        writeSalesToMongo(salesDocuments);
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

    private static void writeSalesToMongo(Map<String, Object> salesDocuments) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            // This reuses the myNewDB database from the Mongo tutorial. You may want to create a better named database
            MongoDatabase myNewDB = mongoClient.getDatabase("moviedb");
            MongoCollection<Document> salesCollection = myNewDB.getCollection("sales");
            List<Document> salesList = (List<Document>) salesDocuments.get("sales");
            salesCollection.insertMany(salesList);
            System.out.println("Inserted all sales in bulk");

            MongoCollection<Document> counterCollection = myNewDB.getCollection("counter");
            Document counterDoc = (Document) salesDocuments.get("orderID");
            counterCollection.insertOne(counterDoc);

            System.out.println("Inserted order ID into counter collection");
        }
    }

    private static List<Document> readMoviesFromMySQL() throws SQLException {
        List<Document> moviesList = new ArrayList<>();
        Set<String> seenStars = new HashSet<>();
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
                    Float rating = resultSet.getObject("rating", Float.class);
                    movieDoc = new Document("movie_id", movieId)
                            .append("title", resultSet.getString("title"))
                            .append("year", resultSet.getInt("year"))
                            .append("director", resultSet.getString("director"))
                            .append("rating", rating);
                    stars = new ArrayList<>();
                    genres = new ArrayList<>();
                    seenStars = new HashSet<>();
                    lastMovieId = movieId;
                }

                String starId = resultSet.getString("star_id");
                if (starId != null && !seenStars.contains(starId)) {
                    stars.add(new Document("star_id", starId)
                            .append("name", resultSet.getString("star_name"))
                            .append("birth_year", resultSet.getObject("star_birth", Integer.class))
                            .append("movie_count", resultSet.getObject("movie_count", Integer.class)));
                    seenStars.add(starId);
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

    private static Map<String, Object> readSalesFromMySQL() throws SQLException {
        List<Document> salesList = new ArrayList<>();
        int orderID = 1;

        try (Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(SALES_QUERY)) {
            String prevCustomer = null;
            String prevDate = null;
            Document currentOrder = null;
            List<Document> currentItems = null;

            while (rs.next()) {
                String customerId = rs.getString("customer_id");
                String saleDate = rs.getString("sale_date");
                if (!customerId.equals(prevCustomer) || !saleDate.equals(prevDate)) {
                    if (currentOrder != null) {
                        currentOrder.append("items", currentItems);
                        salesList.add(currentOrder);
                    }
                    currentItems = new ArrayList<>();
                    currentOrder = new Document()
                            .append("order_id", orderID++)
                            .append("customer_id", customerId)
                            .append("sale_date", saleDate);
                }
                currentItems.add(
                        new Document("movie_id", rs.getString("movie_id"))
                                .append("quantity", rs.getInt("quantity"))
                );

                prevCustomer = customerId;
                prevDate = saleDate;
            }
            if (currentOrder != null) {
                currentOrder.append("items", currentItems);
                salesList.add(currentOrder);
            }
        }
        Document counterDoc = new Document("order_id", orderID);
        Map<String, Object> result = new HashMap<>();
        result.put("sales", salesList);
        result.put("orderID", counterDoc);
        return result;
    }
}