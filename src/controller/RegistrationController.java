package controller;
 
import javafx.scene.paint.Color;
import model.Registration;
import view.RegistrationPage;
 
/**
 * RegistrationController — Controller layer for user sign-up.
 * Reads all fields from RegistrationPage and passes them to Registration model.
 */
public class RegistrationController {
 
    private final RegistrationPage view;
    private final Registration     model;
 
    public RegistrationController(RegistrationPage view, Registration model) {
        this.view  = view;
        this.model = model;
        this.view.registerBtn.setOnAction(e -> handleRegistration());
    }
 
    private void handleRegistration() {
        // Read all fields from the view
        String fullName = view.nameField.getText().trim();
        String email    = view.emailField.getText().trim();
        String password = view.passwordField.getText().trim();
        String role     = view.roleComboBox.getValue();
 
        // ── Validation ──────────────────────────────────────────────
        if (fullName.isEmpty()) {
            view.messageLabel.setText("Please enter your full name.");
            view.messageLabel.setTextFill(Color.RED);
            return;
        }
        if (email.isEmpty()) {
            view.messageLabel.setText("Please enter your email.");
            view.messageLabel.setTextFill(Color.RED);
            return;
        }
        if (password.isEmpty()) {
            view.messageLabel.setText("Please enter a password.");
            view.messageLabel.setTextFill(Color.RED);
            return;
        }
        if (role == null || role.isEmpty()) {
            view.messageLabel.setText("Please select a role.");
            view.messageLabel.setTextFill(Color.RED);
            return;
        }
 
        // ── Save to DB ───────────────────────────────────────────────
        boolean success = model.registerUser(fullName, email, password, role);
 
        if (success) {
            view.messageLabel.setText("Account created! You can now log in.");
            view.messageLabel.setTextFill(Color.web("#2E7D32")); // green
            // Clear fields after successful registration
            view.nameField.clear();
            view.emailField.clear();
            view.passwordField.clear();
            view.roleComboBox.setValue(null);
        } else {
            view.messageLabel.setText("Registration failed. Email may already exist.");
            view.messageLabel.setTextFill(Color.RED);
        }
    }
}