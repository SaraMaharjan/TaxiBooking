package view;
 
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import model.DriverRideService;
import model.DriverRideService.RideRequest;
import model.UserSession;
 
/**
 * DriverRideRequestsPage — Shows real pending ride requests from the DB.
 * Extends BaseAppPage for sidebar.
 *
 * Call refreshRequests() each time this scene is shown.
 * onAccepted callback is set by Main.java to handle post-accept navigation.
 */
public class DriverRideRequestsPage extends BaseAppPage {
 
    // Set by Main.java — called after a ride is successfully accepted
    // so Main can navigate to the dashboard and refresh stats
    public Runnable onAccepted;
 
    private VBox requestList;
 
    public DriverRideRequestsPage() { super(); }
 
    @Override protected String getActiveNavButton() { return "Ride Requests"; }
    @Override protected Button[] getNavButtons() {
        return new Button[]{ dashboardBtn, rideRequestsBtn, myRidesBtn, myProfileBtn };
    }
 
    // Tracks the auto-refresh timer so it can be stopped when navigating away
    private javafx.animation.Timeline autoRefreshTimer;
 
    // ── Called every time this page is shown ─────────────────────────
    public void refreshRequests() {
        if (requestList == null) return;
 
        new Thread(() -> {
            UserSession session = UserSession.getInstance();
            if (session == null) return;
 
            DriverRideService service = new DriverRideService(session.getEmail());
            java.util.List<RideRequest> requests = service.getPendingRequests();
 
            Platform.runLater(() -> {
                requestList.getChildren().clear();
 
                if (requests.isEmpty()) {
                    Label empty = new Label("No pending ride requests right now.");
                    empty.setStyle(
                        "-fx-font-size: 15px; -fx-text-fill: #AAAAAA; " +
                        "-fx-font-weight: 600; -fx-padding: 20px 0;");
                    requestList.getChildren().add(empty);
                } else {
                    for (RideRequest req : requests) {
                        requestList.getChildren().add(buildRequestCard(req, service));
                    }
                }
            });
        }).start();
    }
 
