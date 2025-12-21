package main.java.org.example.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Utils {

    public static String convertToBooleanMode(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }

        String[] tokens = query.trim().split("\\s+");
        StringBuilder booleanQuery = new StringBuilder();

        for (String token : tokens) {
            token = token.toLowerCase();

            token = java.text.Normalizer.normalize(token, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");

            token = token.replace("-", " ");
            token = token.replaceAll("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$", "");

            if (!token.isEmpty()) {
                for (String t : token.split("\\s+")) {
                    booleanQuery.append("+").append(t).append("* ");
                }
            }
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

            Files.write(Paths.get("/tmp/MySQLFullSearch100.log"),
                    logLine.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}