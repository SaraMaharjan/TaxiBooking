package model;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
 
/**
 * Registration — Model layer for new user sign-up.
 * Saves full_name, email, password, and role to the users table.
 */
public class Registration {
 
    DbConnection db = new DbConnection();
 
    /**
     * Registers a new user with all required fields.
     * Returns true if the insert succeeded, false otherwise.
     */
    public boolean registerUser(String fullName, String email,
                                String password, String role) {
        String sql = "INSERT INTO users (full_name, email, password, role) " +
                     "VALUES (?, ?, ?, ?)";
 
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, role);
 
            int rows = stmt.executeUpdate();
            return rows > 0;
 
        } catch (SQLException e) {
            System.out.println("Registration Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}