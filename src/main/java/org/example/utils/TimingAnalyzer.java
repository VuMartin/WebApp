package main.java.org.example.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimingAnalyzer {
    public static void main(String[] args) throws IOException {
        String[] files = {
                "/tmp/MySQLSearch100.log",
                "/tmp/MySQLSearch200.log",
                "/tmp/MySQLSingle100.log",
                "/tmp/MySQLSingle200.log",
                "/tmp/MongoSearch100.log",
                "/tmp/MongoSearch200.log",
                "/tmp/MongoSingle100.log",
                "/tmp/MongoSingle200.log",
        };

        System.out.println("Scenario|Avg Ts|Avg Tj|Med Ts|Med Tj|Std Ts|Std Tj");
        System.out.println("--------|------|------|------|------|------|------");

        for (String filename : files) {
            analyzeFile(filename);
        }
    }

    private static void analyzeFile(String filename) throws IOException {
        List<Double> tsValues = new ArrayList<>();
        List<Double> tjValues = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 5) {
                double ts = Long.parseLong(parts[3]) / 1_000_000.0; // ns to ms
                double tj = Long.parseLong(parts[4]) / 1_000_000.0;
                tsValues.add(ts);
                tjValues.add(tj);
            }
        }
        br.close();

        // Calculate stats
        double avgTs = calculateAverage(tsValues);
        double avgTj = calculateAverage(tjValues);
        double medTs = calculateMedian(tsValues);
        double medTj = calculateMedian(tjValues);
        double stdTs = calculateStdDev(tsValues, avgTs);
        double stdTj = calculateStdDev(tjValues, avgTj);

        // Print in table format
        String scenario = filename.replace("_timing.log", "");
        System.out.printf("%s|%.2f|%.2f|%.2f|%.2f|%.2f|%.2f%n",
                scenario, avgTs, avgTj, medTs, medTj, stdTs, stdTj);
    }

    private static double calculateAverage(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private static double calculateMedian(List<Double> values) {
        Collections.sort(values);
        int mid = values.size() / 2;
        if (values.size() % 2 == 1) {
            return values.get(mid);
        } else {
            return (values.get(mid - 1) + values.get(mid)) / 2.0;
        }
    }

    private static double calculateStdDev(List<Double> values, double mean) {
        double sum = 0;
        for (double value : values) {
            sum += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sum / values.size());
    }
}