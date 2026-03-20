package application;
 
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.*;
import model.*;
 
public class Main extends Application {
 
    private Stage primaryStage;
 
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
 
        try {
            //Pages Initialized
 
            // Authentication
            LoginPage        loginPage        = new LoginPage();
            RegistrationPage registrationPage = new RegistrationPage();
 
            // Passenger pages
            DashboardPage  dashboardPage  = new DashboardPage();
            BookRidePage   bookRidePage   = new BookRidePage();
            MyRidesPage    myRidesPage    = new MyRidesPage();
            MyProfilePage  myProfilePage  = new MyProfilePage();
 
            // Driver pages
            DriverDashboardPage    driverDashboardPage    = new DriverDashboardPage();
            DriverRideRequestsPage driverRideRequestsPage = new DriverRideRequestsPage();
            DriverMyRidesPage      driverMyRidesPage      = new DriverMyRidesPage();
            DriverMyProfilePage    driverMyProfilePage    = new DriverMyProfilePage();
 
            // Admin pages
            AdminDashboardPage   adminDashboardPage   = new AdminDashboardPage();
            AdminManageUsersPage adminManageUsersPage = new AdminManageUsersPage();
            AdminAllRidesPage    adminAllRidesPage    = new AdminAllRidesPage();
            AdminReportsPage     adminReportsPage     = new AdminReportsPage();
 
            // Models initialized
            LogIn        loginModel = new LogIn();
            Registration regModel   = new Registration();
 
            // Scenes created
            Scene loginScene     = new Scene(loginPage,        1100, 700);
            Scene registerScene  = new Scene(registrationPage, 1100, 700);
 
            // Passenger scenes
            Scene dashboardScene  = new Scene(dashboardPage,  1100, 700);
            Scene bookRideScene   = new Scene(bookRidePage,   1100, 700);
            Scene myRidesScene    = new Scene(myRidesPage,    1100, 700);
            Scene myProfileScene  = new Scene(myProfilePage,  1100, 700);
 
            // Driver scenes
            Scene driverDashboardScene    = new Scene(driverDashboardPage,    1100, 700);
            Scene driverRideRequestsScene = new Scene(driverRideRequestsPage, 1100, 700);
            Scene driverMyRidesScene      = new Scene(driverMyRidesPage,      1100, 700);
            Scene driverMyProfileScene    = new Scene(driverMyProfilePage,    1100, 700);
 
            // Admin scenes
            Scene adminDashboardScene   = new Scene(adminDashboardPage,   1100, 700);
            Scene adminManageUsersScene = new Scene(adminManageUsersPage, 1100, 700);
            Scene adminAllRidesScene    = new Scene(adminAllRidesPage,    1100, 700);
            Scene adminReportsScene     = new Scene(adminReportsPage,     1100, 700);
 
            
            String css = getClass().getResource("style.css").toExternalForm();
            loginScene.getStylesheets().add(css);
            registerScene.getStylesheets().add(css);
            dashboardScene.getStylesheets().add(css);
            bookRideScene.getStylesheets().add(css);
            myRidesScene.getStylesheets().add(css);
            myProfileScene.getStylesheets().add(css);
            driverDashboardScene.getStylesheets().add(css);
            driverRideRequestsScene.getStylesheets().add(css);
            driverMyRidesScene.getStylesheets().add(css);
            driverMyProfileScene.getStylesheets().add(css);
            adminDashboardScene.getStylesheets().add(css);
            adminManageUsersScene.getStylesheets().add(css);
            adminAllRidesScene.getStylesheets().add(css);
            adminReportsScene.getStylesheets().add(css);
 
            //Navigation Logic
 
            // ── Authentication
            loginPage.createAccountBtn.setOnAction(e ->
                primaryStage.setScene(registerScene));
 
            loginPage.loginBtn.setOnAction(e -> {
                String email = loginPage.emailField.getText().trim();
                String pass  = loginPage.passwordField.getText().trim();
                String role  = loginModel.loginAndGetRole(email, pass);
 
                if (role != null) {
                    if (role.equalsIgnoreCase("Driver")) {
                        //Driver Login
                        driverDashboardPage.refreshUserName();
                        driverRideRequestsPage.refreshUserName();
                        driverMyRidesPage.refreshUserName();
                        driverMyProfilePage.refreshUserName();
                        primaryStage.setScene(driverDashboardScene);
                        driverDashboardPage.refreshDashboard();
                        primaryStage.setTitle("MakeIt Taxi - Driver Dashboard");
 
                    } else if (role.equalsIgnoreCase("Admin")) {
                        //Admin Login
                        adminDashboardPage.refreshUserName();
                        adminManageUsersPage.refreshUserName();
                        adminAllRidesPage.refreshUserName();
                        adminReportsPage.refreshUserName();
                        primaryStage.setScene(adminDashboardScene);
                        adminDashboardPage.refreshDashboard();
                        primaryStage.setTitle("MakeIt Taxi - Admin Dashboard");
 
                    } else {
                        //Passenger Login
                        dashboardPage.refreshUserName();
                        bookRidePage.refreshUserName();
                        myRidesPage.refreshUserName();
                        myProfilePage.refreshUserName();
                        primaryStage.setScene(dashboardScene);
                        dashboardPage.refreshHistory(); // load real ride history
                        primaryStage.setTitle("MakeIt Taxi - Dashboard");
                    }
                } else {
                    loginPage.messageLabel.setText("Invalid Email or Password");
                    loginPage.messageLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            });
 
            //Registration
            new controller.RegistrationController(registrationPage, regModel);
            registrationPage.backToLoginBtn.setOnAction(e ->
                primaryStage.setScene(loginScene));
 
            //Passenger Navigation
            dashboardPage.bookRideBtn.setOnAction(e ->
                primaryStage.setScene(bookRideScene));
            dashboardPage.myRidesBtn.setOnAction(e ->
                primaryStage.setScene(myRidesScene));
            dashboardPage.myProfileBtn.setOnAction(e -> {
                myProfilePage.loadUserData();
                primaryStage.setScene(myProfileScene);
                primaryStage.setTitle("MakeIt Taxi - My Profile");
            });
            dashboardPage.logoutBtn.setOnAction(e -> {
                UserSession.logout();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("MakeIt Taxi");
            });
 
            //Track the active ride ID so Cancel can update the DB
            final int[] activeRideId = {-1};
            RideBooking rideBookingModel = new RideBooking();
 
            bookRidePage.confirmBtn.setOnAction(e -> {
                String userPickup   = bookRidePage.getPickupLocation();
                String userDropoff  = bookRidePage.getDropoffLocation();
                String userVehicle  = bookRidePage.getSelectedVehicleType();
                double userDistance = bookRidePage.getDistance();
 
                //Validation
                if (userPickup.length() < 3) {
                    showAlert("Missing Pickup",
                        "Please enter a valid pickup location.");
                    return;
                }
                if (userDropoff.length() < 3) {
                    showAlert("Missing Destination",
                        "Please enter a valid destination.");
                    return;
                }
                if (userVehicle.equals("None")) {
                    showAlert("No Vehicle Selected",
                        "Please select a vehicle type before confirming.");
                    return;
                }
                if (userDistance <= 0) {
                    showAlert("Missing Distance",
                        "Please enter the distance in km before confirming.");
                    return;
                }
 
                //Fare Calculation
                double ratePerKm;
                switch (userVehicle) {
                    case "Mini":     ratePerKm = 15; break;
                    case "Sedan":    ratePerKm = 20; break;
                    case "Suzuki":   ratePerKm = 26; break;
                    case "Mercedes": ratePerKm = 32; break;
                    default:         ratePerKm = 20;
                }
                double fare = ratePerKm * userDistance;
 
                //Rides saved to the database
                UserSession session = UserSession.getInstance();
                String passengerEmail = (session != null) ? session.getEmail()   : "";
                String passengerName  = (session != null) ? session.getFullName() : "Guest";
 
                int rideId = rideBookingModel.bookRide(
                    passengerEmail, passengerName,
                    userPickup, userDropoff,
                    userVehicle, userDistance, fare);
 
                if (rideId == -1) {
                    showAlert("Booking Failed",
                        "Could not save your ride. Please try again.");
                    return;
                }
 
                //Storing ride ID
                activeRideId[0] = rideId;
 
                //Ride Details UI
                myRidesPage.showRideDetails(
                    userPickup, userDropoff, userVehicle, userDistance);
                primaryStage.setScene(myRidesScene);
                primaryStage.setTitle("MakeIt Taxi - My Rides");
 
                // ── Start polling for driver assignment ──
                // Updates the driver card once a driver accepts the ride
                myRidesPage.refreshDriverDetails(passengerEmail);
 
                // ── Cancel button — cancels in DB then goes to dashboard ──
                myRidesPage.cancelBtn.setOnAction(ev -> {
                    if (activeRideId[0] != -1) {
                        boolean cancelled = rideBookingModel.cancelRide(activeRideId[0]);
                        if (cancelled) {
                            System.out.println("Ride " + activeRideId[0] + " cancelled in DB.");
                        } else {
                            System.out.println("Could not cancel ride " + activeRideId[0]);
                        }
                        activeRideId[0] = -1; // reset
                    }
                    primaryStage.setScene(dashboardScene);
                    dashboardPage.refreshHistory(); // reload history after cancel
                    primaryStage.setTitle("MakeIt Taxi - Dashboard");
                });
            });
 
            bookRidePage.dashboardBtn.setOnAction(e -> {
                primaryStage.setScene(dashboardScene);
                dashboardPage.refreshHistory();
            });
            bookRidePage.myRidesBtn.setOnAction(e ->
                primaryStage.setScene(myRidesScene));
            bookRidePage.myProfileBtn.setOnAction(e -> {
                myProfilePage.loadUserData();
                primaryStage.setScene(myProfileScene);
                primaryStage.setTitle("MakeIt Taxi - My Profile");
            });
            bookRidePage.logoutBtn.setOnAction(e -> {
                UserSession.logout();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("MakeIt Taxi");
            });
 
            myRidesPage.dashboardBtn.setOnAction(e -> {
                primaryStage.setScene(dashboardScene);
                dashboardPage.refreshHistory();
            });
            myRidesPage.bookRideBtn.setOnAction(e ->
                primaryStage.setScene(bookRideScene));
            myRidesPage.myProfileBtn.setOnAction(e -> {
                myProfilePage.loadUserData();
                primaryStage.setScene(myProfileScene);
                primaryStage.setTitle("MakeIt Taxi - My Profile");
            });
            myRidesPage.logoutBtn.setOnAction(e -> {
                UserSession.logout();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("MakeIt Taxi");
            });
 
            myProfilePage.dashboardBtn.setOnAction(e -> {
                primaryStage.setScene(dashboardScene);
                dashboardPage.refreshHistory();
            });
            myProfilePage.bookRideBtn.setOnAction(e ->
                primaryStage.setScene(bookRideScene));
            myProfilePage.myRidesBtn.setOnAction(e ->
                primaryStage.setScene(myRidesScene));
            myProfilePage.logoutBtn.setOnAction(e -> {
                UserSession.logout();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("MakeIt Taxi");
            });
            myProfilePage.saveBtn.setOnAction(e -> {
                String firstName = myProfilePage.firstNameField.getText().trim();
                String lastName  = myProfilePage.lastNameField.getText().trim();
                String address   = myProfilePage.addressField.getText().trim();
                String phone     = myProfilePage.phoneField.getText().trim();
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    myProfilePage.messageLabel.setText("Name cannot be empty.");
                    myProfilePage.messageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    return;
                }
                if (UserSession.getInstance() != null)
                    UserSession.getInstance().updateProfile(firstName, lastName, address, phone);
                dashboardPage.refreshUserName();
                bookRidePage.refreshUserName();
                myRidesPage.refreshUserName();
                myProfilePage.refreshUserName();
                myProfilePage.messageLabel.setText("Profile updated successfully!");
                myProfilePage.messageLabel.setTextFill(javafx.scene.paint.Color.web("#2E7D32"));
            });
 
            // ════════════════════════════════════════════════════════════
            //  DRIVER navigation
            // ════════════════════════════════════════════════════════════
 
            // ── Helper: navigate to driver dashboard and refresh everything ──
            Runnable goDriverDash = () -> {
                primaryStage.setScene(driverDashboardScene);
                driverDashboardPage.refreshDashboard();
                primaryStage.setTitle("MakeIt Taxi - Driver Dashboard");
            };
 
            // ── Accept ride callback — fired after driver accepts a request ──
            // Goes to dashboard so driver sees the active ride immediately
            driverRideRequestsPage.onAccepted = () -> {
                driverRideRequestsPage.stopAutoRefresh();
                goDriverDash.run();
            };
 
            // ── Complete Ride button ──────────────────────────────────────────
            driverDashboardPage.completeRideBtn.setOnAction(e -> {
                UserSession session = UserSession.getInstance();
                if (session == null) return;
 
                driverDashboardPage.completeRideBtn.setDisable(true);
                driverDashboardPage.completeRideBtn.setText("Completing…");
 
                new Thread(() -> {
                    // Get active ride ID from a fresh DB query
                    model.DriverStats stats =
                        new model.DriverStats(session.getEmail());
                    stats.load();
 
                    boolean ok = false;
                    if (stats.hasActiveRide()) {
                        model.DriverRideService svc =
                            new model.DriverRideService(session.getEmail());
                        ok = svc.completeRide(stats.getActiveRide().id);
                    }
 
                    final boolean success = ok;
                    javafx.application.Platform.runLater(() -> {
                        driverDashboardPage.completeRideBtn.setText("Complete Ride");
                        if (success) {
                            // Refresh dashboard — active ride should now be gone,
                            // earnings should have increased
                            driverDashboardPage.refreshDashboard();
                        } else {
                            driverDashboardPage.completeRideBtn.setDisable(false);
                            javafx.scene.control.Alert alert =
                                new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.WARNING);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Could not complete ride. Please try again.");
                            alert.showAndWait();
                        }
                    });
                }).start();
            });
 
