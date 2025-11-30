package main.java.org.example.servlets.customer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Utils {

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