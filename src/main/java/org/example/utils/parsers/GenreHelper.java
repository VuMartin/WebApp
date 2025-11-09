package main.java.org.example.utils.parsers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GenreHelper {
    private Connection conn;
    private Map<String, Integer> genreMap; // genre_name -> genre_id
    private int maxGenreID;

    public GenreHelper(Connection conn) {
        this.conn = conn;
        maxGenreID = 0;
        loadGenres();
    }

    private void loadGenres() {
        genreMap = new HashMap<>();
        String sql = "SELECT id, name FROM genres";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                genreMap.put(name, id);
                if (id > maxGenreID) maxGenreID = id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String normalizeGenre(String xmlGenre) {
        if (xmlGenre == null) return null;
        switch (xmlGenre) {
            case "act":
            case "axtn":
            case "viol":
            case "actn": return "Action";
            case "adctx":
            case "adct":
            case "advt": return "Adventure";
            case "allegory": return "Allegory";
            case "art video": return "Art Video";
            case "avant garde":
            case "avant":
            case "dada":
            case "cubist":
            case "avga": return "Avant Garde";
            case "biopp":
            case "bio":
            case "biopx":
            case "biob":
            case "biog":
            case "biop": return "Biography";
            case "camp": return "Camp";
            case "cartoon":
            case "cart": return "Animation";
            case "comdx":
            case "cond":
            case "silly":
            case "comd": return "Comedy";
            case "cnrb":
            case "cmr":
            case "cnrbb":
            case "cnr": return "Cops and Robbers";
            case "crim": return "Crime";
            case "ctxxx":
            case "txx":
            case "ctcxx":
            case "ctxx": return "uncategorized";
            case "disa": return "Disaster";
            case "dicu":
            case "ducu":
            case "duco":
            case "natu":
            case "docu": return "Documentary";
            case "dram>":
            case "dramn":
            case "draam":
            case "dramd":
            case "dram": return "Drama";
            case "epic": return "Epic";
            case "expm": return "Experimental";
            case "faml": return "Family";
            case "fanth*":
            case "fant": return "Fantasy";
            case "hist": return "History";
            case "hor":
            case "horr": return "Horror";
            case "muscl":
            case "muusc":
            case "musc":
            case "stage musical": return "Musical";
            case "mystp":
            case "myst": return "Mystery";
            case "noir": return "Black";
            case "nouvelle vague": return "Nouvelle Vague";
            case "kinky":
            case "porb":
            case "porn": return "Pornography";
            case "propaganda": return "Propaganda";
            case "pseudo":
            case "pseudo docu": return "Pseudo Documentary";
            case "romtx":
            case "ront":
            case "romt": return "Romance";
            case "bleak satire":
            case "satire":
            case "sati": return "Satire";
            case "sxfi":
            case "scfi":
            case "scif":
            case "s.f.": return "Sci-Fi";
            case "surr":
            case "surreal":
            case "surl": return "Surreal";
            case "sports":
            case "sport": return "Sport";
            case "psych":
            case "psyc":
            case "susp": return "Thriller";
            case "religious": return "Religious";
            case "road": return "Road";
            case "tv": return "TV show";
            case "tvs": return "TV series";
            case "tvmini":
            case "tvm": return "TV miniseries";
            case "undr":
            case "underground": return "Underground";
            case "west1":
            case "west": return "Western";
            default: return "error";
        }
    }

    private int getOrCreateGenreId(String genreName) {
        String normalized = normalizeGenre(genreName);
        if (normalized.equals("error")) {
            System.out.println("Unknown genre code: " + genreName + " - skipping invalid genre");
            return -1;
        }
        if (genreMap.containsKey(normalized))  return genreMap.get(normalized);
        int newId = getNextGenreId();
        String sql = "INSERT INTO genres (id, name) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newId);
            stmt.setString(2, normalized);
            stmt.executeUpdate();
            genreMap.put(normalized, newId);
            return newId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void processAllGenres(Movie movie, String genreString) {
        if (genreMap.containsKey(genreString))  {
            movie.addGenreID(genreMap.get(genreString));
            return;
        }
        String genreWord = genreString.toLowerCase().trim();
        if (genreWord.equals("romtadvt")) genreWord = "romt advt";
        Set<String> multiWordGenres = Set.of("art video", "s.f.", "stage musical", "bleak satire", "pseudo documentary",
                "nouvelle vague");

        if (multiWordGenres.contains(genreWord)) {
            int genreId = getOrCreateGenreId(genreWord);
            if (genreId != -1) movie.addGenreID(genreId);
            return;
        }

        String[] genreParts = genreWord.split("[ .]+");

        for (String genre : genreParts) {
            if (!genre.isEmpty()) {
                int genreId = getOrCreateGenreId(genre);
                if (genreId != -1) movie.addGenreID(genreId);
            }
        }
    }

    private int getNextGenreId() {
        return ++maxGenreID;
    }
}