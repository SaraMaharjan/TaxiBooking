package view;
 
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.AdminStats;
 
/**
 * AdminAllRidesPage — Admin view of all rides across the system.
 * Extends BaseAppPage for sidebar behaviour.
 *
 * Shows every ride with passenger, driver, route, vehicle, fare, status, time.
 * Includes filter buttons: All / Active / Completed / Cancelled.
 * Call refreshRides() each time this scene is shown.
 */
public class AdminAllRidesPage extends BaseAppPage {
 
    private VBox ridesTable;
    private java.util.List<AdminStats.RideRow> allRidesData;
 
    public AdminAllRidesPage() { super(); }
 
    @Override protected String getActiveNavButton() { return "All Rides"; }
    @Override protected Button[] getNavButtons() {
        return new Button[]{ dashboardBtn, manageUsersBtn, allRidesBtn, reportsBtn, myProfileBtn };
    }
 
    public void refreshRides() {
        new Thread(() -> {
            AdminStats stats = new AdminStats();
            stats.load();
            Platform.runLater(() -> {
                allRidesData = stats.getRides();
                showFiltered("All");
            });
        }).start();
    }
 
    @Override
    protected VBox buildContent() {
        ridesTable   = new VBox(10);
        allRidesData = new java.util.ArrayList<>();
 
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 35, 30, 35));
        content.setStyle("-fx-background-color: #FDF9E7;");
        HBox.setHgrow(content, Priority.ALWAYS);
 
        // Title
        Label title = new Label("All Rides");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: #444;");
 
        // Filter buttons
        HBox filters = buildFilterButtons();
 
        // Table header
        HBox header = buildHeader();
 
        // Scrollable ride list
        ScrollPane scroll = new ScrollPane(ridesTable);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background-color: transparent; -fx-background: transparent; " +
            "-fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
 
        Label loading = new Label("Loading rides…");
        loading.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 14px; -fx-padding: 20px;");
        ridesTable.getChildren().add(loading);
 
        VBox tableSection = new VBox(8, header, scroll);
        VBox.setVgrow(tableSection, Priority.ALWAYS);
 
        content.getChildren().addAll(title, filters, tableSection);
        return content;
    }
 
    // ── Filter pill buttons ──────────────────────────────────────────
    private HBox buildFilterButtons() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
 
        String[] labels = { "All", "Active", "Completed", "Cancelled" };
        String[] accents = { "#555555", "#E6B833", "#4CAF50", "#E53935" };
 
        for (int i = 0; i < labels.length; i++) {
            String label  = labels[i];
            String accent = accents[i];
            Button btn = new Button(label);
            btn.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 20px; " +
                "-fx-border-color: " + accent + "; -fx-border-width: 1.5px; " +
                "-fx-border-radius: 20px; -fx-text-fill: " + accent + "; " +
                "-fx-font-size: 13px; -fx-font-weight: 700; " +
                "-fx-padding: 6px 18px; -fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + accent + "; -fx-background-radius: 20px; " +
                "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 700; " +
                "-fx-padding: 6px 18px; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 20px; " +
                "-fx-border-color: " + accent + "; -fx-border-width: 1.5px; " +
                "-fx-border-radius: 20px; -fx-text-fill: " + accent + "; " +
                "-fx-font-size: 13px; -fx-font-weight: 700; " +
                "-fx-padding: 6px 18px; -fx-cursor: hand;"));
            btn.setOnAction(e -> showFiltered(label));
            box.getChildren().add(btn);
        }
        return box;
    }
 
    private HBox buildHeader() {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-background-color: #F2C94C; -fx-background-radius: 10px;");
 
        String[] cols   = { "Passenger", "Driver", "Pickup", "Dropoff",
                             "Vehicle", "Fare", "Status", "Date & Time" };
        double[] widths = { 140,          150,      130,      130,
                             80,    70,     90,       150 };
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #FFFFFF;");
            lbl.setPrefWidth(widths[i]);
            row.getChildren().add(lbl);
        }
        return row;
    }
 
    // ── Filter and display rides ──────────────────────────────────────
    private void showFiltered(String filter) {
        ridesTable.getChildren().clear();
 
        java.util.List<AdminStats.RideRow> filtered = allRidesData.stream()
            .filter(r -> filter.equals("All") ||
                         r.status.equalsIgnoreCase(filter))
            .collect(java.util.stream.Collectors.toList());
 
        if (filtered.isEmpty()) {
            Label empty = new Label("No " + filter.toLowerCase() + " rides found.");
            empty.setStyle(
                "-fx-text-fill: #AAAAAA; -fx-font-size: 14px; -fx-padding: 16px;");
            ridesTable.getChildren().add(empty);
            return;
        }
 
        for (AdminStats.RideRow ride : filtered) {
            HBox row = new HBox();
            row.setPadding(new Insets(11, 16, 11, 16));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");
 
            String statusColor;
            switch (ride.status.toLowerCase()) {
                case "completed": statusColor = "#4CAF50"; break;
                case "active":    statusColor = "#E6B833"; break;
                default:          statusColor = "#E53935";
            }
 
            row.getChildren().addAll(
                cell(ride.passengerName,             140, "#333", true),
                cell(ride.driverEmail,               150, "#666", false),
                cell(trunc(ride.pickup,  16),    130, "#555", false),
                cell(trunc(ride.dropoff, 16),    130, "#555", false),
                cell(ride.vehicleType,               80,  "#333", false),
                cell("Rs " + (int) ride.fare,        70,  "#333", true),
                styledCell(ride.status.toUpperCase(), 90,  statusColor),
                cell(ride.rideTime != null ? ride.rideTime : "—",
                     150, "#888", false)
            );
            ridesTable.getChildren().add(row);
        }
    }
 
    private Label cell(String text, double w, String color, boolean bold) {
        Label l = new Label(text);
        l.setPrefWidth(w);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + "; " +
                   (bold ? "-fx-font-weight: 700;" : ""));
        return l;
    }
 
    private Label styledCell(String text, double w, String color) {
        Label l = new Label(text);
        l.setPrefWidth(w);
        l.setStyle(
            "-fx-font-size: 11px; -fx-font-weight: 800; -fx-text-fill: " + color + ";");
        return l;
    }
 
    private String trunc(String s, int max) {
        if (s == null) return "—";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
