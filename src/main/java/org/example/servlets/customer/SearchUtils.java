package main.java.org.example.servlets.customer;

public class SearchUtils {

    public static String convertToBooleanMode(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }

        String[] tokens = query.trim().split("\\s+");
        StringBuilder booleanQuery = new StringBuilder();

        for (String token : tokens) {
            if (!token.isEmpty()) {
                booleanQuery.append("+").append(token).append("* ");
            }
        }

        return booleanQuery.toString().trim();
    }
}