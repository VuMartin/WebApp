package main.java.org.example.servlets;

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

// http://localhost:8080/2025_fall_cs_122b_marjoe_war/api/topmovies
// http://localhost:8080/2025_fall_cs_122b_marjoe_war/movies.html

// This is a code freeze branch for project 2
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
    private SessionAttribute<String> sortFieldAttr;
    private SessionAttribute<String> sortOrderAttr;
    private SessionAttribute<String> sortSecondaryAttr;
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
        sortFieldAttr = new SessionAttribute<>(String.class, "sortField");
        prefixAttr = new SessionAttribute<>(String.class, "prefix");
        sortOrderAttr = new SessionAttribute<>(String.class, "sortOrder");
        sortSecondaryAttr = new SessionAttribute<>(String.class, "sortSecondary");
        sortSecondaryOrderAttr = new SessionAttribute<>(String.class, "sortSecondaryOrder");
        pageSizeAttr = new SessionAttribute<>(Integer.class, "pageSize");
        offsetAttr = new SessionAttribute<>(Integer.class, "offset");
        currPageAttr = new SessionAttribute<>(Integer.class, "page");
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

        response.setContentType("application/json"); // Response mime type
        HttpSession session = request.getSession();
        String title;
        String year;
        String director;
        String genre;
        String star;
        String prefix;
        int pageSize;
        int offset;
        String sortField;
        String sortOrder;
        String sortSecondary;
        String sortSecondaryOrder;
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
            sortField = sortFieldAttr.get(session);
            sortOrder = sortOrderAttr.get(session);
            sortSecondary = sortSecondaryAttr.get(session);
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
            sortField = request.getParameter("sortField");
            sortOrder = request.getParameter("sortOrder");
            sortSecondary = request.getParameter("sortSecondary");
            sortSecondaryOrder = request.getParameter("sortSecondaryOrder");

            //  defaults if any sorts are missing
            if (sortField == null || sortField.isEmpty()) sortField = "rating";
            if (sortOrder == null || sortOrder.isEmpty()) sortOrder = "desc";
            if (sortSecondary == null || sortSecondary.isEmpty()) sortSecondary = "title";
            if (sortSecondaryOrder == null || sortSecondaryOrder.isEmpty()) sortSecondaryOrder = "asc";

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
            sortFieldAttr.set(session, sortField);
            sortOrderAttr.set(session, sortOrder);
            sortSecondaryAttr.set(session, sortSecondary);
            sortSecondaryOrderAttr.set(session, sortSecondaryOrder);
            prefixAttr.set(session, prefix);
        }

        // ---- defaults (apply to both normal and restore=true paths) ----
        if (sortField == null || sortField.isEmpty()) sortField = "rating";
        if (sortOrder == null || sortOrder.isEmpty()) sortOrder = "desc";
        if (sortSecondary == null || sortSecondary.isEmpty()) sortSecondary = "title";
        if (sortSecondaryOrder == null || sortSecondaryOrder.isEmpty()) sortSecondaryOrder = "asc";


        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            StringBuilder topMoviesQuery = new StringBuilder(
                    "SELECT m.id, m.title, m.year, m.director, " +
                            "(SELECT GROUP_CONCAT(genre_sub.name SEPARATOR ', ') " +  // takes multiple rows of a column into one
                            " FROM (SELECT g.name " +  // nested select to get limit 3 since it does not work directly with group concat
                            "       FROM genres g " +
                            "       JOIN genres_in_movies gm ON g.id = gm.genre_id " +  // gets genres for specific movie
                            "       WHERE gm.movie_id = m.id " +  // only genres for current movie
                            "       ORDER BY g.name " +
                            "       LIMIT 3) AS genre_sub) AS genres, " +  // genres is the column name, genre_sub is name of temp table
                            "(SELECT GROUP_CONCAT(CONCAT(stars_sub.name, ', ', stars_sub.id) SEPARATOR ', ') " +
                            " FROM (SELECT s.name, s.id " +
                            "       FROM stars s " +
                            "       JOIN stars_in_movies sm ON s.id = sm.star_id " +
                            "       JOIN (SELECT star_id, COUNT(*) as movie_count " +
                            "             FROM stars_in_movies " +
                            "             GROUP BY star_id) AS star_counts ON s.id = star_counts.star_id " +
                            "       WHERE sm.movie_id = m.id " +
                            "       ORDER BY star_counts.movie_count DESC, s.name ASC " +
                            "       LIMIT 3) AS stars_sub) AS stars, " +  // stars is the column name
                            "r.rating " +
                            "FROM movies m " +
                            "JOIN ratings r ON m.id = r.movie_id " +
                            "WHERE 1=1 "
            );

            if (title != null && !title.isEmpty()) topMoviesQuery.append("AND m.title LIKE ? ");
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
            // ---- build a safe ORDER BY from whitelisted values ----
            String primaryCol   = "title".equalsIgnoreCase(sortField) ? "m.title" : "r.rating";
            String primaryDir   = "asc".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
            String secondaryCol = "title".equalsIgnoreCase(sortSecondary) ? "m.title" : "r.rating";
            String secondaryDir = "asc".equalsIgnoreCase(sortSecondaryOrder) ? "ASC" : "DESC";

            topMoviesQuery.append("ORDER BY ")
                    .append(primaryCol).append(" ").append(primaryDir)
                    .append(", ")
                    .append(secondaryCol).append(" ").append(secondaryDir)
                    .append(" ");            topMoviesQuery.append(" LIMIT ? OFFSET ?");

            PreparedStatement statement = conn.prepareStatement(topMoviesQuery.toString());
            int index = 1;
            if (title != null && !title.isEmpty()) statement.setString(index++, "%" + title + "%");
            if (year != null && !year.isEmpty()) statement.setString(index++, year);
            if (director != null && !director.isEmpty()) statement.setString(index++, "%" + director + "%");
            if (genre != null && !genre.isEmpty()) statement.setString(index++, genre);
            if (star != null && !star.isEmpty()) statement.setString(index++, "%" + star + "%");
            if (prefix != null && !prefix.isEmpty()) statement.setString(index++,prefix + "%");
            statement.setInt(index++, pageSize);
            statement.setInt(index, offset);

            ResultSet rs = statement.executeQuery();

            String countQuery =
                    "SELECT COUNT(DISTINCT m.id) AS total " +
                            "FROM movies m " +
                            "LEFT JOIN ratings r ON m.id = r.movie_id " +
                            "LEFT JOIN genres_in_movies gm ON m.id = gm.movie_id " +
                            "LEFT JOIN genres g ON gm.genre_id = g.id " +
                            "LEFT JOIN stars_in_movies sm ON m.id = sm.movie_id " +
                            "LEFT JOIN stars s ON sm.star_id = s.id " +
                            "WHERE 1=1 " +
                            (title != null && !title.isEmpty() ? "AND m.title LIKE ? " : "") +
                            (year != null && !year.isEmpty() ? "AND m.year = ? " : "") +
                            (director != null && !director.isEmpty() ? "AND m.director LIKE ? " : "") +
                            (genre != null && !genre.isEmpty() ? "AND g.name = ? " : "") +
                            (star != null && !star.isEmpty() ? "AND s.name LIKE ? " : "") +
                            (prefix != null && !prefix.isEmpty() ? "AND m.title LIKE ? " : "");

            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            index = 1;
            if (title != null && !title.isEmpty()) countStmt.setString(index++, "%" + title + "%");
            if (year != null && !year.isEmpty()) countStmt.setString(index++, year);
            if (director != null && !director.isEmpty()) countStmt.setString(index++, "%" + director + "%");
            if (genre != null && !genre.isEmpty()) countStmt.setString(index++, genre);
            if (star != null && !star.isEmpty()) countStmt.setString(index++, "%" + star + "%");
            if (prefix != null && !prefix.isEmpty()) countStmt.setString(index, prefix + "%");

            ResultSet countRs = countStmt.executeQuery();
            int totalCount = 0;
            if (countRs.next()) {
                totalCount = countRs.getInt("total");
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("totalCount", totalCount);
            jsonObject.addProperty("currentPage", currentPage);
            jsonObject.addProperty("pageSize", pageSize);
            jsonObject.addProperty("sortField", sortField);
            jsonObject.addProperty("sortOrder", sortOrder);
            jsonObject.addProperty("sortSecondary", sortSecondary);
            jsonObject.addProperty("sortSecondaryOrder", sortSecondaryOrder);
            jsonObject.addProperty("prefix", prefix);
            JsonArray moviesArray = new JsonArray();
            jsonObject.add("movies", moviesArray);
            // Iterate through each row of rs
            while (rs.next()) {
                // get a movie from result set
                String movieID = rs.getString("id");  // db column name from query
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieGenres = rs.getString("genres");
                String movieStars = rs.getString("stars");
                String rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject movieObject = new JsonObject();
                movieObject.addProperty("movieID", movieID);
                movieObject.addProperty("movieTitle", movieTitle);
                movieObject.addProperty("movieYear", movieYear);
                movieObject.addProperty("movieDirector", movieDirector);
                movieObject.addProperty("movieGenres", movieGenres);
                movieObject.addProperty("movieStars", movieStars);
                movieObject.addProperty("movieRating", rating);
                moviesArray.add(movieObject);
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
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}