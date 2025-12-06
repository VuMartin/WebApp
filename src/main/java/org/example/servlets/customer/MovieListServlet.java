package main.java.org.example.servlets.customer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/topmovies
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/html/customer/movies.html
// This is a code freeze branch for project 4
// This annotation maps this Java Servlet Class to a URL
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/topmovies")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private SessionAttribute<String> nameAttribute;
    private SessionAttribute<String> titleAttr;
    private SessionAttribute<String> yearAttr;
    private SessionAttribute<String> directorAttr;
    private SessionAttribute<String> genreAttr;
    private SessionAttribute<String> starAttr;
    private SessionAttribute<String> prefixAttr;
    private SessionAttribute<String> sortPrimaryFieldAttr;
    private SessionAttribute<String> sortPrimaryOrderAttr;
    private SessionAttribute<String> sortSecondaryFieldAttr;
    private SessionAttribute<String> sortSecondaryOrderAttr;
    private SessionAttribute<Integer> pageSizeAttr;
    private SessionAttribute<Integer> offsetAttr;
    private SessionAttribute<Integer> currPageAttr;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> moviesCollection;

    public void init(ServletConfig config) {
        try {
            mongoClient = MongoClients.create(
                    "mongodb+srv://vum4_db_user:qnF25ATNIHOrgRSh@cluster0.n8qtory.mongodb.net/moviedb?retryWrites=true&w=majority"
            );
            //       mongodb+srv://vum4_db_user:qnF25ATNIHOrgRSh@cluster0.n8qtory.mongodb.net/?appName=Cluster0
            database = mongoClient.getDatabase("moviedb");
            moviesCollection = database.getCollection("movies");
        } catch (Exception e) {
            mongoClient = MongoClients.create("mongodb://mytestuser:My6$Password@localhost:27017/moviedb?authSource=moviedb");
            database = mongoClient.getDatabase("moviedb");
            moviesCollection = database.getCollection("movies");
            e.printStackTrace();
        }
        this.nameAttribute = new SessionAttribute<>(String.class, "name");
        titleAttr = new SessionAttribute<>(String.class, "title");
        yearAttr = new SessionAttribute<>(String.class, "year");
        directorAttr = new SessionAttribute<>(String.class, "director");
        genreAttr = new SessionAttribute<>(String.class, "genre");
        starAttr = new SessionAttribute<>(String.class, "star");
        sortPrimaryFieldAttr = new SessionAttribute<>(String.class, "sortField");
        sortPrimaryOrderAttr = new SessionAttribute<>(String.class, "sortOrder");
        sortSecondaryFieldAttr = new SessionAttribute<>(String.class, "sortSecondary");
        sortSecondaryOrderAttr = new SessionAttribute<>(String.class, "sortSecondaryOrder");
        pageSizeAttr = new SessionAttribute<>(Integer.class, "pageSize");
        offsetAttr = new SessionAttribute<>(Integer.class, "offset");
        currPageAttr = new SessionAttribute<>(Integer.class, "page");
        prefixAttr = new SessionAttribute<>(String.class, "prefix");
    }

    class SessionAttribute<T> {
        private final Class<T> clazz;
        private final String name;

        SessionAttribute(Class<T> clazz, String name) {
            this.name = name;
            this.clazz = clazz;
        }

        T get(HttpSession session) {
            return clazz.cast(session.getAttribute(name));
        }

        void set(HttpSession session, T value) {
            session.setAttribute(name, value);
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.nanoTime();
        long totalDbTime = 0;
        response.setContentType("application/json"); // Response mime type
        HttpSession session = request.getSession();
        String title;
        String year;
        String director;
        String genre;
        String star;
        String prefix;
        String sortPrimaryField;
        String sortPrimaryOrder;
        String sortSecondaryField;
        String sortSecondaryOrder;
        int pageSize;
        int offset;
        int currentPage;

        String back = request.getParameter("restore");
        if (back != null && back.equals("true")) {
            title = titleAttr.get(session);
            year = yearAttr.get(session);
            director = directorAttr.get(session);
            genre = genreAttr.get(session);
            star = starAttr.get(session);
            pageSize = pageSizeAttr.get(session);
            offset = offsetAttr.get(session);
            sortPrimaryField = sortPrimaryFieldAttr.get(session);
            sortPrimaryOrder = sortPrimaryOrderAttr.get(session);
            sortSecondaryField = sortSecondaryFieldAttr.get(session);
            sortSecondaryOrder = sortSecondaryOrderAttr.get(session);
            currentPage = currPageAttr.get(session);
            prefix = prefixAttr.get(session);
        } else {
            title = request.getParameter("title");
            year = request.getParameter("year");
            director = request.getParameter("director");
            genre = request.getParameter("genre");
            star = request.getParameter("star");
            String pageSizeStr = request.getParameter("pageSize");
            pageSize = (pageSizeStr != null) ? Integer.parseInt(pageSizeStr) : 10;
            String offsetStr = request.getParameter("offset");
            offset = (offsetStr != null) ? Integer.parseInt(offsetStr) : 0;
            sortPrimaryField = request.getParameter("sortPrimaryField");
            sortPrimaryOrder = request.getParameter("sortPrimaryOrder");
            sortSecondaryField = request.getParameter("sortSecondaryField");
            sortSecondaryOrder = request.getParameter("sortSecondaryOrder");
            String pageStr = request.getParameter("currentPage");
            currentPage = (pageStr != null) ? Integer.parseInt(pageStr) : 1;
            prefix = request.getParameter("prefix");

            titleAttr.set(session, title);
            yearAttr.set(session, year);
            directorAttr.set(session, director);
            genreAttr.set(session, genre);
            starAttr.set(session, star);
            pageSizeAttr.set(session, pageSize);
            offsetAttr.set(session, offset);
            currPageAttr.set(session, currentPage);
            sortPrimaryFieldAttr.set(session, sortPrimaryField);
            sortPrimaryOrderAttr.set(session, sortPrimaryOrder);
            sortSecondaryFieldAttr.set(session, sortSecondaryField);
            sortSecondaryOrderAttr.set(session, sortSecondaryOrder);
            prefixAttr.set(session, prefix);
        }

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        try {
            List<Bson> filters = new ArrayList<>();
            List<Document> pipeline = new ArrayList<>();

            if (title != null && !title.isEmpty()) {
                Document searchStage = Utils.convertToMongoText(title);
                pipeline.add(searchStage);
            }
            if (year != null && !year.isEmpty())
                filters.add(Filters.eq("year", Integer.parseInt(year)));
            if (director != null && !director.isEmpty())
                filters.add(Filters.regex("director", ".*" + Pattern.quote(director) + ".*", "i"));
            if (genre != null && !genre.isEmpty())
                filters.add(Filters.in("genres", genre));
            if (star != null && !star.isEmpty())
                filters.add(Filters.elemMatch("stars",
                        Filters.regex("name", ".*" + Pattern.quote(star) + ".*", "i")));
            if (prefix != null && !prefix.isEmpty())
                filters.add(Filters.regex("title", "^" + Pattern.quote(prefix), "i"));

            if (!filters.isEmpty()) pipeline.add(new Document("$match", Filters.and(filters)));

            String primaryCol = "title".equals(sortPrimaryField) ? "title" : "rating";
            String primaryDir = "asc".equals(sortPrimaryOrder) ? "ASC" : "DESC";
            String secondaryCol = "title".equals(sortSecondaryField) ? "title" : "rating";
            String secondaryDir = "desc".equals(sortSecondaryOrder) ? "DESC" : "ASC"; // corrected

            Document sortStage = new Document("$sort",
                    new Document(primaryCol, primaryDir.equals("ASC") ? 1 : -1)
                            .append(secondaryCol, secondaryDir.equals("ASC") ? 1 : -1));
            pipeline.add(sortStage);
            pipeline.add(new Document("$skip", offset));
            pipeline.add(new Document("$limit", pageSize));
            long dbStart1 = System.nanoTime();
            AggregateIterable<Document> movieResults = moviesCollection.aggregate(pipeline);
            long dbEnd1 = System.nanoTime();
            totalDbTime += (dbEnd1 - dbStart1);

            pipeline = new ArrayList<>();

            if (title != null && !title.isEmpty()) {
                Document searchStage = Utils.convertToMongoText(title);
                pipeline.add(searchStage);
            }
            filters = new ArrayList<>();
            if (year != null && !year.isEmpty())
                filters.add(Filters.eq("year", Integer.parseInt(year)));
            if (director != null && !director.isEmpty())
                filters.add(Filters.regex("director", ".*" + Pattern.quote(director) + ".*", "i"));
            if (genre != null && !genre.isEmpty())
                filters.add(Filters.in("genres", genre));
            if (star != null && !star.isEmpty())
                filters.add(Filters.elemMatch("stars",
                        Filters.regex("name", ".*" + Pattern.quote(star) + ".*", "i")));
            if (prefix != null && !prefix.isEmpty())
                filters.add(Filters.regex("title", "^" + Pattern.quote(prefix), "i"));

            if (!filters.isEmpty()) pipeline.add(new Document("$match", Filters.and(filters)));
            pipeline.add(new Document("$count", "totalCount"));
            long dbStart2 = System.nanoTime();
            AggregateIterable<Document> countResult = moviesCollection.aggregate(pipeline);
            long dbEnd2 = System.nanoTime();
            totalDbTime += (dbEnd2 - dbStart2);
            long totalCount = 0;
            Document countDoc = countResult.first();
            if (countDoc != null) totalCount = countDoc.get("totalCount", Number.class).longValue();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("totalCount", totalCount);
            jsonObject.addProperty("currentPage", currentPage);
            jsonObject.addProperty("pageSize", pageSize);
            jsonObject.addProperty("sortPrimaryField", sortPrimaryField);
            jsonObject.addProperty("sortPrimaryOrder", sortPrimaryOrder);
            jsonObject.addProperty("sortSecondaryField", sortSecondaryField);
            jsonObject.addProperty("sortSecondaryOrder", sortSecondaryOrder);
            jsonObject.addProperty("prefix", prefix);
            JsonArray moviesArray = new JsonArray();
            jsonObject.add("movies", moviesArray);

            for (Document doc : movieResults) {
                JsonObject movieObject = new JsonObject();

                movieObject.addProperty("movieID", doc.getString("_id"));
                movieObject.addProperty("movieTitle", doc.getString("title"));
                movieObject.addProperty("movieYear", doc.getInteger("year"));
                movieObject.addProperty("movieDirector", doc.getString("director"));
                Double rating = doc.getDouble("rating"); // use Double object to allow null
                if (rating != null) {
                    double roundedRating = Math.round(rating * 100.0) / 100.0;
                    movieObject.addProperty("movieRating", roundedRating);
                } else movieObject.addProperty("movieRating", "N/A");
                List<String> genres = doc.getList("genres", String.class);
                List<String> topGenres = genres.stream()
                        .sorted()
                        .limit(3)
                        .collect(Collectors.toList());
                movieObject.add("movieGenres", new Gson().toJsonTree(topGenres));
                List<Document> stars = (List<Document>) doc.get("stars");
                List<Document> topStars = stars.stream()
                        .sorted(Comparator
                                .comparingInt((Document s) -> s.getInteger("movie_count")).reversed()
                                .thenComparing(s -> s.getString("name")))
                        .limit(3)
                        .collect(Collectors.toList());
                movieObject.add("movieStars", new Gson().toJsonTree(topStars));
                moviesArray.add(movieObject);
            }

            // Log to localhost log
            request.getServletContext().log("getting " + moviesArray.size() + " results");

            // Write JSON string to output
            out.write(jsonObject.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            Utils.writeTimingToFile(totalTime, totalDbTime, "MovieServlet-Mongo", title);
            out.close();
        }
    }
    @Override
    public void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
