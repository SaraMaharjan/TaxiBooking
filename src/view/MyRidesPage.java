package view;
 
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import model.RideBooking;
import model.RideBooking.DriverDetails;
 
/**
 * MyRidesPage — Passenger active ride view.
 * Extends BaseAppPage for sidebar behaviour.
 *
 * Call showRideDetails() from Main.java after booking is confirmed.
 * Public cancelBtn is wired in Main.java after showRideDetails() is called.
 */
public class MyRidesPage extends BaseAppPage {
 
    public Button cancelBtn;
    private StackPane contentHolder;
 
    public MyRidesPage() { super(); }
 
    @Override protected String getActiveNavButton() { return "My Rides"; }
    @Override protected Button[] getNavButtons() {
        return new Button[]{ dashboardBtn, bookRideBtn, myRidesBtn, myProfileBtn };
    }
 
    @Override
    protected VBox buildContent() {
        VBox wrapper = new VBox();
        wrapper.setStyle("-fx-background-color: #FDF9E7;");
        VBox.setVgrow(wrapper, Priority.ALWAYS);
 
        contentHolder = new StackPane();
        HBox.setHgrow(contentHolder, Priority.ALWAYS);
        VBox.setVgrow(contentHolder, Priority.ALWAYS);
        contentHolder.setStyle("-fx-background-color: #FDF9E7;");
 
        Label placeholder = new Label("No active ride. Book a ride first!");
        placeholder.setStyle(
            "-fx-font-size: 18px; -fx-text-fill: #AAAAAA; -fx-font-weight: 600;");
        contentHolder.getChildren().add(placeholder);
 
        wrapper.getChildren().add(contentHolder);
        return wrapper;
    }
 
    // ── Called from Main.java after confirm ──────────────────────────
    public void showRideDetails(String pickup, String dropoff,
                                String vehicle, double distance) {
        contentHolder.getChildren().clear();
        VBox rideContent = buildRideContent(pickup, dropoff, vehicle, distance);
        VBox.setVgrow(rideContent, Priority.ALWAYS);
        contentHolder.getChildren().add(rideContent);
    }
 
    private VBox buildRideContent(String pickup, String dropoff,
                                   String vehicle, double distance) {
        VBox content = new VBox(24);
        content.setPadding(new Insets(30, 35, 30, 35));
        content.setStyle("-fx-background-color: #FDF9E7;");
        VBox.setVgrow(content, Priority.ALWAYS);
 
        content.getChildren().addAll(
            buildInfoCard(pickup, dropoff, vehicle, distance),
            buildBottomRow()
        );
        return content;
    }
 
