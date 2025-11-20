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

    private static final String STARS_QUERY =
            "SELECT * " +
            "FROM stars ";

    private static final String CUSTOMERS_QUERY =
            "SELECT id, first_name, last_name, credit_card_id, address, email, password " +
                    "FROM customers";

    private static final String CREDITCARDS_QUERY =
            "SELECT id, first_name, last_name, expiration " +
                    "FROM credit_cards";

    private static final String EMPLOYEES_QUERY =
            "SELECT email, password, fullname " +
                    "FROM employees";

    public static void main(String[] args) throws SQLException {
//        Map<String, Object> starsDocuments = readStarsFromMySQL();
//        writeStarsToMongo(starsDocuments);

//        Map<String, Object> moviesDocuments = readMoviesFromMySQL();
//        writeMoviesToMongo(moviesDocuments);

//        Map<String, Object> customersDocuments = readCustomersFromMySQL();
//        writeCustomersToMongo(customersDocuments);
//
//        Map<String, Object> creditCardsDocuments = readCreditCardsFromMySQL();
//        writeCreditCardsToMongo(creditCardsDocuments);
//
//        Map<String, Object> employeesDocuments = readEmployeesFromMySQL();
//        writeEmployeesToMongo(employeesDocuments);
//
//        Map<String, Object> salesDocuments = readSalesFromMySQL();
//        writeSalesToMongo(salesDocuments);
    }

    private static void writeMoviesToMongo(Map<String, Object> movieDocuments) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            // This reuses the myNewDB database from the Mongo tutorial. You may want to create a better named database
            MongoDatabase myNewDB = mongoClient.getDatabase("moviedb");
            MongoCollection<Document> moviesCollection = myNewDB.getCollection("movies");
            List<Document> moviedocs = (List<Document>) movieDocuments.get("movies");
            moviesCollection.insertMany(moviedocs);
            System.out.println("Inserted all movies in bulk");

