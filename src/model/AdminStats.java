package model;
 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
 
/**
 * AdminStats — Model layer for the Admin role.
 *
 * Provides:
 *   - Dashboard summary stats (total users, rides, revenue, active rides)
 *   - Full user list for Manage Users page
 *   - Full ride list for All Rides page
 *   - Report data (revenue by vehicle type, rides per day)
 *
 * Required DB tables: users, rides (see database_setup.sql)
 */
public class AdminStats {
 
    private final DbConnection db = new DbConnection();
 
    // ── Dashboard summary ────────────────────────────────────────────
    private int    totalPassengers = 0;
    private int    totalDrivers    = 0;
    private int    totalRides      = 0;
    private double totalRevenue    = 0;
    private int    activeRides     = 0;
 
    // ── User list ────────────────────────────────────────────────────
    private final List<UserRow>  users = new ArrayList<>();
 
    // ── Ride list ────────────────────────────────────────────────────
    private final List<RideRow>  rides = new ArrayList<>();
 
    // ── Report data ──────────────────────────────────────────────────
    private final List<ReportRow> reportRows = new ArrayList<>();
 
    // ─────────────────────────────────────────────────────────────────
    //  Load all data in one call
    // ─────────────────────────────────────────────────────────────────
    public void load() {
        loadSummary();
        loadUsers();
        loadRides();
        loadReport();
    }
 
    // ── Summary stats ────────────────────────────────────────────────
    private void loadSummary() {
        try (Connection conn = db.getConnection()) {
 
            // Passenger count
            try (PreparedStatement s = conn.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE role = 'Passenger'")) {
                ResultSet rs = s.executeQuery();
                if (rs.next()) totalPassengers = rs.getInt(1);
            }
 
            // Driver count
            try (PreparedStatement s = conn.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE role = 'Driver'")) {
                ResultSet rs = s.executeQuery();
                if (rs.next()) totalDrivers = rs.getInt(1);
            }
 
            // Total completed rides + revenue
            try (PreparedStatement s = conn.prepareStatement(
                    "SELECT COUNT(*), COALESCE(SUM(fare),0) FROM rides WHERE status='completed'")) {
                ResultSet rs = s.executeQuery();
                if (rs.next()) {
                    totalRides   = rs.getInt(1);
                    totalRevenue = rs.getDouble(2);
                }
            }
 
            // Active rides right now
            try (PreparedStatement s = conn.prepareStatement(
                    "SELECT COUNT(*) FROM rides WHERE status='active'")) {
                ResultSet rs = s.executeQuery();
                if (rs.next()) activeRides = rs.getInt(1);
            }
 
        } catch (Exception e) {
            System.out.println("AdminStats.loadSummary error: " + e.getMessage());
        }
    }
 
    // ── All users ────────────────────────────────────────────────────
    private void loadUsers() {
        String sql = "SELECT id, full_name, email, role, phone, address " +
                     "FROM users ORDER BY role, full_name";
        try (Connection conn = db.getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            ResultSet rs = s.executeQuery();
            users.clear();
            while (rs.next()) {
                users.add(new UserRow(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getString("phone"),
                    rs.getString("address")
                ));
            }
        } catch (Exception e) {
            System.out.println("AdminStats.loadUsers error: " + e.getMessage());
        }
    }
 
