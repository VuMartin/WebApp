package main.java.org.example.utils.parsers;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActorParser extends DefaultHandler {
    private final Connection conn;
    private Star tempStar;
    private String tempVal;
    private final Map<String, String> starIDMap;
    private int starIdCounter;
    private final List<Star> starBatch;
    private static final int BATCH_SIZE = 1000;
    private String starDOB;
    private final Map<String, String> starBirthMap;

    public ActorParser(Connection conn, Map<String, String> starIDMap) {
        this.conn = conn;
        this.starIDMap = starIDMap;
        initializeStarIdCounter();
        starBatch = new ArrayList<>();
        starBirthMap = new HashMap<>();
    }

    public void parse() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse("src/main/resources/xml/actors63.xml", this);
    }

    @Override
    public void startDocument() {
        System.out.println("Starting to parse actors...");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = "";
        starDOB = "";
        if (qName.equalsIgnoreCase("actor")) {
            tempStar = new Star();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        tempVal = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        tempVal = tempVal.trim();
        if (qName.equalsIgnoreCase("actor")) {
            insertStarToDatabase(tempStar);
        } else if (qName.equalsIgnoreCase("stagename")) {
            tempStar.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
            starDOB = tempVal;
            String cleaned = tempVal.replaceAll("[+~]|\\[.*?\\]", "");
            if (!cleaned.isEmpty()) {
                try {
                    tempStar.setBirthYear(Integer.parseInt(cleaned));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid dob: '" + tempVal + "' - using null");
                }
            }
        }
    }

    private void insertStarToDatabase(Star star) {
        String starBirth = star.getName() + "|" + starDOB;
        if (starBirthMap.containsKey(starBirth) || starExists(star)) return;

        String key = star.getName();
        String starId = generateStarId();
        star.setId(starId);
        starIDMap.put(key, starId);
        starBirthMap.put(starBirth, starId);

        starBatch.add(star);

        if (starBatch.size() >= BATCH_SIZE) {
            insertStarBatch();
        }
    }

    private void insertStarBatch() {
        try {
            String sql = "INSERT INTO stars (id, name, birth_year) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Star star : starBatch) {
                    stmt.setString(1, star.getId());
                    stmt.setString(2, star.getName());
                    if (star.getBirthYear() != null) {
                        stmt.setInt(3, star.getBirthYear());
                    } else {
                        stmt.setNull(3, java.sql.Types.INTEGER);
                    }
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            System.out.println("Batch insert error for " + starBatch.size() + " stars");
            e.printStackTrace();
        } finally {
            starBatch.clear();
        }
    }

    @Override
    public void endDocument() {
        if (!starBatch.isEmpty()) {
            insertStarBatch();
            System.out.println("Flushed final batch of stars");
        }
    }

    private void initializeStarIdCounter() {
        String sql = "SELECT MAX(id) as max_id FROM stars WHERE id LIKE 'nm%'";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String maxId = rs.getString("max_id");
                if (maxId != null && maxId.startsWith("nm")) {
                    starIdCounter = Integer.parseInt(maxId.substring(2));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String generateStarId() {
        starIdCounter++;
        return String.format("nm%07d", starIdCounter);
    }

    private boolean starExists(Star star) {
        if (star.getName() == null) {
            System.out.println("star missing name - skipping");
            return true;
        }

        String sql = "SELECT COUNT(*) FROM stars WHERE name = ? AND birth_year = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, star.getName());
            if (star.getBirthYear() != null) {
                stmt.setInt(2, star.getBirthYear());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Error inserting star: " + star.getName());
            return false;
        }
    }
}