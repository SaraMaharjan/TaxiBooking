package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LogIn {
    DbConnection db = new DbConnection();

    /**
     * Checks database for user credentials and returns their role.
     * Also initializes the UserSession for the logged-in user.
     */
    public String loginAndGetRole(String email, String password) {
        // Updated SQL to get Name and Role
        String sql = "SELECT full_name, role FROM users WHERE email = ? AND password = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery(); 

            if (rs.next()) {
                String fullName = rs.getString("full_name");
                String role = rs.getString("role");

                // Initialize the session so the sidebar shows the correct name
                UserSession.createInstance(fullName, email, role);
                
                return role; // Success: returns "Passenger", "Driver", etc.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null; // Failure: user not found or password wrong
    }

    // You can keep this for backward compatibility, or delete it if 
    // your LogInController now only uses loginAndGetRole.
    public boolean login(String email, String password) {
        return loginAndGetRole(email, password) != null;
    }
}