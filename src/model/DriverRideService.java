package model;
 
import java.sql.*;
import java.util.*;
 
/**
 * DriverRideService — Model layer for driver ride operations.
 *
 * getPendingRequests() — reads rides with status='pending' (passenger booked,
 *                        no driver assigned yet)
 * acceptRequest(id)    — assigns this driver to the ride, sets status='active'
 * completeRide(id)     — sets status='completed'
 * getCompletedRides()  — returns this driver's completed ride history
 *
 * The flow:
 *   Passenger confirms booking  → status = 'pending'  (RideBooking.bookRide)
 *   Driver accepts              → status = 'active'   (acceptRequest)
 *   Driver completes            → status = 'completed' (completeRide)
 *   Passenger cancels           → status = 'cancelled' (RideBooking.cancelRide)
 */
public class DriverRideService {
 
    private final DbConnection db  = new DbConnection();
    private final String driverEmail;
 
    public DriverRideService(String driverEmail) {
        this.driverEmail = driverEmail;
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  PENDING REQUESTS  (rides waiting for a driver)
    // ─────────────────────────────────────────────────────────────────
 
    /** Returns all rides with status='pending', most recent first. */
    public List<RideRequest> getPendingRequests() {
        List<RideRequest> list = new ArrayList<>();
        String sql = """
            SELECT id, passenger_name, pickup, dropoff,
                   vehicle_type, distance_km, fare,
                   TIMESTAMPDIFF(MINUTE, ride_time, NOW()) AS mins_ago
            FROM rides
            WHERE status = 'pending'
            ORDER BY ride_time DESC
            LIMIT 20
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                list.add(new RideRequest(
                    rs.getInt("id"),
                    rs.getString("passenger_name"),
                    rs.getString("pickup"),
                    rs.getString("dropoff"),
                    rs.getString("vehicle_type"),
                    rs.getDouble("distance_km"),
                    rs.getDouble("fare"),
                    rs.getInt("mins_ago")
                ));
            }
        } catch (Exception e) {
            System.out.println("DriverRideService.getPendingRequests: " + e.getMessage());
        }
        return list;
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  ACCEPT A RIDE REQUEST
    // ─────────────────────────────────────────────────────────────────
 
    /**
     * Assigns this driver to the ride and sets status='active'.
     * Returns true on success, false if the ride was already taken.
     */
    public boolean acceptRequest(int rideId) {
        // Use a conditional UPDATE to prevent race conditions
        String sql = """
            UPDATE rides
            SET driver_email = ?, status = 'active'
            WHERE id = ? AND status = 'pending'
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setString(1, driverEmail);
            s.setInt(2, rideId);
            return s.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("DriverRideService.acceptRequest: " + e.getMessage());
            return false;
        }
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  COMPLETE AN ACTIVE RIDE
    // ─────────────────────────────────────────────────────────────────
 
    /**
     * Marks the driver's current active ride as completed.
     * Returns true on success.
     */
    public boolean completeRide(int rideId) {
        String sql = """
            UPDATE rides
            SET status = 'completed'
            WHERE id = ? AND driver_email = ? AND status = 'active'
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setInt(1, rideId);
            s.setString(2, driverEmail);
            return s.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("DriverRideService.completeRide: " + e.getMessage());
            return false;
        }
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  COMPLETED RIDE HISTORY FOR THIS DRIVER
    // ─────────────────────────────────────────────────────────────────
 
    /** Returns this driver's completed rides, most recent first. */
    public List<CompletedRide> getCompletedRides() {
        List<CompletedRide> list = new ArrayList<>();
        String sql = """
            SELECT id, passenger_name, pickup, dropoff,
                   vehicle_type, distance_km, fare,
                   CASE
                     WHEN TIMESTAMPDIFF(MINUTE, ride_time, NOW()) < 60
                       THEN CONCAT(TIMESTAMPDIFF(MINUTE, ride_time, NOW()), ' mins ago')
                     WHEN TIMESTAMPDIFF(HOUR, ride_time, NOW()) < 24
                       THEN CONCAT(TIMESTAMPDIFF(HOUR, ride_time, NOW()), 'h ago')
                     ELSE DATE_FORMAT(ride_time, '%b %d, %Y')
                   END AS time_label
            FROM rides
            WHERE driver_email = ? AND status = 'completed'
            ORDER BY ride_time DESC
            LIMIT 20
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setString(1, driverEmail);
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                list.add(new CompletedRide(
                    rs.getInt("id"),
                    rs.getString("passenger_name"),
                    rs.getString("pickup"),
                    rs.getString("dropoff"),
                    rs.getString("vehicle_type"),
                    rs.getDouble("distance_km"),
                    rs.getDouble("fare"),
                    rs.getString("time_label")
                ));
            }
        } catch (Exception e) {
            System.out.println("DriverRideService.getCompletedRides: " + e.getMessage());
        }
        return list;
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  INNER DATA CLASSES
    // ─────────────────────────────────────────────────────────────────
 
    /** A ride waiting to be accepted by a driver. */
    public static class RideRequest {
        public final int    id;
        public final String passengerName;
        public final String pickup;
        public final String dropoff;
        public final String vehicleType;
        public final double distanceKm;
        public final double fare;
        public final int    minsAgo;
 
        public RideRequest(int id, String passengerName,
                           String pickup, String dropoff,
                           String vehicleType, double distanceKm,
                           double fare, int minsAgo) {
            this.id            = id;
            this.passengerName = passengerName;
            this.pickup        = pickup;
            this.dropoff       = dropoff;
            this.vehicleType   = vehicleType;
            this.distanceKm    = distanceKm;
            this.fare          = fare;
            this.minsAgo       = minsAgo;
        }
 
        public String getMinsAgoLabel() {
            if (minsAgo < 1)  return "Just now";
            if (minsAgo < 60) return minsAgo + " mins ago";
            return (minsAgo / 60) + "h ago";
        }
 
        public String getDistStr() {
            return (distanceKm == Math.floor(distanceKm))
                ? (int) distanceKm + " km" : distanceKm + " km";
        }
 
        public String getFareStr() { return "Rs " + (int) fare; }
    }
 
    /** A ride this driver has completed. */
    public static class CompletedRide {
        public final int    id;
        public final String passengerName;
        public final String pickup;
        public final String dropoff;
        public final String vehicleType;
        public final double distanceKm;
        public final double fare;
        public final String timeLabel;
 
        public CompletedRide(int id, String passengerName,
                             String pickup, String dropoff,
                             String vehicleType, double distanceKm,
                             double fare, String timeLabel) {
            this.id            = id;
            this.passengerName = passengerName;
            this.pickup        = pickup;
            this.dropoff       = dropoff;
            this.vehicleType   = vehicleType;
            this.distanceKm    = distanceKm;
            this.fare          = fare;
            this.timeLabel     = timeLabel;
        }
 
        public String getDistStr() {
            return (distanceKm == Math.floor(distanceKm))
                ? (int) distanceKm + " km" : distanceKm + " km";
        }
 
        public String getFareStr() { return "Rs " + (int) fare; }
    }
}
