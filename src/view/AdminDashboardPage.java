package view;
 
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.AdminStats;
import model.UserSession;
 
/**
 * AdminDashboardPage — Admin home screen.
 * Extends BaseAppPage for sidebar, nav, and avatar via inheritance.
 *
 * Shows: total passengers, total drivers, total revenue,
 *        active rides right now, and recent ride activity table.
 *
 * Call refreshDashboard() each time this scene is shown.
 */
public class AdminDashboardPage extends BaseAppPage {
 
    // Stat card labels — assigned in buildContent(), NOT at field level
    private Label totalPassengersVal;
    private Label totalDriversVal;
    private Label totalRevenueVal;
    private Label activeRidesVal;
 
    // Recent rides table
    private VBox ridesTable;
 
    public AdminDashboardPage() { super(); }
 
    @Override protected String getActiveNavButton() { return "Dashboard"; }
    @Override protected Button[] getNavButtons() {
        return new Button[]{ dashboardBtn, manageUsersBtn, allRidesBtn, reportsBtn, myProfileBtn };
    }
 
    // ── Refresh live data from DB ────────────────────────────────────
    public void refreshDashboard() {
        new Thread(() -> {
            AdminStats stats = new AdminStats();
            stats.load();
            Platform.runLater(() -> {
                totalPassengersVal.setText(String.valueOf(stats.getTotalPassengers()));
                totalDriversVal.setText(String.valueOf(stats.getTotalDrivers()));
                totalRevenueVal.setText(AdminStats.formatRs(stats.getTotalRevenue()));
                activeRidesVal.setText(String.valueOf(stats.getActiveRides()));
                populateRidesTable(stats);
            });
        }).start();
    }
 
    @Override
    protected VBox buildContent() {
        // Initialise dynamic labels here — safe after super()
        totalPassengersVal = new Label("—");
        totalDriversVal    = new Label("—");
        totalRevenueVal    = new Label("—");
        activeRidesVal     = new Label("—");
        ridesTable         = new VBox(10);
 
        VBox content = new VBox(24);
        content.setPadding(new Insets(30, 35, 30, 35));
        content.setStyle("-fx-background-color: #FDF9E7;");
        HBox.setHgrow(content, Priority.ALWAYS);
 
        // ── Page title ───────────────────────────────────────────────
        Label title = new Label("Admin Dashboard");
        title.setStyle(
            "-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: #444;");
 
        // ── 4 stat cards ─────────────────────────────────────────────
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
            statCard("👥", "Total Passengers", totalPassengersVal, "#4A90D9"),
            statCard("🚗", "Total Drivers",    totalDriversVal,    "#E6B833"),
            statCard("💰", "Total Revenue",    totalRevenueVal,    "#4CAF50"),
            statCard("📍", "Active Rides",     activeRidesVal,     "#E53935")
        );
 
        // ── Recent rides section ──────────────────────────────────────
        Label recentTitle = new Label("Recent Rides");
        recentTitle.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #444;");
 
        // Table header
        HBox header = tableHeader();
 
        ScrollPane scroll = new ScrollPane(ridesTable);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background-color: transparent; -fx-background: transparent; " +
            "-fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
 
        VBox tableSection = new VBox(8, recentTitle, header, scroll);
        VBox.setVgrow(tableSection, Priority.ALWAYS);
 
        content.getChildren().addAll(title, statsRow, tableSection);
        return content;
    }
 
    // ── Stat card ────────────────────────────────────────────────────
    private HBox statCard(String emoji, String title,
                           Label valueLabel, String accentColor) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 16px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        HBox.setHgrow(card, Priority.ALWAYS);
 
        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 28px;");
 
        Label titleLbl = new Label(title);
        titleLbl.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: #888; -fx-font-weight: 600;");
        valueLabel.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: " + accentColor + ";");
 
        card.getChildren().addAll(icon, new VBox(3, titleLbl, valueLabel));
        return card;
    }
 
    // ── Table header row ─────────────────────────────────────────────
    private HBox tableHeader() {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle(
            "-fx-background-color: #F2C94C; -fx-background-radius: 10px;");
 
        String[] cols   = { "Passenger", "Driver", "Route", "Vehicle", "Fare", "Status" };
        double[] widths = { 180,          180,       220,     100,       90,     110 };
 
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #FFFFFF;");
            lbl.setPrefWidth(widths[i]);
            row.getChildren().add(lbl);
        }
        return row;
    }
 
    // ── Populate table with ride data ─────────────────────────────────
    private void populateRidesTable(AdminStats stats) {
        ridesTable.getChildren().clear();
 
        if (stats.getRides().isEmpty()) {
            Label empty = new Label("No rides found.");
            empty.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 14px; -fx-padding: 16px;");
            ridesTable.getChildren().add(empty);
            return;
        }
 
        // Show only most recent 8 on dashboard
        stats.getRides().stream().limit(8).forEach(ride -> {
            HBox row = new HBox();
            row.setPadding(new Insets(12, 16, 12, 16));
            row.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");
 
            String route = ride.pickup.length() > 14
                ? ride.pickup.substring(0, 14) + "…" : ride.pickup;
            route += " → ";
            route += ride.dropoff.length() > 14
                ? ride.dropoff.substring(0, 14) + "…" : ride.dropoff;
 
            String[] values = {
                ride.passengerName, ride.driverEmail,
                route, ride.vehicleType,
                "Rs " + (int) ride.fare, ride.status.toUpperCase()
            };
            double[] widths  = { 180, 180, 220, 100, 90, 110 };
            String[] colors  = {
                "#333", "#666", "#555", "#333", "#333",
                statusColor(ride.status)
            };
 
            for (int i = 0; i < values.length; i++) {
                Label cell = new Label(values[i]);
                cell.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: " + colors[i] + "; " +
                    (i == 5 ? "-fx-font-weight: 700;" : ""));
                cell.setPrefWidth(widths[i]);
                row.getChildren().add(cell);
            }
            ridesTable.getChildren().add(row);
        });
    }
 
    private String statusColor(String status) {
        switch (status.toLowerCase()) {
            case "completed":  return "#4CAF50";
            case "active":     return "#E6B833";
            case "cancelled":  return "#E53935";
            default:           return "#888888";
        }
    }
}