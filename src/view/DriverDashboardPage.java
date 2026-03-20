package view;
 
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import model.DriverStats;
import model.UserSession;
 
/**
 * DriverDashboardPage — Driver home screen.
 * Extends BaseAppPage for sidebar behaviour.
 *
 * Call refreshDashboard() each time this scene is shown to load
 * live stats from the database for the logged-in driver.
 */
public class DriverDashboardPage extends BaseAppPage {
 
    // All assigned inside buildContent() / buildActiveRideCard()
    // DO NOT initialise inline — super() runs before child fields are set
    public Button completeRideBtn;
    public Button callPassengerBtn;
 
    private Label todayEarningVal;
    private Label monthEarningVal;
    private Label todayRidesVal;
    private Label ratingVal;
    private Label acceptanceVal;
 
    private Label activePassengerName;
    private Label activePickup;
    private Label activeDropoff;
    private Label activeDist;
    private Label activeFare;
    private Label activeRideStatusBadge;
 
    public DriverDashboardPage() { super(); }
 
    @Override protected String getActiveNavButton() { return "Dashboard"; }
    @Override protected Button[] getNavButtons() {
        return new Button[]{ dashboardBtn, rideRequestsBtn, myRidesBtn, myProfileBtn };
    }
 
    // ── Live DB refresh ──────────────────────────────────────────────
    public void refreshDashboard() {
        UserSession session = UserSession.getInstance();
        if (session == null) return;
 
        new Thread(() -> {
            DriverStats stats = new DriverStats(session.getEmail());
            stats.load();
 
            javafx.application.Platform.runLater(() -> {
                todayEarningVal.setText(DriverStats.formatRs(stats.getTodayEarnings()));
                monthEarningVal.setText(DriverStats.formatRs(stats.getMonthEarnings()));
                todayRidesVal.setText(String.valueOf(stats.getTodayRides()));
 
                double r = stats.getAverageRating();
                ratingVal.setText(r == 0 ? "N/A" : String.format("%.1f", r));
                acceptanceVal.setText((int) stats.getAcceptanceRate() + "%");
 
                if (stats.hasActiveRide()) {
                    DriverStats.ActiveRide ride = stats.getActiveRide();
                    activePassengerName.setText(ride.passengerName);
                    activePickup.setText(ride.pickup);
                    activeDropoff.setText(ride.dropoff);
                    activeDist.setText("Total Distance: " + ride.getDistanceStr());
                    activeFare.setText("Total Fare: " + ride.getFareStr());
                    activeRideStatusBadge.setText("On the way");
                    activeRideStatusBadge.setStyle(
                        "-fx-background-color: #4CAF50; -fx-background-radius: 20px; " +
                        "-fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 13px; " +
                        "-fx-padding: 6px 16px;");
                    completeRideBtn.setDisable(false);
                    callPassengerBtn.setDisable(false);
                } else {
                    activePassengerName.setText("No active ride");
                    activePickup.setText("—"); activeDropoff.setText("—");
                    activeDist.setText(""); activeFare.setText("");
                    activeRideStatusBadge.setText("Idle");
                    activeRideStatusBadge.setStyle(
                        "-fx-background-color: #AAAAAA; -fx-background-radius: 20px; " +
                        "-fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 13px; " +
                        "-fx-padding: 6px 16px;");
                    completeRideBtn.setDisable(true);
                    callPassengerBtn.setDisable(true);
                }
            });
        }).start();
    }
 
