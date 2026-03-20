package view;
 
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.AdminStats;
 
/**
 * AdminReportsPage — Admin revenue and ride breakdown report.
 * Extends BaseAppPage for sidebar behaviour.
 *
 * Shows: total summary, revenue per vehicle type with visual bar,
 *        rides count per vehicle type.
 * Call refreshReport() each time this scene is shown.
 */
public class AdminReportsPage extends BaseAppPage {
 
    private Label summaryRidesVal;
    private Label summaryRevenueVal;
    private Label summaryAvgFareVal;
    private VBox  reportTable;
 
    public AdminReportsPage() { super(); }
 
    @Override protected String getActiveNavButton() { return "Reports"; }
    @Override protected Button[] getNavButtons() {
        return new Button[]{ dashboardBtn, manageUsersBtn, allRidesBtn, reportsBtn, myProfileBtn };
    }
 
    public void refreshReport() {
        new Thread(() -> {
            AdminStats stats = new AdminStats();
            stats.load();
            Platform.runLater(() -> populateReport(stats));
        }).start();
    }
 
    @Override
    protected VBox buildContent() {
        // Initialise dynamic labels here — safe after super()
        summaryRidesVal   = new Label("—");
        summaryRevenueVal = new Label("—");
        summaryAvgFareVal = new Label("—");
        reportTable       = new VBox(12);
 
        VBox content = new VBox(24);
        content.setPadding(new Insets(30, 35, 30, 35));
        content.setStyle("-fx-background-color: #FDF9E7;");
        HBox.setHgrow(content, Priority.ALWAYS);
 
        // ── Title ────────────────────────────────────────────────────
        Label title = new Label("Reports");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: #444;");
        Label subtitle = new Label("Revenue and ride breakdown by vehicle type.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");
 
        // ── Summary cards ─────────────────────────────────────────────
        HBox summaryRow = new HBox(16);
        summaryRow.getChildren().addAll(
            summaryCard("🚗", "Total Rides",    summaryRidesVal,   "#4A90D9"),
            summaryCard("💰", "Total Revenue",  summaryRevenueVal, "#4CAF50"),
            summaryCard("📊", "Avg Fare / Ride", summaryAvgFareVal, "#E6B833")
        );
 
        // ── Breakdown heading ─────────────────────────────────────────
        Label breakdownTitle = new Label("Revenue by Vehicle Type");
        breakdownTitle.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #444;");
 
        // ── Table header ──────────────────────────────────────────────
        HBox header = buildHeader();
 
        ScrollPane scroll = new ScrollPane(reportTable);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background-color: transparent; -fx-background: transparent; " +
            "-fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
 
        Label loading = new Label("Loading report…");
        loading.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 14px; -fx-padding: 20px;");
        reportTable.getChildren().add(loading);
 
        VBox tableSection = new VBox(8, header, scroll);
        VBox.setVgrow(tableSection, Priority.ALWAYS);
 
        content.getChildren().addAll(
            title, subtitle, summaryRow, breakdownTitle, tableSection);
        return content;
    }
 
    private HBox summaryCard(String emoji, String title,
                              Label valueLabel, String color) {
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
            "-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: " + color + ";");
 
        card.getChildren().addAll(icon, new VBox(3, titleLbl, valueLabel));
        return card;
    }
 
    private HBox buildHeader() {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-background-color: #F2C94C; -fx-background-radius: 10px;");
 
        String[] cols   = { "Vehicle Type", "Total Rides", "Revenue", "Avg Fare", "Share" };
        double[] widths = { 160,             120,           150,       120,         280 };
 
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #FFFFFF;");
            lbl.setPrefWidth(widths[i]);
            row.getChildren().add(lbl);
        }
        return row;
    }
 
    private void populateReport(AdminStats stats) {
        reportTable.getChildren().clear();
 
        // Update summary cards
        int totalRides    = stats.getReport().stream().mapToInt(r -> r.totalRides).sum();
        double totalRev   = stats.getReport().stream().mapToDouble(r -> r.totalRevenue).sum();
        double avgFare    = totalRides > 0 ? totalRev / totalRides : 0;
 
        summaryRidesVal.setText(String.valueOf(totalRides));
        summaryRevenueVal.setText(AdminStats.formatRs(totalRev));
        summaryAvgFareVal.setText(AdminStats.formatRs(avgFare));
 
        if (stats.getReport().isEmpty()) {
            Label empty = new Label("No completed rides yet.");
            empty.setStyle(
                "-fx-text-fill: #AAAAAA; -fx-font-size: 14px; -fx-padding: 16px;");
            reportTable.getChildren().add(empty);
            return;
        }
 
        // Vehicle type accent colours
        String[] vehicleColors = { "#4A90D9", "#E6B833", "#4CAF50", "#9C27B0" };
        int colorIdx = 0;
 
        for (AdminStats.ReportRow rr : stats.getReport()) {
            String accent = vehicleColors[colorIdx % vehicleColors.length];
            colorIdx++;
 
            HBox row = new HBox();
            row.setPadding(new Insets(14, 16, 14, 16));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 12px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");
 
            // Vehicle type with colour dot
            HBox vTypeBox = new HBox(8);
            vTypeBox.setAlignment(Pos.CENTER_LEFT);
            vTypeBox.setPrefWidth(160);
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 14px;");
            Label vLbl = new Label(rr.vehicleType);
            vLbl.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #333;");
            vTypeBox.getChildren().addAll(dot, vLbl);
 
            // Rides count
            Label ridesLbl = cell(String.valueOf(rr.totalRides), 120, "#555", false);
 
            // Revenue
            Label revLbl = cell(AdminStats.formatRs(rr.totalRevenue), 150, "#333", true);
 
            // Avg fare
            Label avgLbl = cell(AdminStats.formatRs(rr.avgFare), 120, "#666", false);
 
            // Visual progress bar (proportional to max revenue)
            double maxRev = stats.getReport().get(0).totalRevenue; // sorted desc
            double pct    = maxRev > 0 ? rr.totalRevenue / maxRev : 0;
 
            HBox barContainer = new HBox();
            barContainer.setPrefWidth(260);
            barContainer.setAlignment(Pos.CENTER_LEFT);
            barContainer.setStyle(
                "-fx-background-color: #F5F5F5; -fx-background-radius: 8px;");
 
            HBox bar = new HBox();
            bar.setPrefWidth(250 * pct);
            bar.setPrefHeight(14);
            bar.setStyle(
                "-fx-background-color: " + accent + "; " +
                "-fx-background-radius: 8px;");
            barContainer.getChildren().add(bar);
 
            row.getChildren().addAll(vTypeBox, ridesLbl, revLbl, avgLbl, barContainer);
            reportTable.getChildren().add(row);
        }
    }
 
    private Label cell(String text, double w, String color, boolean bold) {
        Label l = new Label(text);
        l.setPrefWidth(w);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + "; " +
                   (bold ? "-fx-font-weight: 700;" : ""));
        return l;
    }
}
