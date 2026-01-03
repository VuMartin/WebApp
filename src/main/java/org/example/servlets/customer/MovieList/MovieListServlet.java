package main.java.org.example.servlets.customer.MovieList;

import com.google.gson.JsonObject;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;


// http://localhost:8080/api/topmovies
// http://localhost:8080/html/customer/movies.html
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

    private MovieListRetriever movieListRetriever;
    public void init(ServletConfig config) {
        this.movieListRetriever = new MySQLMovieListRetriever();
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
        try {
            JsonObject movieList = movieListRetriever.getMovieList(
                    title,
                    year,
                    director,
                    genre,
                    star,
                    prefix,
                    sortPrimaryField,
                    sortPrimaryOrder,
                    sortSecondaryField,
                    sortSecondaryOrder,
                    currentPage,
                    pageSize,
                    offset
            );
            JsonObject result = movieListRetriever.getPagination(
                    title,
                    year,
                    director,
                    genre,
                    star,
                    prefix,
                    movieList
            );
            out.write(result.toString());
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("errorMessage", e.getMessage());
            out.write(error.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        }
    }
}