    @Override
    protected VBox buildContent() {
        // Initialise all fields here — this runs after super(), so it's safe
        todayEarningVal      = new Label("Rs. 0");
        monthEarningVal      = new Label("Rs. 0");
        todayRidesVal        = new Label("0");
        ratingVal            = new Label("0.0");
        acceptanceVal        = new Label("0%");
        activePassengerName  = new Label("No active ride");
        activePickup         = new Label("—");
        activeDropoff        = new Label("—");
        activeDist           = new Label("");
        activeFare           = new Label("");
        activeRideStatusBadge = new Label("Idle");
        completeRideBtn      = new Button("Complete Ride");
        callPassengerBtn     = new Button("Call Passenger");
 
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 35, 30, 35));
        content.setStyle("-fx-background-color: #FDF9E7;");
        HBox.setHgrow(content, Priority.ALWAYS);
 
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
            statCard("💵", "Today's Earning", todayEarningVal),
            statCard("📊", "This month",      monthEarningVal),
            statCard("🚗", "Today's Rides",   todayRidesVal)
        );
 
        HBox bottomRow = new HBox(20);
        VBox.setVgrow(bottomRow, Priority.ALWAYS);
        bottomRow.getChildren().addAll(buildActiveRideCard(), buildPerformanceCard());
 
        content.getChildren().addAll(statsRow, bottomRow);
        return content;
    }
 
    private HBox statCard(String emoji, String title, Label valueLabel) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 20, 14, 20));
        card.setStyle(
            "-fx-background-color: #FFF3C4; -fx-background-radius: 14px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 6, 0, 0, 2);");
        HBox.setHgrow(card, Priority.ALWAYS);
 
        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 26px;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-font-weight: 600;");
        valueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #444;");
 
        card.getChildren().addAll(icon, new VBox(3, titleLbl, valueLabel));
        return card;
    }
 
    private VBox buildActiveRideCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(22, 26, 22, 26));
        card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 20px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0, 0, 3);");
        HBox.setHgrow(card, Priority.ALWAYS);
        VBox.setVgrow(card, Priority.ALWAYS);
 
        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Label activeTitle = new Label("Active Ride");
        activeTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #555;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        activeRideStatusBadge.setStyle(
            "-fx-background-color: #AAAAAA; -fx-background-radius: 20px; " +
            "-fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 13px; " +
            "-fx-padding: 6px 16px;");
        header.getChildren().addAll(activeTitle, sp, activeRideStatusBadge);
 
        // Passenger row
        HBox pRow = new HBox(12);
        pRow.setAlignment(Pos.CENTER_LEFT);
        pRow.setPadding(new Insets(10, 14, 10, 14));
        pRow.setStyle("-fx-background-color: #FFF9E0; -fx-background-radius: 12px;");
 
        javafx.scene.shape.Circle pBg = new javafx.scene.shape.Circle(22);
        pBg.setFill(javafx.scene.paint.Color.web("#F2C94C"));
        StackPane pAvatar = new StackPane();
        pAvatar.setPrefSize(44, 44);
        pAvatar.setStyle("-fx-background-color: transparent;");
        pAvatar.getChildren().add(pBg);
        try {
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("userProfileIcon.png")));
            iv.setFitWidth(44); iv.setFitHeight(44);
            iv.setClip(new Circle(22, 22, 22));
            pAvatar.getChildren().add(iv);
        } catch (Exception ignored) {}
 
        activePassengerName.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #333;");
        Region pSp = new Region(); HBox.setHgrow(pSp, Priority.ALWAYS);
 
        callPassengerBtn.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 10px; " +
            "-fx-border-color: #DDDDDD; -fx-border-width: 1.5px; -fx-border-radius: 10px; " +
            "-fx-text-fill: #555; -fx-font-size: 13px; -fx-font-weight: 600; " +
            "-fx-padding: 8px 16px; -fx-cursor: hand;");
        callPassengerBtn.setDisable(true);
        pRow.getChildren().addAll(pAvatar, new VBox(2, activePassengerName), pSp, callPassengerBtn);
 
        // Route + fare
        HBox routeFare = new HBox(40);
        routeFare.setAlignment(Pos.CENTER_LEFT);
 
        VBox routeCol = new VBox(6);
        HBox pick = new HBox(8); pick.setAlignment(Pos.CENTER_LEFT);
        Label pickDot = new Label("●"); pickDot.setStyle("-fx-text-fill: #F2C94C;");
        activePickup.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #333;");
        pick.getChildren().addAll(pickDot, activePickup);
 
        Label connLbl = new Label("  ⋮"); connLbl.setStyle("-fx-text-fill: #BBB;");
 
        HBox drop = new HBox(8); drop.setAlignment(Pos.CENTER_LEFT);
        Label dropPin = new Label("📍"); dropPin.setStyle("-fx-font-size: 12px;");
        activeDropoff.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #333;");
        drop.getChildren().addAll(dropPin, activeDropoff);
        routeCol.getChildren().addAll(pick, connLbl, drop);
 
        VBox fareCol = new VBox(4);
        activeDist.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");
        activeFare.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #333;");
        fareCol.getChildren().addAll(activeDist, activeFare);
        routeFare.getChildren().addAll(routeCol, fareCol);
 
        // Complete button
        completeRideBtn.setMaxWidth(Double.MAX_VALUE);
        completeRideBtn.setPrefHeight(48);
        completeRideBtn.setStyle(
            "-fx-background-color: #4CAF50; -fx-background-radius: 12px; " +
            "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700; -fx-cursor: hand;");
        completeRideBtn.setDisable(true);
 
        card.getChildren().addAll(header, pRow, routeFare, completeRideBtn);
        return card;
    }
 
    private VBox buildPerformanceCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(22, 24, 22, 24));
        card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 20px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0, 0, 3);");
        card.setPrefWidth(240); card.setMinWidth(200);
        VBox.setVgrow(card, Priority.ALWAYS);
 
        Label title = new Label("Performance");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #444;");
 
        ratingVal.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #F2C94C;");
        acceptanceVal.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #4CAF50;");
 
        Label statusBadge = new Label("🟢  Online");
        statusBadge.setStyle(
            "-fx-background-color: #E8F5E9; -fx-background-radius: 12px; " +
            "-fx-text-fill: #2E7D32; -fx-font-size: 13px; -fx-font-weight: 700; " +
            "-fx-padding: 4px 12px;");
 
        card.getChildren().addAll(title,
            perfRow("⭐", "Rating",     ratingVal),
            perfRow("🎯", "Acceptance", acceptanceVal),
            statusRow(statusBadge));
        return card;
    }
 
    private HBox perfRow(String icon, String label, Label val) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: #FFF9E0; -fx-background-radius: 10px;");
        Label i = new Label(icon); i.setStyle("-fx-font-size: 18px;");
        Label n = new Label(label); n.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #555;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        row.getChildren().addAll(i, n, sp, val);
        return row;
    }
 
    private HBox statusRow(Label badge) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: #FFF9E0; -fx-background-radius: 10px;");
        Label n = new Label("Status"); n.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #555;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        row.getChildren().addAll(n, sp, badge);
        return row;
    }
}
