package view;
 
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
 
/**
 * BookRidePage — Passenger ride booking screen.
 * Extends BaseAppPage for sidebar behaviour.
 *
 * Simple text fields for pickup, dropoff and distance.
 * Distance field only accepts numeric input.
 * Fare is calculated from distance x rate when Confirm is clicked.
 */
public class BookRidePage extends BaseAppPage {
 
    // All assigned in buildContent() — do NOT initialise here
    public Button confirmBtn;
 
    private TextField pickupField;
    private TextField dropoffField;
    private TextField distanceField;
 
    private VBox   selectedVehicleCard;
    private String selectedVehicleName = "";
 
    private static final double RATE_MINI     = 15;
    private static final double RATE_SEDAN    = 20;
    private static final double RATE_SUZUKI   = 26;
    private static final double RATE_MERCEDES = 32;
 
    public BookRidePage() { super(); }
 
    // ── Getters used by Main.java ────────────────────────────────────
    public String getPickupLocation()  {
        return pickupField != null ? pickupField.getText().trim() : "";
    }
    public String getDropoffLocation() {
        return dropoffField != null ? dropoffField.getText().trim() : "";
    }
    public String getSelectedVehicleType() {
        return selectedVehicleName.isEmpty() ? "None" : selectedVehicleName;
    }
    public double getDistance() {
        try {
            return Double.parseDouble(distanceField.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
 
    @Override protected String getActiveNavButton() { return "Book a Ride"; }
    @Override protected Button[] getNavButtons() {
        return new Button[]{ dashboardBtn, bookRideBtn, myRidesBtn, myProfileBtn };
    }
 
    @Override
    protected VBox buildContent() {
        // Initialise all fields here — safe after super()
        confirmBtn    = new Button("Confirm \u25B6");
        pickupField   = new TextField();
        dropoffField  = new TextField();
        distanceField = new TextField();
 
        VBox main = new VBox(24);
        main.setPadding(new Insets(35, 50, 35, 50));
        main.setStyle("-fx-background-color: #FDF9E7;");
        HBox.setHgrow(main, Priority.ALWAYS);
 
        Label title = new Label("BOOK YOUR RIDE!");
        title.setStyle(
            "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #E6B833;");
 
        HBox bookingLayout = new HBox(30);
        bookingLayout.setAlignment(Pos.TOP_CENTER);
        bookingLayout.getChildren().addAll(buildLocationPanel(), buildVehiclePanel());
        HBox.setHgrow(bookingLayout, Priority.ALWAYS);
 
        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.BOTTOM_RIGHT);
        confirmBtn.getStyleClass().add("confirm-button");
        bottomBar.getChildren().add(confirmBtn);
        VBox.setVgrow(bottomBar, Priority.ALWAYS);
 
        main.getChildren().addAll(title, bookingLayout, bottomBar);
        return main;
    }
 
    // ── Location + distance panel ────────────────────────────────────
    private VBox buildLocationPanel() {
        VBox box = new VBox(18);
        box.setPadding(new Insets(25));
        box.setStyle(
            "-fx-background-color: white; -fx-background-radius: 15px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.10), 10, 0, 0, 5);");
        box.setPrefWidth(400);
 
        Label where = new Label("Where to? \uD83D\uDE95");
        where.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #444;");
 
        // Pickup
        pickupField.setPromptText("Enter pickup location");
        pickupField.getStyleClass().add("booking-field");
 
        // Dropoff
        dropoffField.setPromptText("Enter destination");
        dropoffField.getStyleClass().add("booking-field");
 
        // Distance — numeric only validation
        distanceField.setPromptText("e.g. 3.5");
        distanceField.getStyleClass().add("booking-field");
        distanceField.textProperty().addListener((obs, old, nw) -> {
            if (!nw.matches("\\d*\\.?\\d*")) distanceField.setText(old);
        });
 
        Separator sep = new Separator();
 
        box.getChildren().addAll(
            where,
            fieldGroup("\u25CF Pickup Location",   "#F2C94C", pickupField),
            fieldGroup("\uD83D\uDCCD Destination",  "#FF69B4", dropoffField),
            sep,
            fieldGroup("\uD83D\uDCCF Distance (km)", "#4CAF50", distanceField)
        );
        return box;
    }
 
    private VBox fieldGroup(String labelText, String color, TextField field) {
        Label lbl = new Label(labelText);
        lbl.setStyle(
            "-fx-text-fill: " + color + "; " +
            "-fx-font-weight: bold; -fx-font-size: 13px;");
        return new VBox(7, lbl, field);
    }
 
    // ── Vehicle selection panel ───────────────────────────────────────
    private VBox buildVehiclePanel() {
        VBox box = new VBox(18);
        box.setPadding(new Insets(25));
        box.setStyle(
            "-fx-background-color: white; -fx-background-radius: 15px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.10), 10, 0, 0, 5);");
 
        Label choose = new Label("Choose Vehicle Type");
        choose.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #444;");
 
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.add(vehicleCard("Mini",     "miniLogo.png",  50, "4", "3",  RATE_MINI),     0, 0);
        grid.add(vehicleCard("Sedan",    "carIcon.png",   28, "4", "5",  RATE_SEDAN),    1, 0);
        grid.add(vehicleCard("Suzuki",   "suzuki.png",    32, "6", "8",  RATE_SUZUKI),   0, 1);
        grid.add(vehicleCard("Mercedes", "mercedes.png",  24, "8", "10", RATE_MERCEDES), 1, 1);
 
        box.getChildren().addAll(choose, grid);
        return box;
    }
 
    // ── One vehicle card ──────────────────────────────────────────────
    private VBox vehicleCard(String type, String iconFile, double iconSize,
                              String passengers, String mins, double rate) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setPrefSize(175, 120);
        card.setStyle(
            "-fx-background-color: #FDF9E7; -fx-background-radius: 10px;" +
            "-fx-cursor: hand; -fx-border-color: transparent; -fx-border-width: 2;");
 
        card.setOnMouseClicked(e -> {
            if (selectedVehicleCard != null) {
                selectedVehicleCard.setStyle(
                    "-fx-background-color: #FDF9E7; -fx-background-radius: 10px;" +
                    "-fx-cursor: hand; -fx-border-color: transparent; -fx-border-width: 2;");
            }
            selectedVehicleCard = card;
            selectedVehicleName = type;
            card.setStyle(
                "-fx-background-color: #FFF3C4; -fx-background-radius: 10px;" +
                "-fx-cursor: hand; -fx-border-color: #F2C94C; -fx-border-width: 2;");
        });
 
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        ImageView icon = loadImg(iconFile, iconSize);
        Label name = new Label(type);
        name.setStyle(
            "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");
        header.getChildren().addAll(icon != null ? icon : new Label(), name);
 
        Label meta = new Label("\uD83D\uDC64 " + passengers +
                               "   \uD83D\uDD52 " + mins + " mins");
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        Label rateLbl = new Label("Rs " + (int) rate + " per km");
        rateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
 
        card.getChildren().addAll(header, meta, rateLbl);
        return card;
    }
 
    private ImageView loadImg(String file, double size) {
        try {
            ImageView iv = new ImageView(
                new Image(getClass().getResourceAsStream(file)));
            iv.setFitWidth(size);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            return iv;
        } catch (Exception e) { return null; }
    }
}
 