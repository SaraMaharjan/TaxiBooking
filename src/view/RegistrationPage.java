package view;



import javafx.scene.layout.HBox;

import javafx.scene.layout.Priority;

import javafx.scene.layout.VBox;

import javafx.scene.control.*;

import javafx.geometry.Pos;

import javafx.geometry.Insets;

import javafx.scene.image.Image;

import javafx.scene.image.ImageView;

import javafx.collections.FXCollections;





public class RegistrationPage extends HBox {



public TextField nameField = new TextField();

public TextField emailField = new TextField();

public PasswordField passwordField = new PasswordField();


// New ComboBox for Role selection

public ComboBox<String> roleComboBox = new ComboBox<>(

FXCollections.observableArrayList("Passenger", "Driver")

);


public Button registerBtn = new Button("Sign Up");

public Button backToLoginBtn = new Button("Back to Login");

public Label messageLabel = new Label("");



public RegistrationPage() {

this.setPrefSize(1100, 700);

this.setStyle("-fx-background-color: #FDF9E7;");

initView();


}




private void initView() {

// --- Left Side: Branding ---

VBox branding = new VBox();

branding.setAlignment(Pos.CENTER);

branding.setPrefWidth(550);

branding.getStyleClass().add("branding-side");



try {

Image logo = new Image(getClass().getResourceAsStream("taxilogo.png"));

ImageView logoView = new ImageView(logo);

logoView.setFitWidth(400);

logoView.setPreserveRatio(true);

branding.getChildren().add(logoView);

} catch (Exception e) {

branding.getChildren().addAll(new Label("MAKEIT"));

}



// --- Right Side: Registration Form ---

VBox form = new VBox(15);

form.setAlignment(Pos.CENTER_LEFT);

form.setPrefWidth(550);

form.setPadding(new Insets(30, 100, 30, 100)); // Adjusted vertical padding for new field

form.getStyleClass().add("form-side");


HBox.setHgrow(form, Priority.ALWAYS);

form.setMaxWidth(Double.MAX_VALUE);

form.setStyle("-fx-background-color: #FDF9E7;");



Label title = new Label("Create Account");

title.getStyleClass().add("form-title");



// Styling the input labels and fields

setupField(nameField, "Enter your full name");

setupField(emailField, "Enter your email");

setupField(passwordField, "Enter your password");


// --- Role Field Setup ---

roleComboBox.setPromptText("Select your role");

roleComboBox.setMaxWidth(350);

roleComboBox.getStyleClass().add("text-input-field"); // Keeps styling consistent with text fields



// Buttons

registerBtn.setMaxWidth(350);

registerBtn.getStyleClass().add("primary-button");


backToLoginBtn.setMaxWidth(350);

backToLoginBtn.getStyleClass().add("secondary-button");



// Add everything to the form (including the new Role section)

form.getChildren().addAll(

title,

createLabel("Full Name"), nameField,

createLabel("Email"), emailField,

createLabel("Password"), passwordField,

createLabel("Role"), roleComboBox, // New field added here

messageLabel,

registerBtn,

backToLoginBtn

);


this.getChildren().addAll(branding, form);

}



private void setupField(TextField field, String prompt) {

field.setPromptText(prompt);

field.getStyleClass().add("text-input-field");

field.setMaxWidth(350);

}



private Label createLabel(String text) {

Label l = new Label(text);

l.getStyleClass().add("input-label");

return l;

}

}