package main.java.org.example.servlets.employee;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class InsertEmployee {
    public static void main(String[] args) throws Exception {
        // encrypt password
        StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
        String encryptedPassword = encryptor.encryptPassword("classta");

        // connect to DB
        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd)) {
            String query = "INSERT INTO employees (email, password, fullname) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, "classta@email.edu");
                statement.setString(2, encryptedPassword);
                statement.setString(3, "TA CS122B");
                statement.executeUpdate();
                System.out.println("Employee inserted successfully.");
            }
        }
    }
}