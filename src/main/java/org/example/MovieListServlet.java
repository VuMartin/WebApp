package main.java.org.example;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

// http://localhost:8080/2025_fall_cs_122b_marjoe_war/movies
// This annotation maps this Java Servlet Class to a URL
@WebServlet("/movies")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Change this to your own mysql username and password
        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        // Set response mime type
        response.setContentType("text/html");

        // Get the PrintWriter for writing response
        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head><title>Fabflix</title></head>");

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            // create database connection
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            // declare statement
            Statement statement = connection.createStatement();
            // prepare query
            String topMoviesQuery = "SELECT m.id, m.title, m.year, m.director + r.rating " +
                                    "FROM movies m " +
                                    "JOIN ratings r ON m.id = r.movie_id " +
                                    "ORDER BY r.rating DESC " +
                                    "LIMIT 20";
//
//            String firstThreeGenres = "SELECT g.name " +
//                                      "FROM genres g " +
//                                      "JOIN genres_in_movies gm ON g.id = gm.genre_id " +
//                                      "WHERE gm.movie_id = ? " +
//                                      "LIMIT 3";
//
//            String firstThreeStars = "SELECT s.name " +
//                                     "FROM stars s " +
//                                     "JOIN stars_in_movies sm ON s.id = sm.star_id " +
//                                     "WHERE sm.movie_id = ? " +
//                                     "LIMIT 3";
            // execute query
            ResultSet resultSet = statement.executeQuery(topMoviesQuery);
//            ResultSet resultSet2 = statement.executeQuery(firstThreeGenres);
//            ResultSet resultSet3 = statement.executeQuery(firstThreeStars);

            out.println("<body>");
            out.println("<h1>MovieDB Top 20 Movies</h1>");

            out.println("<table border>");

            // Add table header row
            out.println("<tr>");
            out.println("<td>Title</td>");
            out.println("<td>Year</td>");
            out.println("<td>Director</td>");
            out.println("<td>Genres</td>");
            out.println("<td>Stars</td>");
            out.println("<td>Rating</td>");
            out.println("</tr>");

            // Add a row for every movie result
            while (resultSet.next()) {
                // get a movie from result set
                String movieID = resultSet.getString("id");
                String movieTitle = resultSet.getString("title");
                String movieYear = resultSet.getString("year");
                String movieDirector = resultSet.getString("director");

                out.println("<tr>");
                out.println("<td>" + movieTitle + "</td>");
                out.println("<td>" + movieYear + "</td>");
                out.println("<td>" + movieDirector + "</td>");
                out.println("</tr>");
            }

            out.println("</table>");
            out.println("</body>");

            resultSet.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            /*
             * After you deploy the WAR file through tomcat manager webpage,
             *   there's no console to see the print messages.
             * Tomcat append all the print messages to the file: tomcat_directory/logs/catalina.out
             *
             * To view the last n lines (for example, 100 lines) of messages you can use:
             *   tail -100 catalina.out
             * This can help you debug your program after deploying it on AWS.
             */
            request.getServletContext().log("Error: ", e);

            out.println("<body>");
            out.println("<p>");
            out.println("Exception in doGet: " + e.getMessage());
            out.println("</p>");
            out.print("</body>");
        }

        out.println("</html>");
        out.close();

    }


}
