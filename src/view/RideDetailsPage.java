package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class RideDetailsPage extends VBox {
    
    public Button cancelBtn = new Button("Cancel X");

    public RideDetailsPage(String pickup, String dropoff, String vehicle) {
        this.setSpacing(20);
        this.setPadding(new Insets(30));
        this.setStyle("-fx-background-color: #fdf9e7;"); // Match main background

        // --- 1. Top Green Info Card ---
        HBox infoCard = new HBox(40);
        infoCard.setPadding(new Insets(20));
        infoCard.setStyle("-fx-background-color: #e2f3e7; -fx-background-radius: 20;");
        infoCard.setAlignment(Pos.CENTER_LEFT);

        VBox locations = new VBox(10);
        locations.getChildren().addAll(
            new Label("📍 " + pickup),
            new Label("📍 " + dropoff)
        );

        VBox stats = new VBox(5);
        stats.getChildren().addAll(
            new Label("🕒 10:00 AM"),
            new Label("🚗 " + vehicle),
            new Label("Total Distance: 2 km"),
            new Label("Total Fare: Rs 40")
        );
        
        cancelBtn.setStyle("-fx-text-fill: red; -fx-background-color: transparent; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        infoCard.getChildren().addAll(locations, stats, spacer, cancelBtn);

        // --- 2. Bottom Section (Driver Card + Map) ---
        HBox bottomSection = new HBox(30);
        
        // Driver Details Card (White)
        VBox driverCard = new VBox(15);
        driverCard.setPadding(new Insets(20));
        driverCard.setPrefWidth(350);
        driverCard.setStyle("-fx-background-color: white; -fx-background-radius: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 10);");
        driverCard.setAlignment(Pos.CENTER);

        Label driverHeader = new Label("Driver's Details");
        driverHeader.setStyle("-fx-text-fill: gray;");
        
        // Driver Image Placeholder
        ImageView driverImg = new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/3135/3135715.png"));
        driverImg.setFitHeight(100);
        driverImg.setFitWidth(100);

        Label driverName = new Label("Sara Maharjan");
        driverName.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        Label carDetails = new Label("Maruti Suzuki Dzire\nABC-1234\nPhone: 8274029");
        Label rating = new Label("⭐ 4.5");

        driverCard.getChildren().addAll(driverHeader, driverImg, driverName, carDetails, rating);

        // Map Image
        ImageView mapImg = new ImageView();
        try {
            // Replace with your actual local map image path
            mapImg.setImage(new Image(getClass().getResourceAsStream("/images/map.png")));
        } catch (Exception e) {
            System.out.println("Map image not found, using placeholder.");
        }
        mapImg.setFitWidth(400);
        mapImg.setPreserveRatio(true);

        bottomSection.getChildren().addAll(driverCard, mapImg);

        // Add everything to the page
        this.getChildren().addAll(infoCard, bottomSection);
    }
}