    private HBox buildInfoCard(String pickup, String dropoff,
                                String vehicle, double distance) {
        HBox card = new HBox(30);
        card.setPadding(new Insets(18, 24, 18, 24));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #E2F3E7; -fx-background-radius: 18px;");
 
        // Locations
        VBox locations = new VBox(10);
        HBox pickRow = new HBox(10);
        pickRow.setAlignment(Pos.CENTER_LEFT);
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: #F2C94C; -fx-font-size: 14px;");
        Label pLbl = new Label(pickup.isEmpty() ? "Pickup" : pickup);
        pLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #333;");
        pickRow.getChildren().addAll(dot, pLbl);
 
        Label conn = new Label("⋮");
        conn.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 18px; -fx-padding: 0 0 0 6px;");
 
        HBox dropRow = new HBox(10);
        dropRow.setAlignment(Pos.CENTER_LEFT);
        Label pin = new Label("📍");
        pin.setStyle("-fx-font-size: 14px;");
        Label dLbl = new Label(dropoff.isEmpty() ? "Destination" : dropoff);
        dLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #333;");
        dropRow.getChildren().addAll(pin, dLbl);
        locations.getChildren().addAll(pickRow, conn, dropRow);
 
        // Fare
        double rate = getRateForVehicle(vehicle);
        double fare = rate * distance;
        String distStr = (distance == Math.floor(distance))
            ? (int) distance + "" : String.valueOf(distance);
 
        VBox fareBox = new VBox(6);
        HBox vRow = new HBox(10);
        vRow.setAlignment(Pos.CENTER_LEFT);
        Label carEmoji = new Label("🚗");
        Label vLbl = new Label(vehicle.isEmpty() ? "Sedan" : vehicle);
        vLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #333;");
        vRow.getChildren().addAll(carEmoji, vLbl);
 
        Label distLbl = new Label("Total Distance: " + distStr + " km");
        distLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        Label rateLbl = new Label("Rate: Rs " + (int) rate + " per km");
        rateLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label fareLbl = new Label("Total Fare: Rs " + (int) fare);
        fareLbl.setStyle("-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: #1A1A1A;");
        fareBox.getChildren().addAll(vRow, distLbl, rateLbl, fareLbl);
 
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
 
        // Cancel button
        cancelBtn = new Button("Cancel ✕");
        cancelBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #E53935; " +
            "-fx-font-size: 14px; -fx-font-weight: 700; -fx-cursor: hand; " +
            "-fx-border-color: transparent;");
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #B71C1C; " +
            "-fx-font-size: 14px; -fx-font-weight: 700; -fx-cursor: hand; " +
            "-fx-underline: true; -fx-border-color: transparent;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #E53935; " +
            "-fx-font-size: 14px; -fx-font-weight: 700; -fx-cursor: hand; " +
            "-fx-border-color: transparent;"));
 
        card.getChildren().addAll(locations, fareBox, spacer, cancelBtn);
        return card;
    }
 
    private HBox buildBottomRow() {
        HBox row = new HBox(28);
        row.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(row, Priority.ALWAYS);
        row.getChildren().addAll(buildDriverCard(), buildRideStatusPanel());
        HBox.setHgrow((javafx.scene.Node) row.getChildren().get(1), Priority.ALWAYS);
        return row;
    }
 
    // ── Driver card labels — updated by refreshDriverDetails() ───────
    private Label driverNameLbl;
    private Label driverCarLbl;
    private Label driverPlateLbl;
    private Label driverPhoneLbl;
    private Label driverRatingLbl;
    private Label driverStatusLbl;
 
    // ── Ride status step nodes — updated when driver is assigned ─────
    private javafx.scene.shape.Circle step2Circle;
    private Label                     step2NumLbl;
    private Label                     step2TitleLbl;
    private Label                     step2SubLbl;
 
    private javafx.scene.shape.Circle step3Circle;
    private Label                     step3NumLbl;
    private Label                     step3TitleLbl;
    private Label                     step3SubLbl;
 
    // ── Call this after booking is confirmed ─────────────────────────
    // Polls the DB every 3 seconds. Steps only advance when a real
    // driver accepts — nothing activates automatically.
    public void refreshDriverDetails(String passengerEmail) {
        // Show waiting message in driver card — no step changes yet
        Platform.runLater(() -> {
            if (driverStatusLbl != null) {
                driverStatusLbl.setText("Waiting for a driver to accept...");
                driverStatusLbl.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #AAAAAA; " +
                    "-fx-font-weight: 700;");
            }
        });
 
        new Thread(() -> {
            RideBooking rideBooking = new RideBooking();
            // Poll every 3 seconds for up to 60 seconds
            for (int attempt = 0; attempt < 20; attempt++) {
                DriverDetails details =
                    rideBooking.getDriverDetails(passengerEmail);
 
                if (details != null) {
                    // ── Driver accepted — now update everything ───────
                    final DriverDetails d = details;
                    Platform.runLater(() -> {
                        // Update driver card with real driver info
                        driverNameLbl.setText(d.name);
                        driverCarLbl.setText(d.carModel);
                        driverPlateLbl.setText(d.plateNumber);
                        driverPhoneLbl.setText("Phone: " + d.phone);
                        driverRatingLbl.setText("\u2B50  " + d.getRatingStr());
                        driverStatusLbl.setText("Driver assigned \u2713");
                        driverStatusLbl.setStyle(
                            "-fx-font-size: 12px; -fx-text-fill: #2E7D32; " +
                            "-fx-font-weight: 700;");
 
                        // Step 2 → green (Finding Driver — done)
                        if (step2Circle != null) {
                            step2Circle.setFill(
                                javafx.scene.paint.Color.web("#4CAF50"));
                            step2NumLbl.setText("\u2713");
                            step2NumLbl.setStyle(
                                "-fx-font-size: 13px; -fx-font-weight: 800; " +
                                "-fx-text-fill: white;");
                            step2TitleLbl.setStyle(
                                "-fx-font-size: 14px; -fx-font-weight: 700; " +
                                "-fx-text-fill: #AAAAAA;");
                            step2SubLbl.setStyle(
                                "-fx-font-size: 12px; -fx-text-fill: #CCCCCC;");
                        }
                        // Step 3 → blue (Driver Assigned — active now)
                        if (step3Circle != null) {
                            step3Circle.setFill(
                                javafx.scene.paint.Color.web("#4A90D9"));
                            step3NumLbl.setStyle(
                                "-fx-font-size: 14px; -fx-font-weight: 800; " +
                                "-fx-text-fill: white;");
                            step3TitleLbl.setStyle(
                                "-fx-font-size: 14px; -fx-font-weight: 700; " +
                                "-fx-text-fill: #333333;");
                            step3SubLbl.setStyle(
                                "-fx-font-size: 12px; -fx-text-fill: #777777;");
                        }
                    });
                    return;
                }
 
                // Still waiting — keep the neutral waiting message
                // Do not change any status steps yet
                try { Thread.sleep(3000); }
                catch (InterruptedException ignored) { return; }
            }
            // Timed out after 60 seconds — still no driver
            Platform.runLater(() -> {
                if (driverStatusLbl != null) {
                    driverStatusLbl.setText("Still waiting for a driver...");
                    driverStatusLbl.setStyle(
                        "-fx-font-size: 12px; -fx-text-fill: #AAAAAA;");
                }
            });
        }).start();
    }
 
    private VBox buildDriverCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24, 28, 24, 28));
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(290); card.setMinWidth(290); card.setMaxWidth(290);
        card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 24px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 14, 0, 0, 4);");
 
        Label header = new Label("Driver's Details");
        header.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: #AAAAAA; -fx-font-weight: 600;");
 
        // Avatar
        javafx.scene.shape.Circle bg = new javafx.scene.shape.Circle(45);
        bg.setFill(javafx.scene.paint.Color.web("#F2C94C"));
        StackPane avatar = new StackPane();
        avatar.setPrefSize(90, 90);
        avatar.setStyle("-fx-background-color: transparent;");
        avatar.getChildren().add(bg);
        try {
            ImageView iv = new ImageView(
                new Image(getClass().getResourceAsStream("userProfileIcon.png")));
            iv.setFitWidth(90); iv.setFitHeight(90);
            iv.setClip(new Circle(45, 45, 45));
            avatar.getChildren().add(iv);
        } catch (Exception ignored) {}
 
        // Dynamic labels — start with placeholders
        driverNameLbl   = new Label("Waiting for driver...");
        driverCarLbl    = new Label("—");
        driverPlateLbl  = new Label("—");
        driverPhoneLbl  = new Label("—");
        driverRatingLbl = new Label("—");
        driverStatusLbl = new Label("No driver assigned yet");
 
        driverNameLbl.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #222;");
        driverCarLbl.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: #666;");
        driverPlateLbl.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: #666;");
        driverPhoneLbl.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: #666;");
        driverRatingLbl.setStyle(
            "-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #E6B833;");
        driverStatusLbl.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: #E6B833; -fx-font-weight: 700;");
 
        card.getChildren().addAll(
            header, avatar, driverNameLbl,
            driverStatusLbl, driverCarLbl,
            driverPlateLbl, driverPhoneLbl, driverRatingLbl);
        return card;
    }
 
    private VBox buildRideStatusPanel() {
        VBox panel = new VBox(0);
        panel.setPadding(new Insets(28, 32, 28, 32));
        panel.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 24px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 14, 0, 0, 4);");
        panel.setMinHeight(300);
        HBox.setHgrow(panel, Priority.ALWAYS);
        VBox.setVgrow(panel, Priority.ALWAYS);
 
        Label title = new Label("Ride Status");
        title.setStyle(
            "-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: #444;");
        VBox.setMargin(title, new Insets(0, 0, 24, 0));
 
        panel.getChildren().addAll(title,
            // Step 1 — immediately active (ride was just booked)
            statusStep("1", "Ride Booked",
                "Your ride request has been placed.",
                "#4CAF50", true, null, null, null, null),
            stepConnector(),
            // Step 2 — starts grey, turns yellow when polling begins
            statusStep("2", "Finding Driver",
                "Looking for a nearby driver for you.",
                "#AAAAAA", false,
                c -> step2Circle   = c,
                n -> step2NumLbl   = n,
                t -> step2TitleLbl = t,
                s -> step2SubLbl   = s),
            stepConnector(),
            // Step 3 — starts grey, turns blue when driver accepts
            statusStep("3", "Driver Assigned",
                "A driver has accepted your ride.",
                "#AAAAAA", false,
                c -> step3Circle   = c,
                n -> step3NumLbl   = n,
                t -> step3TitleLbl = t,
                s -> step3SubLbl   = s),
            stepConnector(),
            statusStep("4", "On the Way",
                "Your driver is heading to your pickup.",
                "#AAAAAA", false, null, null, null, null),
            stepConnector(),
            statusStep("5", "Ride Complete",
                "Enjoy your journey!",
                "#AAAAAA", false, null, null, null, null)
        );
 
        return panel;
    }
 
    private HBox statusStep(String number, String title,
                             String subtitle, String color, boolean active,
                             java.util.function.Consumer<javafx.scene.shape.Circle> circleRef,
                             java.util.function.Consumer<Label> numRef,
                             java.util.function.Consumer<Label> titleRef,
                             java.util.function.Consumer<Label> subRef) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));
 
        // Circle with number
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(18);
        circle.setFill(javafx.scene.paint.Color.web(
            active ? color : "#EEEEEE"));
        StackPane circlePane = new StackPane(circle);
        circlePane.setPrefSize(36, 36);
        circlePane.setMinSize(36, 36);
        circlePane.setMaxSize(36, 36);
 
        Label numLbl = new Label(number);
        numLbl.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: 800; " +
            "-fx-text-fill: " + (active ? "white" : "#BBBBBB") + ";");
        circlePane.getChildren().add(numLbl);
 
        // Text
        VBox textCol = new VBox(3);
        Label titleLbl = new Label(title);
        titleLbl.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: 700; " +
            "-fx-text-fill: " + (active ? "#333333" : "#BBBBBB") + ";");
        Label subtitleLbl = new Label(subtitle);
        subtitleLbl.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: " + (active ? "#777777" : "#CCCCCC") + ";");
        textCol.getChildren().addAll(titleLbl, subtitleLbl);
 
        row.getChildren().addAll(circlePane, textCol);
 
        // Store references if callbacks provided
        if (circleRef != null) circleRef.accept(circle);
        if (numRef    != null) numRef.accept(numLbl);
        if (titleRef  != null) titleRef.accept(titleLbl);
        if (subRef    != null) subRef.accept(subtitleLbl);
 
        return row;
    }
 
    private VBox stepConnector() {
        VBox line = new VBox();
        line.setPrefHeight(14);
        line.setMinHeight(14);
        line.setMaxHeight(14);
        line.setPadding(new Insets(0, 0, 0, 17));
        Label dash = new Label("│");
        dash.setStyle("-fx-text-fill: #DDDDDD; -fx-font-size: 14px;");
        line.getChildren().add(dash);
        return line;
    }
 
    private double getRateForVehicle(String vehicle) {
        switch (vehicle) {
            case "Mini":     return 15;
            case "Sedan":    return 20;
            case "Suzuki":   return 26;
            case "Mercedes": return 32;
            default:         return 20;
        }
    }
}
 
 
 