            // ── Driver Dashboard sidebar ──────────────────────────────────────
            driverDashboardPage.rideRequestsBtn.setOnAction(e -> {
                primaryStage.setScene(driverRideRequestsScene);
                driverRideRequestsPage.refreshRequests();
                driverRideRequestsPage.startAutoRefresh(); // new requests appear every 5s
                primaryStage.setTitle("MakeIt Taxi - Ride Requests");
            });
            driverDashboardPage.myRidesBtn.setOnAction(e -> {
                driverRideRequestsPage.stopAutoRefresh();
                primaryStage.setScene(driverMyRidesScene);
                driverMyRidesPage.refreshRides();
                primaryStage.setTitle("MakeIt Taxi - My Rides");
            });
            driverDashboardPage.myProfileBtn.setOnAction(e -> {
                driverRideRequestsPage.stopAutoRefresh();
                driverMyProfilePage.loadUserData();
                primaryStage.setScene(driverMyProfileScene);
                primaryStage.setTitle("MakeIt Taxi - My Profile");
            });
            driverDashboardPage.logoutBtn.setOnAction(e -> {
                driverRideRequestsPage.stopAutoRefresh();
                UserSession.logout();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("MakeIt Taxi");
            });
 
            // ── Driver Ride Requests sidebar ──────────────────────────────────
            driverRideRequestsPage.dashboardBtn.setOnAction(e -> {
                driverRideRequestsPage.stopAutoRefresh();
                goDriverDash.run();
            });
            driverRideRequestsPage.myRidesBtn.setOnAction(e -> {
                driverRideRequestsPage.stopAutoRefresh();
                primaryStage.setScene(driverMyRidesScene);
                driverMyRidesPage.refreshRides();
            });
            driverRideRequestsPage.myProfileBtn.setOnAction(e -> {
                driverRideRequestsPage.stopAutoRefresh();
                driverMyProfilePage.loadUserData();
                primaryStage.setScene(driverMyProfileScene);
                primaryStage.setTitle("MakeIt Taxi - My Profile");
            });
            driverRideRequestsPage.logoutBtn.setOnAction(e -> {
                driverRideRequestsPage.stopAutoRefresh();
                UserSession.logout();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("MakeIt Taxi");
            });
 
