package model;
 
import java.sql.*;
 
/**
 * DriverStats — Model layer.
 * Queries the database for a specific driver's stats.
 *
 * Required DB tables (add these if not present):
 *
 *   rides (
 *     id            INT PRIMARY KEY AUTO_INCREMENT,
 *     driver_email  VARCHAR(255),
 *     passenger_name VARCHAR(255),
 *     pickup        VARCHAR(255),
 *     dropoff       VARCHAR(255),
 *     vehicle_type  VARCHAR(50),
 *     distance_km   DOUBLE,
 *     fare          DOUBLE,
 *     status        VARCHAR(50),   -- 'completed', 'active', 'cancelled'
 *     ride_date     DATE,
 *     ride_time     DATETIME DEFAULT CURRENT_TIMESTAMP
 *   )
 *
 *   driver_ratings (
 *     id           INT PRIMARY KEY AUTO_INCREMENT,
 *     driver_email VARCHAR(255),
 *     rating       DOUBLE,
 *     ride_id      INT
 *   )
 */
public class DriverStats {
 
    private final DbConnection db = new DbConnection();
    private final String driverEmail;
 
    // ── Cached values (loaded once per call to load()) ───────────────
    private double todayEarnings  = 0;
    private double monthEarnings  = 0;
    private int    todayRides     = 0;
    private double averageRating  = 0;
    private double acceptanceRate = 0;
 
    // Active ride details (null if no active ride)
    private ActiveRide activeRide = null;
 
    public DriverStats(String driverEmail) {
        this.driverEmail = driverEmail;
    }
 
    // ── Load all stats from DB in one go ─────────────────────────────
    public void load() {
        loadEarningsAndRides();
        loadRating();
        loadAcceptanceRate();
        loadActiveRide();
    }
 
    // ── Today's earnings + monthly earnings + today's ride count ─────
    private void loadEarningsAndRides() {
        String sql = """
            SELECT
                SUM(CASE WHEN DATE(ride_time) = CURDATE() AND status = 'completed'
                         THEN fare ELSE 0 END)           AS today_earn,
                SUM(CASE WHEN MONTH(ride_time) = MONTH(CURDATE())
                              AND YEAR(ride_time) = YEAR(CURDATE())
                              AND status = 'completed'
                         THEN fare ELSE 0 END)           AS month_earn,
                COUNT(CASE WHEN DATE(ride_time) = CURDATE()
                                AND status = 'completed'
                           THEN 1 END)                   AS today_rides
            FROM rides
            WHERE driver_email = ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, driverEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                todayEarnings = rs.getDouble("today_earn");
                monthEarnings = rs.getDouble("month_earn");
                todayRides    = rs.getInt("today_rides");
            }
        } catch (Exception e) {
            System.out.println("DriverStats.loadEarningsAndRides error: " + e.getMessage());
        }
    }
 
    // ── Average rating from driver_ratings table ──────────────────────
    private void loadRating() {
        String sql = "SELECT AVG(rating) AS avg_rating FROM driver_ratings WHERE driver_email = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, driverEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                averageRating = rs.getDouble("avg_rating");
            }
        } catch (Exception e) {
            System.out.println("DriverStats.loadRating error: " + e.getMessage());
        }
    }
 
    // ── Acceptance rate = completed / (completed + cancelled) * 100 ──
    private void loadAcceptanceRate() {
        String sql = """
            SELECT
                COUNT(CASE WHEN status = 'completed' THEN 1 END) AS completed,
                COUNT(CASE WHEN status = 'cancelled' THEN 1 END) AS cancelled
            FROM rides
            WHERE driver_email = ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, driverEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int completed = rs.getInt("completed");
                int cancelled = rs.getInt("cancelled");
                int total = completed + cancelled;
                acceptanceRate = (total > 0)
                    ? Math.round((completed * 100.0 / total) * 10.0) / 10.0
                    : 100.0;
            }
        } catch (Exception e) {
            System.out.println("DriverStats.loadAcceptanceRate error: " + e.getMessage());
        }
    }
 
    // ── Active ride (status = 'active') for this driver ──────────────
    private void loadActiveRide() {
        String sql = """
            SELECT id, passenger_name, pickup, dropoff, distance_km, fare
            FROM rides
            WHERE driver_email = ? AND status = 'active'
            ORDER BY ride_time DESC
            LIMIT 1
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, driverEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                activeRide = new ActiveRide(
                    rs.getInt("id"),
                    rs.getString("passenger_name"),
                    rs.getString("pickup"),
                    rs.getString("dropoff"),
                    rs.getDouble("distance_km"),
                    rs.getDouble("fare")
                );
            } else {
                activeRide = null;
            }
        } catch (Exception e) {
            System.out.println("DriverStats.loadActiveRide error: " + e.getMessage());
        }
    }
 
    // ── Getters ───────────────────────────────────────────────────────
    public double getTodayEarnings()  { return todayEarnings;  }
    public double getMonthEarnings()  { return monthEarnings;  }
    public int    getTodayRides()     { return todayRides;      }
    public double getAverageRating()  { return averageRating;   }
    public double getAcceptanceRate() { return acceptanceRate;  }
    public ActiveRide getActiveRide() { return activeRide;      }
    public boolean hasActiveRide()    { return activeRide != null; }
 
    // ── Helper: format money without trailing .0 ──────────────────────
    public static String formatRs(double amount) {
        if (amount == Math.floor(amount))
            return "Rs. " + (int) amount;
        return String.format("Rs. %.1f", amount);
    }
 
    // ── Inner class: active ride data ─────────────────────────────────
    public static class ActiveRide {
        public final int    id;          // ← ride ID for completeRide()
        public final String passengerName;
        public final String pickup;
        public final String dropoff;
        public final double distanceKm;
        public final double fare;
 
        public ActiveRide(int id, String passengerName, String pickup,
                          String dropoff, double distanceKm, double fare) {
            this.id            = id;
            this.passengerName = passengerName;
            this.pickup        = pickup;
            this.dropoff       = dropoff;
            this.distanceKm    = distanceKm;
            this.fare          = fare;
        }
 
        public String getDistanceStr() {
            return (distanceKm == Math.floor(distanceKm))
                ? (int) distanceKm + " km"
                : distanceKm + " km";
        }
 
        public String getFareStr() {
            return "Rs " + (int) fare;
        }
    }
}