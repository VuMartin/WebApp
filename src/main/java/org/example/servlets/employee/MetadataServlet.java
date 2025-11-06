package main.java.org.example.servlets.employee;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(name = "MetadataServlet", urlPatterns = "/api/employee/metadata")
public class MetadataServlet extends HttpServlet {

    private DataSource dataSource;

    @Override
    public void init() {
        try {
            InitialContext ic = new InitialContext();
            Context env = (Context) ic.lookup("java:comp/env");
            dataSource = (DataSource) env.lookup("jdbc/moviedb");
        } catch (NamingException e) {
            throw new RuntimeException("JNDI DataSource lookup failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject root = new JsonObject();

        // Implement employee session if needed here, right now allows to see
        // metadata without logging in as employee first

        try (Connection conn = dataSource.getConnection()) {
            String schema = conn.getCatalog();

            String sql =
                    "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE " +
                            "FROM INFORMATION_SCHEMA.COLUMNS " +
                            "WHERE TABLE_SCHEMA = ? " +
                            "ORDER BY TABLE_NAME, ORDINAL_POSITION";

            Map<String, JsonArray> tableToCols = new LinkedHashMap<>();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, schema);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String table = rs.getString("TABLE_NAME");
                        String colName = rs.getString("COLUMN_NAME");
                        String colType = rs.getString("COLUMN_TYPE");

                        JsonObject col = new JsonObject();
                        col.addProperty("name", colName);
                        col.addProperty("type", colType);

                        tableToCols.computeIfAbsent(table, k -> new JsonArray()).add(col);
                    }
                }
            }

            JsonArray tables = new JsonArray();
            for (Map.Entry<String, JsonArray> e : tableToCols.entrySet()) {
                JsonObject t = new JsonObject();
                t.addProperty("table", e.getKey());
                t.add("columns", e.getValue());
                tables.add(t);
            }

            root.addProperty("status", "success");
            root.addProperty("database", schema);
            root.add("tables", tables);
            response.setStatus(200);
            out.write(root.toString());

        } catch (SQLException e) {
            response.setStatus(200);
            root.addProperty("status", "error");
            root.addProperty("message", "DB error: " + e.getMessage());
            out.write(root.toString());
        }
    }
}