//            MongoCollection<Document> countersCollection = myNewDB.getCollection("counters");
//            Document movieCounter = (Document) movieDocuments.get("movie_counter");
//            countersCollection.insertOne(movieCounter);
//            System.out.println("Inserted movie ID into counters collection");
        }
    }

    private static void writeStarsToMongo(Map<String, Object> starDocuments) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            // This reuses the myNewDB database from the Mongo tutorial. You may want to create a better named database
            MongoDatabase myNewDB = mongoClient.getDatabase("moviedb");
            MongoCollection<Document> starsCollection = myNewDB.getCollection("stars");
            List<Document> starsdocs = (List<Document>) starDocuments.get("stars");
            starsCollection.insertMany(starsdocs);
            System.out.println("Inserted all stars in bulk");

            MongoCollection<Document> countersCollection = myNewDB.getCollection("counters");
            Document starCounter = (Document) starDocuments.get("star_counter");
            countersCollection.insertOne(starCounter);
            System.out.println("Inserted star ID into counters collection");
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

            MongoCollection<Document> counterCollection = myNewDB.getCollection("counters");
            Document counterDoc = (Document) salesDocuments.get("orderID");
            counterCollection.insertOne(counterDoc);

            System.out.println("Inserted order ID into counters collection");
        }
    }

    private static void writeCustomersToMongo(Map<String, Object> customersDocuments) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase myNewDB = mongoClient.getDatabase("moviedb");
            MongoCollection<Document> customersCollection = myNewDB.getCollection("customers");

            List<Document> customers = (List<Document>) customersDocuments.get("customers");
            customersCollection.insertMany(customers);
            System.out.println("Inserted " + customers.size() + " customers");
        }
    }

    private static void writeCreditCardsToMongo(Map<String, Object> creditCardDocuments) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase myNewDB = mongoClient.getDatabase("moviedb");
            MongoCollection<Document> creditCardsCollection = myNewDB.getCollection("credit_cards");

            List<Document> creditCards = (List<Document>) creditCardDocuments.get("credit_cards");
            creditCardsCollection.insertMany(creditCards);
            System.out.println("Inserted " + creditCards.size() + " credit cards");
        }
    }

    private static void writeEmployeesToMongo(Map<String, Object> employeeDocuments) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase myNewDB = mongoClient.getDatabase("moviedb");
            MongoCollection<Document> employeesCollection = myNewDB.getCollection("employees");

            List<Document> employees = (List<Document>) employeeDocuments.get("employees");
            employeesCollection.insertMany(employees);
            System.out.println("Inserted " + employees.size() + " employees");
        }
    }


    private static Map<String, Object> readMoviesFromMySQL() throws SQLException {
        List<Document> moviesList = new ArrayList<>();
        Set<String> seenStars = new HashSet<>();
        int maxMovieCount = 0;
        try (Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(QUERY)) {
            String lastMovieId = null;
            Document movieDoc = null;
            List<Document> stars = new ArrayList<>();
            List<String> genres = new ArrayList<>();
            while (resultSet.next()) {
                String movieId = resultSet.getString("movie_id");
                String numericPart = movieId.substring(2);
                int num = Integer.parseInt(numericPart);
                if (num > maxMovieCount) maxMovieCount = num;
                if (!movieId.equals(lastMovieId)) {
                    if (movieDoc != null) {
                        movieDoc.append("stars", stars)
                                .append("genres", genres);
                        moviesList.add(movieDoc);
                    }
                    Float rating = resultSet.getObject("rating", Float.class);
                    movieDoc = new Document("_id", movieId)
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
            Document movieCounterDoc = new Document("_id", "movie_id")
                    .append("seq", maxMovieCount);
            Map<String, Object> result = new HashMap<>();
            result.put("movies", moviesList);
            result.put("movie_counter", movieCounterDoc);
            return result;
        }
    }

    private static Map<String, Object> readSalesFromMySQL() throws SQLException {
        List<Document> salesList = new ArrayList<>();
        int orderID = 0;

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
                            .append("_id", ++orderID)
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
        Document counterDoc = new Document("_id", "order_id")
                .append("seq", orderID);
        Map<String, Object> result = new HashMap<>();
        result.put("sales", salesList);
        result.put("orderID", counterDoc);
        return result;
    }

    private static Map<String, Object> readStarsFromMySQL() throws SQLException {
        List<Document> starsList = new ArrayList<>();
        int maxStarNum = 0;

        try (Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(STARS_QUERY)) {

            while (rs.next()) {
                String starId = rs.getString("id");
                String name = rs.getString("name");
                String numericPart = starId.substring(2);
                int num = Integer.parseInt(numericPart);
                if (num > maxStarNum) maxStarNum = num;
                Integer birthYear = rs.getObject("birth_year", Integer.class);
                starsList.add(new Document("_id", starId)
                        .append("name", name)
                        .append("birth_year", birthYear));
            }
        }

        Document starCounterDoc = new Document("_id", "star_id")
                .append("seq", maxStarNum);

        Map<String, Object> result = new HashMap<>();
        result.put("stars", starsList);
        result.put("star_counter", starCounterDoc);

        return result;
    }

    private static Map<String, Object> readCustomersFromMySQL() throws SQLException {
        List<Document> customersList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(CUSTOMERS_QUERY)) {

            while (rs.next()) {
                Document customerDoc = new Document("_id", rs.getInt("id"))
                        .append("first_name", rs.getString("first_name"))
                        .append("last_name", rs.getString("last_name"))
                        .append("credit_card_id", rs.getString("credit_card_id"))
                        .append("address", rs.getString("address"))
                        .append("email", rs.getString("email"))
                        .append("password", rs.getString("password"));
                customersList.add(customerDoc);
            }
        }

        System.out.println("Read " + customersList.size() + " customers from MySQL");

        Map<String, Object> result = new HashMap<>();
        result.put("customers", customersList);
        return result;
    }

    private static Map<String, Object> readCreditCardsFromMySQL() throws SQLException {
        List<Document> creditCardsList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(CREDITCARDS_QUERY)) {

            while (rs.next()) {
                Document ccDoc = new Document("_id", rs.getString("id"))
                        .append("first_name", rs.getString("first_name"))
                        .append("last_name", rs.getString("last_name"))
                        .append("expiration", rs.getDate("expiration"));
                creditCardsList.add(ccDoc);
            }
        }

        System.out.println("Read " + creditCardsList.size() + " credit cards from MySQL");

        Map<String, Object> result = new HashMap<>();
        result.put("credit_cards", creditCardsList);
        return result;
    }

    private static Map<String, Object> readEmployeesFromMySQL() throws SQLException {
        List<Document> employeesList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(EMPLOYEES_QUERY)) {

            while (rs.next()) {
                Document employeeDoc = new Document("_id", rs.getString("email"))
                        .append("email", rs.getString("email"))
                        .append("password", rs.getString("password"))
                        .append("fullname", rs.getString("fullname"));
                employeesList.add(employeeDoc);
            }
        }

        System.out.println("Read " + employeesList.size() + " employees from MySQL");

        Map<String, Object> result = new HashMap<>();
        result.put("employees", employeesList);
        return result;
    }


}