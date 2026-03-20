package controller;





import javafx.scene.Scene;

import javafx.stage.Stage;

import model.LogIn;

import view.LoginPage;



public class LogInController {


    private LoginPage view;

    private LogIn model;

    private Stage stage;           // Reference to the main window

    private Scene dashboardScene;  // Reference to the dashboard screen

	private Scene bookRideScene;



    // Updated constructor to accept 4 parameters
    public LogInController(LoginPage view, LogIn model, Stage stage, Scene dashboardScene, Scene bookRideScene) {
        this.view = view;
        this.model = model;
        this.stage = stage;
        this.dashboardScene = dashboardScene;
        this.bookRideScene = bookRideScene;



        // Set the action for the login button

        this.view.loginBtn.setOnAction(e -> handleLogin());

    }



    private void handleLogin() {

        String email = view.emailField.getText();

        String pass = view.passwordField.getText();



        // Check credentials via the Model

        if (model.login(email, pass)) {

            // Success: Switch the scene to the Dashboard

            stage.setScene(dashboardScene);

            stage.setTitle("MakeIt Taxi - Dashboard");

            stage.centerOnScreen();



        } else {

            // Failure: Show error message

            view.messageLabel.setText("Invalid Email or Password");

            view.messageLabel.setTextFill(javafx.scene.paint.Color.RED);

        }

    }

}