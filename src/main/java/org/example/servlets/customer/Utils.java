package main.java.org.example.servlets.customer;

import org.bson.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static Document convertToMongoText(String query) {
        if (query == null || query.isEmpty()) return null;

        String cleanedQuery = query.replace("-", " ");

        String[] tokens = cleanedQuery.trim().split("\\s+");
        List<Document> mustList = new ArrayList<>();

        for (String token : tokens) {
            if (!token.isEmpty()) {
                token = token.toLowerCase();
                token = Normalizer.normalize(token, Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "");

                token = token.replaceAll("[^a-zA-Z0-9.]", "");
                if (!token.isEmpty()) {
                    mustList.add(new Document("autocomplete",
                            new Document("query", token)
                                    .append("path", "title")));
                }
            }
        }

        // Build the $search stage
        Document searchStage = new Document("$search",
                new Document("index", "title")
                        .append("compound", new Document("must", mustList))
        );

        return searchStage;
    }

    public static void writeTimingToFile(long totalTime, long dbTime, String servletName, String identifier) {
        try {
            // Sanitize identifier for CSV (remove commas, newlines)
            String safeIdentifier = identifier != null ? identifier
                    .replace(",", "").replace("\n", "") : "null";

            String logLine = String.format("%s,%s,%d,%d,%d%n",
                    servletName, safeIdentifier, System.currentTimeMillis(), totalTime, dbTime);

            Files.write(Paths.get("/tmp/timing.log"),
                    logLine.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}