    // ── All rides ────────────────────────────────────────────────────
    private void loadRides() {
        String sql = """
            SELECT id, passenger_name, driver_email,
                   pickup, dropoff, vehicle_type,
                   distance_km, fare, status,
                   DATE_FORMAT(ride_time, '%d %b %Y  %H:%i') AS formatted_time
            FROM rides
            ORDER BY ride_time DESC
            LIMIT 100
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            ResultSet rs = s.executeQuery();
            rides.clear();
            while (rs.next()) {
                rides.add(new RideRow(
                    rs.getInt("id"),
                    rs.getString("passenger_name"),
                    rs.getString("driver_email"),
                    rs.getString("pickup"),
                    rs.getString("dropoff"),
                    rs.getString("vehicle_type"),
                    rs.getDouble("distance_km"),
                    rs.getDouble("fare"),
                    rs.getString("status"),
                    rs.getString("formatted_time")
                ));
            }
        } catch (Exception e) {
            System.out.println("AdminStats.loadRides error: " + e.getMessage());
        }
    }
 
    // ── Revenue by vehicle type (for Reports page) ───────────────────
    private void loadReport() {
        String sql = """
            SELECT vehicle_type,
                   COUNT(*)          AS total_rides,
                   COALESCE(SUM(fare),0) AS total_revenue,
                   ROUND(AVG(fare),1)    AS avg_fare
            FROM rides
            WHERE status = 'completed'
            GROUP BY vehicle_type
            ORDER BY total_revenue DESC
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            ResultSet rs = s.executeQuery();
            reportRows.clear();
            while (rs.next()) {
                reportRows.add(new ReportRow(
                    rs.getString("vehicle_type"),
                    rs.getInt("total_rides"),
                    rs.getDouble("total_revenue"),
                    rs.getDouble("avg_fare")
                ));
            }
        } catch (Exception e) {
            System.out.println("AdminStats.loadReport error: " + e.getMessage());
        }
    }
 
    // ── Delete a user by ID ──────────────────────────────────────────
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setInt(1, userId);
            return s.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("AdminStats.deleteUser error: " + e.getMessage());
            return false;
        }
    }
 
    // ── Getters ──────────────────────────────────────────────────────
    public int    getTotalPassengers() { return totalPassengers; }
    public int    getTotalDrivers()    { return totalDrivers;    }
    public int    getTotalRides()      { return totalRides;      }
    public double getTotalRevenue()    { return totalRevenue;    }
    public int    getActiveRides()     { return activeRides;     }
    public List<UserRow>   getUsers()  { return users;           }
    public List<RideRow>   getRides()  { return rides;           }
    public List<ReportRow> getReport() { return reportRows;      }
 
    public static String formatRs(double amount) {
        return (amount == Math.floor(amount))
            ? "Rs. " + (int) amount
            : String.format("Rs. %.1f", amount);
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  Inner data classes
    // ─────────────────────────────────────────────────────────────────
 
    /** Represents a single row in the users table. */
    public static class UserRow {
        public final int    id;
        public final String fullName;
        public final String email;
        public final String role;
        public final String phone;
        public final String address;
 
        public UserRow(int id, String fullName, String email,
                       String role, String phone, String address) {
            this.id       = id;
            this.fullName = fullName;
            this.email    = email;
            this.role     = role;
            this.phone    = phone;
            this.address  = address;
        }
    }
 
    /** Represents a single row in the rides table. */
    public static class RideRow {
        public final int    id;
        public final String passengerName;
        public final String driverEmail;
        public final String pickup;
        public final String dropoff;
        public final String vehicleType;
        public final double distanceKm;
        public final double fare;
        public final String status;
        public final String rideTime;
 
        public RideRow(int id, String passengerName, String driverEmail,
                       String pickup, String dropoff, String vehicleType,
                       double distanceKm, double fare,
                       String status, String rideTime) {
            this.id            = id;
            this.passengerName = passengerName;
            this.driverEmail   = driverEmail;
            this.pickup        = pickup;
            this.dropoff       = dropoff;
            this.vehicleType   = vehicleType;
            this.distanceKm    = distanceKm;
            this.fare          = fare;
            this.status        = status;
            this.rideTime      = rideTime;
        }
    }
 
    /** Represents a report row — revenue per vehicle type. */
    public static class ReportRow {
        public final String vehicleType;
        public final int    totalRides;
        public final double totalRevenue;
        public final double avgFare;
 
        public ReportRow(String vehicleType, int totalRides,
                         double totalRevenue, double avgFare) {
            this.vehicleType  = vehicleType;
            this.totalRides   = totalRides;
            this.totalRevenue = totalRevenue;
            this.avgFare      = avgFare;
        }
    }
}