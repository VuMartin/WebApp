package main.java.org.example.servlets.customer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/topmovies
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/html/customer/movies.html
// This annotation maps this Java Servlet Class to a URL
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/topmovies")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;
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

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
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
            pageSize = (pageSizeAttr.get(session) != null) ? pageSizeAttr.get(session) : 10;
            offset = (offsetAttr.get(session) != null) ? offsetAttr.get(session) : 0;
            currentPage = (currPageAttr.get(session) != null) ? currPageAttr.get(session) : 1;
            sortPrimaryField  = (sortPrimaryFieldAttr.get(session) != null) ? sortPrimaryFieldAttr.get(session) : "rating";
            sortPrimaryOrder  = (sortPrimaryOrderAttr.get(session) != null) ? sortPrimaryOrderAttr.get(session) : "desc";
            sortSecondaryField = (sortSecondaryFieldAttr.get(session) != null) ? sortSecondaryFieldAttr.get(session) : "title";
            sortSecondaryOrder = (sortSecondaryOrderAttr.get(session) != null) ? sortSecondaryOrderAttr.get(session) : "asc";
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

        // Get a connection from dataSource and let resource manager close the connection after usage.
        long dbStartConn = System.nanoTime();
        try (Connection conn = dataSource.getConnection()) {
            long dbEndConn = System.nanoTime();
            totalDbTime += (dbEndConn - dbStartConn);

            String createTempTable = "CREATE TEMPORARY TABLE temp_movies ( " +
                    "sort_order INT AUTO_INCREMENT PRIMARY KEY, " +
                    "movie_id VARCHAR(10) NOT NULL, " +
                    "rating FLOAT " +
                    ")";

            Statement dropStmt = conn.createStatement();
            long dbStartDrop = System.nanoTime();
            dropStmt.execute("DROP TEMPORARY TABLE IF EXISTS temp_movies");
            long dbEndDrop = System.nanoTime();
            totalDbTime += (dbEndDrop - dbStartDrop);
            dropStmt.close();

            Statement createStmt = conn.createStatement();
            long dbStartDrop2 = System.nanoTime();
            createStmt.execute(createTempTable);
            long dbEndDrop2 = System.nanoTime();
            totalDbTime += (dbEndDrop2 - dbStartDrop2);
            createStmt.close();

            StringBuilder topMoviesQuery = new StringBuilder(
                    "INSERT INTO temp_movies (movie_id, rating) " +
                            "SELECT m.id, r.rating " +
                            "FROM movies m " +
                            "LEFT JOIN ratings r ON m.id = r.movie_id " +
                            "WHERE 1=1 "
            );

            if (title != null && !title.isEmpty()) topMoviesQuery.append("AND MATCH(m.title) AGAINST(? IN BOOLEAN MODE) ");
            if (year != null && !year.isEmpty()) topMoviesQuery.append("AND m.year = ? ");
            if (director != null && !director.isEmpty()) topMoviesQuery.append("AND m.director LIKE ? ");
            if (genre != null && !genre.isEmpty()) topMoviesQuery.append(
                    "AND m.id IN (SELECT movie_id " +
                            "FROM genres_in_movies gm JOIN genres g ON g.id = gm.genre_id " +
                            "WHERE g.name = ?) "
            );
            if (star != null && !star.isEmpty()) topMoviesQuery.append(
                    "AND m.id IN (SELECT movie_id " +
                            "FROM stars_in_movies sm JOIN stars s ON s.id = sm.star_id " +
                            "WHERE s.name LIKE ?) "
            );
            if (prefix != null && !prefix.isEmpty()) topMoviesQuery.append("AND m.title LIKE ? ");

            String primaryCol   = "title".equals(sortPrimaryField) ? "m.title" : "COALESCE(r.rating, -1)";
            String primaryDir   = "asc".equals(sortPrimaryOrder) ? "ASC" : "DESC";
            String secondaryCol = "title".equals(sortSecondaryField) ? "m.title" : "COALESCE(r.rating, -1)";
            String secondaryDir = "asc".equals(sortSecondaryOrder) ? "ASC" : "DESC";

            topMoviesQuery.append("ORDER BY ")
                    .append(primaryCol).append(" ").append(primaryDir)
                    .append(", ")
                    .append(secondaryCol).append(" ").append(secondaryDir)
                    .append(" ")
                    .append("LIMIT ? OFFSET ?");

            PreparedStatement statement = conn.prepareStatement(topMoviesQuery.toString());
            int index = 1;
            if (title != null && !title.isEmpty()) {
                String booleanQuery = Utils.convertToBooleanMode(title);
                statement.setString(index++, booleanQuery);
            }
            if (year != null && !year.isEmpty()) statement.setString(index++, year);
            if (director != null && !director.isEmpty()) statement.setString(index++, "%" + director + "%");
            if (genre != null && !genre.isEmpty()) statement.setString(index++, genre);
            if (star != null && !star.isEmpty()) statement.setString(index++, "%" + star + "%");
            if (prefix != null && !prefix.isEmpty()) statement.setString(index++,prefix + "%");
            statement.setInt(index++, pageSize);
            statement.setInt(index, offset);

            String moviesQuery =
                    "SELECT m.id, m.title, m.year, m.director, t.rating " +
                    "FROM temp_movies t " +
                    "JOIN movies m ON m.id = t.movie_id " +
                    "ORDER BY t.sort_order";

            String genresQuery =
                    "SELECT gm.movie_id, " +
                            "(SELECT GROUP_CONCAT(g_sub.name SEPARATOR ', ') " +
                            "FROM (SELECT g.name AS name " +
                            "FROM genres g " +
                            "JOIN genres_in_movies gm2 ON g.id = gm2.genre_id " +
                            "WHERE gm2.movie_id = gm.movie_id " +
                            "ORDER BY g.name " +
                            "LIMIT 3) AS g_sub) AS genres " +
                            "FROM genres_in_movies gm " +
                            "JOIN temp_movies t ON t.movie_id = gm.movie_id " +
                            "GROUP BY gm.movie_id";

            String starsQuery =
                    "SELECT t.movie_id, " +
                            "       (SELECT GROUP_CONCAT(CONCAT(stars_sub.name, ', ', stars_sub.id) SEPARATOR ', ') " +
                            "        FROM (SELECT s.name, s.id " +
                            "              FROM stars s " +
                            "              JOIN stars_in_movies sm ON s.id = sm.star_id " +
                            "              JOIN (SELECT star_id, COUNT(*) as movie_count " +
                            "                    FROM stars_in_movies " +
                            "                    GROUP BY star_id) AS star_counts ON s.id = star_counts.star_id " +
                            "              WHERE sm.movie_id = t.movie_id " +
                            "              ORDER BY star_counts.movie_count DESC, s.name ASC " +
                            "              LIMIT 3) AS stars_sub) AS stars " +
                            "FROM temp_movies t";

            long dbStart1 = System.nanoTime();
            statement.executeUpdate();
            long dbEnd1 = System.nanoTime();
            totalDbTime += (dbEnd1 - dbStart1);

            PreparedStatement moviesStmt = conn.prepareStatement(moviesQuery);
            long dbStart2 = System.nanoTime();
            ResultSet rs = moviesStmt.executeQuery();
            long dbEnd2 = System.nanoTime();
            totalDbTime += (dbEnd2 - dbStart2);

            PreparedStatement genresStmt = conn.prepareStatement(genresQuery);
            long dbStart3 = System.nanoTime();
            ResultSet genresRs = genresStmt.executeQuery();
            long dbEnd3 = System.nanoTime();
            totalDbTime += (dbEnd3 - dbStart3);

            PreparedStatement starsStmt = conn.prepareStatement(starsQuery);
            long dbStart4 = System.nanoTime();
            ResultSet starsRs = starsStmt.executeQuery();
            long dbEnd4 = System.nanoTime();
            totalDbTime += (dbEnd4 - dbStart4);

            StringBuilder countQuery = new StringBuilder(
                    "SELECT COUNT(DISTINCT m.id) as total " +
                            "FROM movies m " +
                            "LEFT JOIN ratings r ON m.id = r.movie_id "
            );

            if (genre != null && !genre.isEmpty()) {
                countQuery.append(
                        "LEFT JOIN genres_in_movies gm ON m.id = gm.movie_id " +
                        "LEFT JOIN genres g ON gm.genre_id = g.id "
                );
            }

            if (star != null && !star.isEmpty()) {
                countQuery.append(
                        "LEFT JOIN stars_in_movies sm ON m.id = sm.movie_id " +
                        "LEFT JOIN stars s ON sm.star_id = s.id "
                );
            }
            countQuery.append("WHERE 1=1 ");

            if (title != null) countQuery.append("AND MATCH(m.title) AGAINST(? IN BOOLEAN MODE) ");
            if (year != null) countQuery.append("AND m.year = ? ");
            if (director != null) countQuery.append("AND m.director LIKE ? ");
            if (genre != null && !genre.isEmpty()) countQuery.append("AND g.name = ? ");
            if (star != null && !star.isEmpty()) countQuery.append("AND s.name LIKE ? ");
            if (prefix != null && !prefix.isEmpty()) countQuery.append("AND m.title LIKE ? ");

            PreparedStatement countStmt = conn.prepareStatement(countQuery.toString());
            index = 1;
            if (title != null && !title.isEmpty()) {
                String booleanQuery = Utils.convertToBooleanMode(title);
                countStmt.setString(index++, booleanQuery);
            }
            if (year != null && !year.isEmpty()) countStmt.setString(index++, year);
            if (director != null && !director.isEmpty()) countStmt.setString(index++, "%" + director + "%");
            if (genre != null && !genre.isEmpty()) countStmt.setString(index++, genre);
            if (star != null && !star.isEmpty()) countStmt.setString(index++, "%" + star + "%");
            if (prefix != null && !prefix.isEmpty()) countStmt.setString(index, prefix + "%");

            long dbStart5 = System.nanoTime();
            ResultSet countRs = countStmt.executeQuery();
            long dbEnd5 = System.nanoTime();
            totalDbTime += (dbEnd5 - dbStart5);
            int totalCount = 0;
            if (countRs.next()) {
                totalCount = countRs.getInt("total");
            }
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
            Map<String, JsonObject> moviesMap = new LinkedHashMap<>();
            while (rs.next()) {
                // get a movie from result set
                String movieID = rs.getString("id");  // db column name from query
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String rating = rs.getString("rating");
                if (rating == null) rating = "N/A";

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject movieObject = new JsonObject();
                movieObject.addProperty("movieID", movieID);
                movieObject.addProperty("movieTitle", movieTitle);
                movieObject.addProperty("movieYear", movieYear);
                movieObject.addProperty("movieDirector", movieDirector);
                movieObject.addProperty("movieRating", rating);
                moviesMap.put(movieID, movieObject);
            }

            while (genresRs.next()) {
                String genres = genresRs.getString("genres");
                String movieID = genresRs.getString("movie_id");
                JsonObject movie = moviesMap.get(movieID);
                movie.addProperty("movieGenres", genres != null ? genres : "");
            }

            while (starsRs.next()) {
                String stars = starsRs.getString("stars");
                String movieID = starsRs.getString("movie_id");
                JsonObject movie = moviesMap.get(movieID);
                movie.addProperty("movieStars", stars != null ? stars : "");
            }

            for (JsonObject movie : moviesMap.values()) {
                moviesArray.add(movie);
            }
            countRs.close();
            countStmt.close();
            rs.close();
            statement.close();

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
            long dbTime = totalDbTime;
            Utils.writeTimingToFile(totalTime, dbTime, "MovieListServlet", title);
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}