            // ── Driver My Rides sidebar ───────────────────────────────────────
            driverMyRidesPage.dashboardBtn.setOnAction(e -> goDriverDash.run());
            driverMyRidesPage.rideRequestsBtn.setOnAction(e -> {
                primaryStage.setScene(driverRideRequestsScene);
                driverRideRequestsPage.refreshRequests();
                driverRideRequestsPage.startAutoRefresh();
            });
            driverMyRidesPage.myProfileBtn.setOnAction(e -> {
                driverMyProfilePage.loadUserData();
                primaryStage.setScene(driverMyProfileScene);
                primaryStage.setTitle("MakeIt Taxi - My Profile");
            });
            driverMyRidesPage.logoutBtn.setOnAction(e -> {
                UserSession.logout();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("MakeIt Taxi");
            });
 
            // Driver My Profile
            driverMyProfilePage.dashboardBtn.setOnAction(e -> {
                primaryStage.setScene(driverDashboardScene);
                driverDashboardPage.refreshDashboard();
            });
            driverMyProfilePage.rideRequestsBtn.setOnAction(e ->
                primaryStage.setScene(driverRideRequestsScene));
            driverMyProfilePage.myRidesBtn.setOnAction(e ->
                primaryStage.setScene(driverMyRidesScene));
            driverMyProfilePage.logoutBtn.setOnAction(e -> {
                UserSession.logout();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("MakeIt Taxi");
            });
            driverMyProfilePage.saveBtn.setOnAction(e -> {
                String firstName = driverMyProfilePage.firstNameField.getText().trim();
                String lastName  = driverMyProfilePage.lastNameField.getText().trim();
                String address   = driverMyProfilePage.addressField.getText().trim();
                String phone     = driverMyProfilePage.phoneField.getText().trim();
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    driverMyProfilePage.messageLabel.setText("Name cannot be empty.");
                    driverMyProfilePage.messageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    return;
                }
                if (UserSession.getInstance() != null)
                    UserSession.getInstance().updateProfile(firstName, lastName, address, phone);
                driverDashboardPage.refreshUserName();
                driverRideRequestsPage.refreshUserName();
                driverMyRidesPage.refreshUserName();
                driverMyProfilePage.refreshUserName();
                driverMyProfilePage.messageLabel.setText("Profile updated successfully!");
                driverMyProfilePage.messageLabel.setTextFill(
                    javafx.scene.paint.Color.web("#2E7D32"));
            });
 
            // ════════════════════════════════════════════════════════════
            //  ADMIN navigation
            // ════════════════════════════════════════════════════════════
 
            // Helper: navigate to admin dashboard and refresh stats
            Runnable goAdminDash = () -> {
                primaryStage.setScene(adminDashboardScene);
                adminDashboardPage.refreshDashboard();
                primaryStage.setTitle("MakeIt Taxi - Admin Dashboard");
            };
 
            // Admin Dashboard
            adminDashboardPage.manageUsersBtn.setOnAction(e -> {
                primaryStage.setScene(adminManageUsersScene);
                adminManageUsersPage.refreshUsers();
                primaryStage.setTitle("MakeIt Taxi - Manage Users");
            });
            adminDashboardPage.allRidesBtn.setOnAction(e -> {
                primaryStage.setScene(adminAllRidesScene);
                adminAllRidesPage.refreshRides();
                primaryStage.setTitle("MakeIt Taxi - All Rides");
            });
            adminDashboardPage.reportsBtn.setOnAction(e -> {
                primaryStage.setScene(adminReportsScene);
                adminReportsPage.refreshReport();
                primaryStage.setTitle("MakeIt Taxi - Reports");
            });
            adminDashboardPage.myProfileBtn.setOnAction(e ->
                primaryStage.setScene(adminDashboardScene));
            adminDashboardPage.logoutBtn.setOnAction(e -> {
                UserSession.logout();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("MakeIt Taxi");
            });
 
            // Admin Manage Users
            adminManageUsersPage.dashboardBtn.setOnAction(e -> goAdminDash.run());
            adminManageUsersPage.allRidesBtn.setOnAction(e -> {
                primaryStage.setScene(adminAllRidesScene);
                adminAllRidesPage.refreshRides();
                primaryStage.setTitle("MakeIt Taxi - All Rides");
            });
            adminManageUsersPage.reportsBtn.setOnAction(e -> {
                primaryStage.setScene(adminReportsScene);
                adminReportsPage.refreshReport();
                primaryStage.setTitle("MakeIt Taxi - Reports");
            });
            adminManageUsersPage.myProfileBtn.setOnAction(e ->
                primaryStage.setScene(adminManageUsersScene));
            adminManageUsersPage.logoutBtn.setOnAction(e -> {
                UserSession.logout();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("MakeIt Taxi");
            });
 
            // Admin All Rides
            adminAllRidesPage.dashboardBtn.setOnAction(e -> goAdminDash.run());
            adminAllRidesPage.manageUsersBtn.setOnAction(e -> {
                primaryStage.setScene(adminManageUsersScene);
                adminManageUsersPage.refreshUsers();
                primaryStage.setTitle("MakeIt Taxi - Manage Users");
            });
            adminAllRidesPage.reportsBtn.setOnAction(e -> {
                primaryStage.setScene(adminReportsScene);
                adminReportsPage.refreshReport();
                primaryStage.setTitle("MakeIt Taxi - Reports");
            });
            adminAllRidesPage.myProfileBtn.setOnAction(e ->
                primaryStage.setScene(adminAllRidesScene));
            adminAllRidesPage.logoutBtn.setOnAction(e -> {
                UserSession.logout();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("MakeIt Taxi");
            });
 
            // Admin Reports
            adminReportsPage.dashboardBtn.setOnAction(e -> goAdminDash.run());
            adminReportsPage.manageUsersBtn.setOnAction(e -> {
                primaryStage.setScene(adminManageUsersScene);
                adminManageUsersPage.refreshUsers();
                primaryStage.setTitle("MakeIt Taxi - Manage Users");
            });
            adminReportsPage.allRidesBtn.setOnAction(e -> {
                primaryStage.setScene(adminAllRidesScene);
                adminAllRidesPage.refreshRides();
                primaryStage.setTitle("MakeIt Taxi - All Rides");
            });
            adminReportsPage.myProfileBtn.setOnAction(e ->
                primaryStage.setScene(adminReportsScene));
            adminReportsPage.logoutBtn.setOnAction(e -> {
                UserSession.logout();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("MakeIt Taxi");
            });
 
            // ── 6. FINAL STAGE SETUP ──────────────────────────────────────
            primaryStage.setTitle("MakeIt Taxi");
            primaryStage.setScene(loginScene);
            primaryStage.setResizable(false);
            primaryStage.show();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}