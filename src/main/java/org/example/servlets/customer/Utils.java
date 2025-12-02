package main.java.org.example.servlets.customer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Utils {

    public static String convertToBooleanMode(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }

        String[] tokens = query.trim().split("\\s+");
        StringBuilder booleanQuery = new StringBuilder();

        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "a", "an", "the", "la", "le", "el", "del", "de", "y", "and", "of", "-", "+"
        ));

        for (String token : tokens) {
            token = token.toLowerCase();
            if (token.isEmpty() || stopWords.contains(token)) {
                continue;
            }

            booleanQuery.append("+").append(token).append("* ");
        }

        return booleanQuery.toString().trim();
    }

    public static void writeTimingToFile(long totalTime, long dbTime, String servletName, String identifier) {
        try {
            // Sanitize identifier for CSV (remove commas, newlines)
            String safeIdentifier = identifier != null ? identifier
                    .replace(",", "").replace("\n", "") : "null";

            String logLine = String.format("%s,%s,%d,%d,%d%n",
                    servletName, safeIdentifier, System.currentTimeMillis(), totalTime, dbTime);

            Files.write(Paths.get("/tmp/MySQLtiming.log"),
                    logLine.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}