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
import model.DriverRideService.CompletedRide;
import model.UserSession;
 
/**
 * DriverMyRidesPage — Shows the driver's real completed ride history from DB.
 * Extends BaseAppPage for sidebar.
 *
 * Call refreshRides() each time this scene is shown.
 */
public class DriverMyRidesPage extends BaseAppPage {
 
    private VBox ridesList;
 
    public DriverMyRidesPage() { super(); }
 
    @Override protected String getActiveNavButton() { return "My Rides"; }
    @Override protected Button[] getNavButtons() {
        return new Button[]{ dashboardBtn, rideRequestsBtn, myRidesBtn, myProfileBtn };
    }
 
    // ── Called every time this scene is shown ────────────────────────
    public void refreshRides() {
        if (ridesList == null) return;
 
        new Thread(() -> {
            UserSession session = UserSession.getInstance();
            if (session == null) return;
 
            DriverRideService service = new DriverRideService(session.getEmail());
            java.util.List<CompletedRide> rides = service.getCompletedRides();
 
            Platform.runLater(() -> {
                ridesList.getChildren().clear();
 
                if (rides.isEmpty()) {
                    Label empty = new Label("No completed rides yet.");
                    empty.setStyle(
                        "-fx-font-size: 15px; -fx-text-fill: #AAAAAA; " +
                        "-fx-font-weight: 600; -fx-padding: 20px 0;");
                    ridesList.getChildren().add(empty);
                } else {
                    for (CompletedRide ride : rides) {
                        ridesList.getChildren().add(buildRideCard(ride));
                    }
                }
            });
        }).start();
    }
 
    @Override
    protected VBox buildContent() {
        ridesList = new VBox(14);
 
        VBox content = new VBox(16);
        content.setPadding(new Insets(30, 35, 30, 35));
        content.setStyle("-fx-background-color: #FDF9E7;");
        HBox.setHgrow(content, Priority.ALWAYS);
 
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("My Rides");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: #444;");
        Label emoji = new Label("🚕");
        emoji.setStyle("-fx-font-size: 22px;");
        titleRow.getChildren().addAll(title, emoji);
 
        // Loading placeholder
        Label loading = new Label("Loading ride history…");
        loading.setStyle("-fx-font-size: 13px; -fx-text-fill: #AAAAAA;");
        ridesList.getChildren().add(loading);
 
        ScrollPane scroll = new ScrollPane(ridesList);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background-color: transparent; -fx-background: transparent; " +
            "-fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
 
        content.getChildren().addAll(titleRow, scroll);
        return content;
    }
 
    // ── Build one completed ride card ─────────────────────────────────
    private HBox buildRideCard(CompletedRide ride) {
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
        Label nameLbl = new Label(ride.passengerName);
        nameLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #333;");
        Label timeLbl = new Label("🕒 " + ride.timeLabel);
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
        Label pickLbl = new Label(ride.pickup);
        pickLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #333;");
        pickRow.getChildren().addAll(pickDot, pickLbl);
 
        Label conn = new Label("  ⋮");
        conn.setStyle("-fx-text-fill: #BBB; -fx-font-size: 14px;");
 
        HBox dropRow = new HBox(8);
        dropRow.setAlignment(Pos.CENTER_LEFT);
        Label dropPin = new Label("📍");
        dropPin.setStyle("-fx-font-size: 12px;");
        Label dropLbl = new Label(ride.dropoff);
        dropLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #333;");
        dropRow.getChildren().addAll(dropPin, dropLbl);
 
        routeCol.getChildren().addAll(pickRow, conn, dropRow);
 
        // ── Vehicle + fare ──
        VBox fareCol = new VBox(5);
 
        HBox vehicleRow = new HBox(8);
        vehicleRow.setAlignment(Pos.CENTER_LEFT);
        Label carIcon = new Label("🚗");
        Label vehicleLbl = new Label(ride.vehicleType);
        vehicleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #333;");
        vehicleRow.getChildren().addAll(carIcon, vehicleLbl);
 
        Label distLbl = new Label("Total Distance: " + ride.getDistStr());
        distLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label fareLbl = new Label("Total Fare: " + ride.getFareStr());
        fareLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #333;");
        fareCol.getChildren().addAll(vehicleRow, distLbl, fareLbl);
 
        // ── Completed badge ──
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label badge = new Label("✓ Completed");
        badge.setStyle(
            "-fx-background-color: #E8F5E9; -fx-background-radius: 10px; " +
            "-fx-text-fill: #2E7D32; -fx-font-size: 11px; " +
            "-fx-font-weight: 700; -fx-padding: 4px 10px;");
 
        card.getChildren().addAll(passengerCol, routeCol, fareCol, sp, badge);
        return card;
    }
}