package main.java.org.example.servlets.employee;


import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import javax.naming.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name="AddStarServlet", urlPatterns="/api/add_star")
public class AddStarServlet extends HttpServlet {
    private DataSource ds;
    @Override public void init() {
        try {
            InitialContext ic = new InitialContext();
            Context env = (Context) ic.lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/moviedb"); // adjust if your JNDI name differs
        } catch (NamingException e) { throw new RuntimeException(e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject json = new JsonObject();

        String name = req.getParameter("name");
        String birth = req.getParameter("birthYear"); // optional

        if (name == null || name.trim().isEmpty()) {
            json.addProperty("status","error");
            json.addProperty("message","Star name is required.");
            out.write(json.toString()); return;
        }

        Integer birthYear = null;
        if (birth != null && !birth.trim().isEmpty()) {
            try { birthYear = Integer.valueOf(birth.trim()); }
            catch (NumberFormatException e) {
                json.addProperty("status","error");
                json.addProperty("message","Birth year must be an integer.");
                out.write(json.toString()); return;
            }
        }

        try (Connection conn = ds.getConnection()) {
            // Transactionally generate next id: 'nm' + 7 digits
            boolean old = conn.getAutoCommit(); conn.setAutoCommit(false);
            String nextNum;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT LPAD(COALESCE(MAX(CAST(SUBSTRING(id,3) AS UNSIGNED)) + 1, 1), 7, '0') AS next_num " +
                            "FROM stars WHERE id LIKE 'nm%' FOR UPDATE");
                 ResultSet rs = ps.executeQuery()) {
                rs.next(); nextNum = rs.getString("next_num"); // e.g. "0000124"
            }
            String newId = "nm" + nextNum;

            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO stars (id, name, birth_year) VALUES (?, ?, ?)")) {
                ins.setString(1, newId);
                ins.setString(2, name.trim());
                if (birthYear == null) ins.setNull(3, Types.INTEGER); else ins.setInt(3, birthYear);
                ins.executeUpdate();
            }
            conn.commit(); conn.setAutoCommit(old);

            json.addProperty("status","success");
            json.addProperty("message","Star added successfully.");
            json.addProperty("id", newId);
            out.write(json.toString());
        } catch (SQLException e) {
            JsonObject err = new JsonObject();
            err.addProperty("status","error");
            err.addProperty("message","DB error: " + e.getMessage());
            out.write(err.toString());
        }
    }
}
