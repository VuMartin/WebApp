package main.java.org.example.utils.parsers;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CastParser extends DefaultHandler {
    private final Connection conn;
    private final Map<String, String> movieIdMap; // fid -> movie_id
    private final Map<String, String> starIdMap;  // stagename -> star_id
    private String currentMovie;
    private String currentActor;
    private String tempVal;
    private final List<String> castStarBatch;
    private final List<String> castMovieBatch;
    private static final int BATCH_SIZE = 1000;
    private final Set<String> notFoundCache;

    public CastParser(Connection conn, Map<String, String> movieIdMap, Map<String, String> starIdMap) {
        this.conn = conn;
        this.movieIdMap = movieIdMap;
        this.starIdMap = starIdMap;
        castStarBatch = new ArrayList<>();
        castMovieBatch = new ArrayList<>();
        notFoundCache  = new HashSet<>();
    }

    public void parse() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse("src/main/resources/xml/casts124.xml", this);
    }

    @Override
    public void startDocument() {
        System.out.println("Starting to parse casts...");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = "";
        if (qName.equalsIgnoreCase("m")) {
            currentMovie = "";
            currentActor = "";
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        tempVal = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        tempVal = tempVal.trim();
        if (qName.equalsIgnoreCase("f")) {
            currentMovie = tempVal.toLowerCase();
        } else if (qName.equalsIgnoreCase("a")) {
            currentActor = tempVal;
        } else if (qName.equalsIgnoreCase("m")) {
            processCastRelationship();
        }
    }

    private void processCastRelationship() {
        if (notFoundCache.contains(currentActor) || notFoundCache.contains(currentMovie)) return;
        if (currentMovie.isEmpty()) {
            System.out.println("Movie is empty");
            notFoundCache.add(currentMovie);
            return;
        }

        if (currentActor.isEmpty()) {
            System.out.println("Star is empty");
            notFoundCache.add(currentActor);
            return;
        }

        String movieId = movieIdMap.get(currentMovie);
        String starId = starIdMap.get(currentActor);

        if (starId == null) {
            starId = findStarInDB(currentActor);
            if (starId == null) {
                System.out.println("Star not found: " + currentActor);
                notFoundCache.add(currentActor);
            }
        }
        if (movieId == null) {
            System.out.println("Movie not found: " + currentMovie);
            notFoundCache.add(currentMovie);
        }
        if (movieId == null || starId == null) {
            return;
        }
        castStarBatch.add(starId);
        castMovieBatch.add(movieId);
        if (castStarBatch.size() >= BATCH_SIZE) {
            insertCastBatch();
        }
    }

    private void insertCastBatch() {
        try {
            String sql = "INSERT IGNORE INTO stars_in_movies (star_id, movie_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < castStarBatch.size(); i++) {
                    stmt.setString(1, castStarBatch.get(i));
                    stmt.setString(2, castMovieBatch.get(i));
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            System.out.println("Batch insert error for " + castStarBatch.size() + " cast relationships");
            e.printStackTrace();
        } finally {
            castStarBatch.clear();
            castMovieBatch.clear();
        }
    }

    private String findStarInDB(String actorName) {
        String sql = "SELECT id FROM stars WHERE name = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, actorName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error finding actor=" + actorName);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void endDocument() {
        if (!castStarBatch.isEmpty()) {
            insertCastBatch();
            System.out.println("Flushed final cast batch");
        }
    }
}
