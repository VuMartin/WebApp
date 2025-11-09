package main.java.org.example.utils.parsers;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class SaxParserMain {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password")) {
            long start = System.currentTimeMillis();
            GenreHelper genreHelper = new GenreHelper(conn);
            Map<String, String> movieIdMap = new HashMap<>();
            Map<String, String> actorIdMap = new HashMap<>();
            MovieParser movieParser = new MovieParser(conn, genreHelper, movieIdMap);
            ActorParser actorParser = new ActorParser(conn, actorIdMap);
            CastParser castParser = new CastParser(conn, movieIdMap, actorIdMap);

            System.out.println("Starting XML parsing...");
            movieParser.parse();
            System.out.println("Finished parsing movies");

            actorParser.parse();
            System.out.println("Finished parsing actors");

            castParser.parse();
            long end = System.currentTimeMillis();
            System.out.println("Finished parsing casts");

            System.out.println("All XML files parsed successfully!");
            System.out.println("Parsing time: " + (end - start) + " ms");

        } catch (Exception e) {
            System.err.println("Error during parsing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}