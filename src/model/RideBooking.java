package model;
 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
 
/**
 * RideBooking — Model layer for passenger ride operations.
 *
 * bookRide()         → inserts a new ride with status='pending'
 * cancelRide()       → updates ride to 'cancelled'
 * getDriverDetails() → fetches the assigned driver info once accepted
 * getActiveRideId()  → finds the passenger's current pending/active ride id
 * getRideHistory()   → returns completed/cancelled rides for history page
 */
public class RideBooking {
 
    private final DbConnection db = new DbConnection();
 
    // ─────────────────────────────────────────────────────────────────
    //  BOOK A RIDE
    //  Inserts a new ride row with status='pending'.
    //  Returns the new ride's auto-generated ID, or -1 on failure.
    // ─────────────────────────────────────────────────────────────────
    public int bookRide(String passengerEmail, String passengerName,
                        String pickup, String dropoff,
                        String vehicleType, double distanceKm, double fare) {
        String sql =
            "INSERT INTO rides " +
            "(driver_email, passenger_name, passenger_email, " +
            " pickup, dropoff, vehicle_type, distance_km, fare, status, ride_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'pending', NOW())";
 
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
 
            stmt.setString(1, "pending");   // driver not assigned yet
            stmt.setString(2, passengerName);
            stmt.setString(3, passengerEmail);
            stmt.setString(4, pickup);
            stmt.setString(5, dropoff);
            stmt.setString(6, vehicleType);
            stmt.setDouble(7, distanceKm);
            stmt.setDouble(8, fare);
 
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    System.out.println("Ride booked — ID: " + id);
                    return id;
                }
            }
        } catch (Exception e) {
            System.out.println("RideBooking.bookRide error: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  CANCEL A RIDE
    //  Works on both pending and active rides.
    //  Returns true if the row was updated.
    // ─────────────────────────────────────────────────────────────────
    public boolean cancelRide(int rideId) {
        String sql =
            "UPDATE rides SET status = 'cancelled' " +
            "WHERE id = ? AND status IN ('pending', 'active')";
 
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, rideId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Ride cancelled — ID: " + rideId);
                return true;
            }
        } catch (Exception e) {
            System.out.println("RideBooking.cancelRide error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  GET DRIVER DETAILS
    //  Called from MyRidesPage after a booking is confirmed.
    //  Joins the active ride to the users table to get the assigned
    //  driver's name, phone, car model, plate number and rating.
    //  Returns null if no driver has been assigned yet (still pending).
    // ─────────────────────────────────────────────────────────────────
    public DriverDetails getDriverDetails(String passengerEmail) {
        String sql =
            "SELECT u.full_name, u.phone, " +
            "       COALESCE(u.car_model,    'Not assigned') AS car_model, " +
            "       COALESCE(u.plate_number, 'N/A')          AS plate_number, " +
            "       COALESCE(" +
            "         (SELECT ROUND(AVG(r.rating), 1) " +
            "          FROM driver_ratings r " +
            "          WHERE r.driver_email = rides.driver_email), 0" +
            "       ) AS avg_rating " +
            "FROM rides " +
            "JOIN users u ON u.email = rides.driver_email " +
            "WHERE rides.passenger_email = ? " +
            "  AND rides.status = 'active' " +
            "ORDER BY rides.ride_time DESC " +
            "LIMIT 1";
 
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setString(1, passengerEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new DriverDetails(
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("car_model"),
                    rs.getString("plate_number"),
                    rs.getDouble("avg_rating")
                );
            }
        } catch (Exception e) {
            System.out.println("RideBooking.getDriverDetails error: " + e.getMessage());
        }
        return null; // ride still pending — no driver yet
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  GET ACTIVE RIDE ID
    //  Returns the ID of the passenger's current pending or active ride.
    //  Returns -1 if none found.
    // ─────────────────────────────────────────────────────────────────
    public int getActiveRideId(String passengerEmail) {
        String sql =
            "SELECT id FROM rides " +
            "WHERE passenger_email = ? AND status IN ('pending', 'active') " +
            "ORDER BY ride_time DESC " +
            "LIMIT 1";
 
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setString(1, passengerEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
 
        } catch (Exception e) {
            System.out.println("RideBooking.getActiveRideId error: " + e.getMessage());
        }
        return -1;
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  GET RIDE HISTORY
    //  Returns up to 10 completed/cancelled rides for the passenger,
    //  most recent first. Used by DashboardPage history section.
    // ─────────────────────────────────────────────────────────────────
    public List<RideHistoryRow> getRideHistory(String passengerEmail) {
        List<RideHistoryRow> history = new ArrayList<>();
        String sql =
            "SELECT dropoff, vehicle_type, fare, status, " +
            "       DATE_FORMAT(ride_time, '%b %d, %Y') AS ride_date, " +
            "       DATE_FORMAT(ride_time, '%l:%i %p')  AS ride_time_fmt " +
            "FROM rides " +
            "WHERE passenger_email = ? " +
            "  AND status IN ('completed', 'cancelled') " +
            "ORDER BY ride_time DESC " +
            "LIMIT 10";
 
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setString(1, passengerEmail);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                history.add(new RideHistoryRow(
                    rs.getString("dropoff"),
                    rs.getString("vehicle_type"),
                    rs.getDouble("fare"),
                    rs.getString("status"),
                    rs.getString("ride_date"),
                    rs.getString("ride_time_fmt")
                ));
            }
        } catch (Exception e) {
            System.out.println("RideBooking.getRideHistory error: " + e.getMessage());
        }
        return history;
    }
 
    // ═════════════════════════════════════════════════════════════════
    //  INNER CLASS: DriverDetails
    //  Holds the assigned driver's information for the passenger view.
    // ═════════════════════════════════════════════════════════════════
    public static class DriverDetails {
        public final String name;
        public final String phone;
        public final String carModel;
        public final String plateNumber;
        public final double rating;
 
        public DriverDetails(String name, String phone,
                             String carModel, String plateNumber,
                             double rating) {
            this.name        = name;
            this.phone       = phone;
            this.carModel    = carModel;
            this.plateNumber = plateNumber;
            this.rating      = rating;
        }
 
        public String getRatingStr() {
            return rating == 0
                ? "No ratings yet"
                : String.format("%.1f", rating);
        }
    }
 
    // ═════════════════════════════════════════════════════════════════
    //  INNER CLASS: RideHistoryRow
    //  Holds one row of ride history for the Dashboard history cards.
    // ═════════════════════════════════════════════════════════════════
    public static class RideHistoryRow {
        public final String dropoff;
        public final String vehicleType;
        public final double fare;
        public final String status;
        public final String rideDate;
        public final String rideTime;
 
        public RideHistoryRow(String dropoff, String vehicleType,
                              double fare, String status,
                              String rideDate, String rideTime) {
            this.dropoff     = dropoff;
            this.vehicleType = vehicleType;
            this.fare        = fare;
            this.status      = status;
            this.rideDate    = rideDate;
            this.rideTime    = rideTime;
        }
 
        /** Returns the icon filename matching this vehicle type. */
        public String getIconFile() {
            switch (vehicleType) {
                case "Mini":     return "miniLogo.png";
                case "Suzuki":   return "suzuki.png";
                case "Mercedes": return "mercedes.png";
                default:         return "carIcon.png";  // Sedan
            }
        }
 
        public String getFareStr() {
            return "Rs " + (int) fare;
        }
    }
}