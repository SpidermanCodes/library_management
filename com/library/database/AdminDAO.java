package com.library.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDAO {
       public boolean validateAdmin(String username, String password) {
        String query = "SELECT * FROM admin WHERE username = ? AND password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            return rs.next(); // Return true if a matching record is found
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}