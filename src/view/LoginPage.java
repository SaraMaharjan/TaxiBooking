package view;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoginPage extends HBox {

    public TextField emailField = new TextField();
    public PasswordField passwordField = new PasswordField();
    public Button loginBtn = new Button("Log in");
    public Button createAccountBtn = new Button("Create an account");
    public Label messageLabel = new Label(""); 

    public LoginPage() {
        // Updated to match your 1100x700 stage size
        this.setPrefSize(1100, 700);
        this.setStyle("-fx-background-color: #FDF9E7;"); 
        initView();
    }

    private void initView() {
        // --- Left Side: Branding (Yellow Side) ---
        VBox branding = new VBox(); 
        branding.setAlignment(Pos.CENTER);
        branding.setPrefWidth(550); // Equal 50/50 split
        branding.getStyleClass().add("branding-side");

        try {
            Image logo = new Image(getClass().getResourceAsStream("taxilogo.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(400); 
            logoView.setPreserveRatio(true);
            branding.getChildren().add(logoView);
        } catch (Exception e) {
            System.out.println("Login logo error: " + e.getMessage());
            branding.getChildren().add(new Label("MAKEIT TAXI"));
        }

        // --- Right Side: Login Form (The Gap-Killer Section) ---
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER_LEFT);
        // Using wide padding (100px on sides) to keep the fields thin like the screenshot
        form.setPadding(new Insets(50, 100, 50, 100)); 
        form.getStyleClass().add("form-side");

        // THE FIX: Tell the form to take all remaining space
        HBox.setHgrow(form, Priority.ALWAYS);
        form.setMaxWidth(Double.MAX_VALUE);
        form.setStyle("-fx-background-color: #FDF9E7;"); // Seamless yellow background

        Label title = new Label("Log In");
        title.getStyleClass().add("form-title");

        // Email Section
        Label emailLabel = new Label("Email");
        emailLabel.getStyleClass().add("input-label");
        emailField.setPromptText("Enter your email");
        emailField.getStyleClass().add("text-input-field");
        emailField.setMaxWidth(350); // Limits width to keep it crisp

        // Password Section
        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("input-label");
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("text-input-field");
        passwordField.setMaxWidth(350);

        // Buttons
        loginBtn.setMaxWidth(350);
        loginBtn.getStyleClass().add("primary-button");

        createAccountBtn.setMaxWidth(350);
        createAccountBtn.getStyleClass().add("secondary-button");

        // Assemble all components into the form
        form.getChildren().addAll(title, emailLabel, emailField, passLabel, passwordField, messageLabel, loginBtn, createAccountBtn);

        // Add both halves to this HBox
        this.getChildren().addAll(branding, form);
    }
}