    // ── Start auto-refresh every 5 seconds ───────────────────────────
    public void startAutoRefresh() {
        stopAutoRefresh();
        autoRefreshTimer = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(5),
                e -> refreshRequests()
            )
        );
        autoRefreshTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
        autoRefreshTimer.play();
    }
 
    // ── Stop auto-refresh when leaving the page ───────────────────────
    public void stopAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
            autoRefreshTimer = null;
        }
    }
 
    @Override
    protected VBox buildContent() {
        requestList = new VBox(14);
 
        VBox content = new VBox(16);
        content.setPadding(new Insets(30, 35, 30, 35));
        content.setStyle("-fx-background-color: #FDF9E7;");
        HBox.setHgrow(content, Priority.ALWAYS);
 
        // Title row
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Ride Requests");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: #444;");
        Label emoji = new Label("🚕");
        emoji.setStyle("-fx-font-size: 22px;");
 
        // Refresh button
        Button refreshBtn = new Button("↻  Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: #F2C94C; -fx-background-radius: 10px; " +
            "-fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 13px; " +
            "-fx-padding: 8px 16px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshRequests());
 
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleRow.getChildren().addAll(title, emoji, spacer, refreshBtn);
 
        // Loading placeholder
        Label loading = new Label("Loading requests…");
        loading.setStyle("-fx-font-size: 13px; -fx-text-fill: #AAAAAA;");
        requestList.getChildren().add(loading);
 
        ScrollPane scroll = new ScrollPane(requestList);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background-color: transparent; -fx-background: transparent; " +
            "-fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
 
        content.getChildren().addAll(titleRow, scroll);
        return content;
    }
 
    // ── Build one request card ────────────────────────────────────────
    private HBox buildRequestCard(RideRequest req, DriverRideService service) {
        HBox card = new HBox(18);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 24, 18, 24));
        card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 18px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
 
        // ── Passenger avatar + info ──
        HBox passengerCol = new HBox(12);
        passengerCol.setAlignment(Pos.CENTER_LEFT);
        passengerCol.setPrefWidth(200);
 
        javafx.scene.shape.Circle avatarBg = new javafx.scene.shape.Circle(24);
        avatarBg.setFill(javafx.scene.paint.Color.web("#F2C94C"));
        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(48, 48);
        avatarPane.setStyle("-fx-background-color: transparent;");
        avatarPane.getChildren().add(avatarBg);
        try {
            ImageView iv = new ImageView(
                new Image(getClass().getResourceAsStream("userProfileIcon.png")));
            iv.setFitWidth(48); iv.setFitHeight(48);
            iv.setClip(new Circle(24, 24, 24));
            avatarPane.getChildren().add(iv);
        } catch (Exception ignored) {}
 
        VBox pInfo = new VBox(3);
        Label nameLbl = new Label(req.passengerName);
        nameLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #333;");
        Label timeLbl = new Label("🕒 " + req.getMinsAgoLabel());
        timeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
        pInfo.getChildren().addAll(nameLbl, timeLbl);
        passengerCol.getChildren().addAll(avatarPane, pInfo);
 
        // ── Route ──
        VBox routeCol = new VBox(6);
        routeCol.setPrefWidth(180);
 
        HBox pickRow = new HBox(8);
        pickRow.setAlignment(Pos.CENTER_LEFT);
        Label pickDot = new Label("●");
        pickDot.setStyle("-fx-text-fill: #F2C94C; -fx-font-size: 13px;");
        Label pickLbl = new Label(req.pickup);
        pickLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #333;");
        pickRow.getChildren().addAll(pickDot, pickLbl);
 
        Label conn = new Label("  ⋮");
        conn.setStyle("-fx-text-fill: #BBB; -fx-font-size: 14px;");
 
        HBox dropRow = new HBox(8);
        dropRow.setAlignment(Pos.CENTER_LEFT);
        Label dropPin = new Label("📍");
        dropPin.setStyle("-fx-font-size: 12px;");
        Label dropLbl = new Label(req.dropoff);
        dropLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #333;");
        dropRow.getChildren().addAll(dropPin, dropLbl);
 
        routeCol.getChildren().addAll(pickRow, conn, dropRow);
 
        // ── Vehicle + fare ──
        VBox fareCol = new VBox(5);
        fareCol.setPrefWidth(180);
 
        HBox vehicleRow = new HBox(8);
        vehicleRow.setAlignment(Pos.CENTER_LEFT);
        Label carIcon = new Label("🚗");
        Label vehicleLbl = new Label(req.vehicleType);
        vehicleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #333;");
        vehicleRow.getChildren().addAll(carIcon, vehicleLbl);
 
        Label distLbl = new Label("Total Distance: " + req.getDistStr());
        distLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label fareLbl = new Label("Total Fare: " + req.getFareStr());
        fareLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #333;");
        fareCol.getChildren().addAll(vehicleRow, distLbl, fareLbl);
 
        // ── Accept button ──
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
 
        Button acceptBtn = new Button("Accept");
        acceptBtn.setPrefWidth(100);
        acceptBtn.setPrefHeight(40);
        acceptBtn.setStyle(
            "-fx-background-color: #4CAF50; -fx-background-radius: 10px; " +
            "-fx-text-fill: white; -fx-font-size: 14px; " +
            "-fx-font-weight: 700; -fx-cursor: hand;");
        acceptBtn.setOnMouseEntered(e -> acceptBtn.setStyle(
            "-fx-background-color: #388E3C; -fx-background-radius: 10px; " +
            "-fx-text-fill: white; -fx-font-size: 14px; " +
            "-fx-font-weight: 700; -fx-cursor: hand;"));
        acceptBtn.setOnMouseExited(e -> acceptBtn.setStyle(
            "-fx-background-color: #4CAF50; -fx-background-radius: 10px; " +
            "-fx-text-fill: white; -fx-font-size: 14px; " +
            "-fx-font-weight: 700; -fx-cursor: hand;"));
 
        acceptBtn.setOnAction(e -> {
            acceptBtn.setDisable(true);
            acceptBtn.setText("Accepting…");
 
            new Thread(() -> {
                boolean ok = service.acceptRequest(req.id);
                Platform.runLater(() -> {
                    if (ok) {
                        // Remove this card from the list immediately
                        requestList.getChildren().remove(card);
                        // Notify Main.java to go to dashboard + refresh stats
                        if (onAccepted != null) onAccepted.run();
                    } else {
                        // Ride was already taken by another driver
                        acceptBtn.setText("Taken");
                        acceptBtn.setStyle(
                            "-fx-background-color: #AAAAAA; -fx-background-radius: 10px; " +
                            "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 700;");
                        refreshRequests(); // reload to show fresh list
                    }
                });
            }).start();
        });
 
        card.getChildren().addAll(passengerCol, routeCol, fareCol, sp, acceptBtn);
        return card;
    }
}
 