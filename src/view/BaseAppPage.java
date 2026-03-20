package view;
 
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import model.UserSession;
 
/**
 * BaseAppPage — Abstract base class for all authenticated pages.
 *
 * Demonstrates OOP principles:
 *   - ABSTRACTION:   Defines the common page template; subclasses
 *                    provide their own content via buildContent().
 *   - INHERITANCE:   All 8 pages extend this class and inherit
 *                    the sidebar, nav buttons, and avatar logic.
 *   - ENCAPSULATION: Sidebar internals are private; only the public
 *                    nav buttons and refreshUserName() are exposed.
 *   - POLYMORPHISM:  Each subclass overrides buildContent() to
 *                    render its own unique right-hand panel.
 *
 * Usage:
 *   public class DashboardPage extends BaseAppPage {
 *       public DashboardPage() { super(); }
 *
 *       @Override
 *       protected String getActiveNavButton() { return "Dashboard"; }
 *
 *       @Override
 *       protected VBox buildContent() {
 *           // return your page-specific content here
 *       }
 *   }
 */
public abstract class BaseAppPage extends HBox {
 
    //Public nav buttons
    public Button logoutBtn = new Button("Log Out");
 
    //Sidebar username label
    public Label sidebarUserLabel = new Label("Guest");
 
    //Nav button definitions
    // Passenger pages
    public Button dashboardBtn    = new Button("Dashboard");
    public Button bookRideBtn     = new Button("Book a Ride");
    public Button myRidesBtn      = new Button("My Rides");
    public Button myProfileBtn    = new Button("My Profile");
 
    // Driver pages
    public Button rideRequestsBtn = new Button("Ride Requests");
 
    // Admin pages
    public Button manageUsersBtn  = new Button("Manage Users");
    public Button allRidesBtn     = new Button("All Rides");
    public Button reportsBtn      = new Button("Reports");
 
    //  Constructor (builds the full layout once for all subclasses)
  
    public BaseAppPage() {
        this.setPrefSize(1100, 700);
        this.setStyle("-fx-background-color: #FDF9E7;");
 
        VBox sidebar = buildSidebar();
        VBox content = buildContent();
        HBox.setHgrow(content, Priority.ALWAYS);
 
        this.getChildren().addAll(sidebar, content);
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  Abstract methods — subclasses MUST implement these
    // ─────────────────────────────────────────────────────────────────
 
    /**
     * Returns the label text of the nav button that should be
     * highlighted as active for this page.
     * e.g. "Dashboard", "Book a Ride", "My Rides", "My Profile"
     */
    protected abstract String getActiveNavButton();
    protected abstract Button[] getNavButtons();
    protected abstract VBox buildContent();
 
    // ─────────────────────────────────────────────────────────────────
    //  Shared method — updates the username label after login
    // ─────────────────────────────────────────────────────────────────
    public void refreshUserName() {
        if (UserSession.getInstance() != null)
            sidebarUserLabel.setText(UserSession.getInstance().getFullName());
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  SIDEBAR — built once here, never repeated in subclasses
    // ─────────────────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sidebar = new VBox(25);
        sidebar.setMinWidth(280);
        sidebar.setPrefWidth(280);
        sidebar.setMaxWidth(280);
        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.getStyleClass().add("sidebar");
 
        // ── Logo ──
        VBox logoBox = new VBox();
        logoBox.setAlignment(Pos.CENTER);
        try {
            Image logoImg = new Image(
                getClass().getResourceAsStream("taxilogo.png"));
            ImageView logoView = new ImageView(logoImg);
            logoView.setFitWidth(180);
            logoView.setPreserveRatio(true);
            logoBox.getChildren().add(logoView);
        } catch (Exception ignored) {
            Label fb = new Label("🚕 MAKEIT");
            fb.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #444;");
            logoBox.getChildren().add(fb);
        }
 
        // ── Avatar + username ──
        VBox profileBox = new VBox(8);
        profileBox.setAlignment(Pos.CENTER);
 
        // Use Circle shape — avoids the rectangle bug from -fx-background-radius
        javafx.scene.shape.Circle avatarBg = new javafx.scene.shape.Circle(35);
        avatarBg.setFill(javafx.scene.paint.Color.web(getAvatarColor()));
 
        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(70, 70);
        avatarPane.setStyle("-fx-background-color: transparent;");
        avatarPane.getChildren().add(avatarBg);
 
        try {
            Image userImg = new Image(
                getClass().getResourceAsStream("userProfileIcon.png"));
            ImageView userView = new ImageView(userImg);
            userView.setFitWidth(70);
            userView.setFitHeight(70);
            userView.setClip(new Circle(35, 35, 35));
            avatarPane.getChildren().add(userView);
        } catch (Exception ignored) {
            Label fb = new Label("👤");
            fb.setStyle("-fx-font-size: 28px;");
            avatarPane.getChildren().add(fb);
        }
 
        // Populate label from session (refreshUserName() updates it after login)
        String name = (UserSession.getInstance() != null)
                ? UserSession.getInstance().getFullName() : "Guest";
        sidebarUserLabel.setText(name);
        sidebarUserLabel.setStyle(
            "-fx-font-weight: bold; -fx-text-fill: #555; -fx-font-size: 15px;");
        sidebarUserLabel.setWrapText(true);
        sidebarUserLabel.setMaxWidth(200);
        sidebarUserLabel.setAlignment(Pos.CENTER);
 
        profileBox.getChildren().addAll(avatarPane, sidebarUserLabel);
 
        // ── Nav buttons — provided by subclass ──
        VBox navMenu = new VBox(10);
        String activeLabel = getActiveNavButton();
 
        for (Button btn : getNavButtons()) {
            boolean isActive = btn.getText().equals(activeLabel);
            configureNavButton(btn, getIconFor(btn.getText()), getSizeFor(btn.getText()), isActive);
            navMenu.getChildren().add(btn);
        }
 
        // ── Logout ──
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        configureNavButton(logoutBtn, "logOut.png", 50, false);
        logoutBtn.getStyleClass().add("nav-button-logout");
 
        sidebar.getChildren().addAll(logoBox, profileBox, navMenu, spacer, logoutBtn);
        return sidebar;
    }
 
    // ─────────────────────────────────────────────────────────────────
    //  Private helpers — internal to sidebar construction
    // ─────────────────────────────────────────────────────────────────
 
    /** Passenger uses beige avatar; Driver uses yellow. */
    private String getAvatarColor() {
        UserSession s = UserSession.getInstance();
        if (s != null && "Driver".equalsIgnoreCase(s.getRole()))
            return "#F2C94C";
        return "#E8C8A0";
    }
 
    /** Maps button label to its icon filename. */
    private String getIconFor(String label) {
        switch (label) {
            case "Dashboard":     return "dashboardIcon.png";
            case "Book a Ride":   return "carIcon.png";
            case "Ride Requests": return "carIcon.png";
            case "My Rides":      return "clockIcon.png";
            case "My Profile":    return "personIcon.png";
            case "Manage Users":  return "personIcon.png";
            case "All Rides":     return "carIcon.png";
            case "Reports":       return "dashboardIcon.png";
            default:              return "dashboardIcon.png";
        }
    }
 
    /** Maps button label to its icon size. */
    private double getSizeFor(String label) {
        switch (label) {
            case "Dashboard":     return 40;
            case "Book a Ride":
            case "Ride Requests":
            case "All Rides":     return 30;
            case "My Rides":      return 32;
            case "My Profile":
            case "Manage Users":  return 31;
            case "Reports":       return 36;
            default:              return 30;
        }
    }
 
    /** Applies nav-button CSS and icon to a button. */
    private void configureNavButton(Button btn, String iconFile,
                                     double size, boolean active) {
        ImageView icon = loadIcon(iconFile, size);
        if (icon != null) btn.setGraphic(icon);
 
        btn.getStyleClass().add("nav-button");
        if (active) btn.getStyleClass().add("nav-button-active");
 
        btn.setGraphicTextGap(15);
        btn.setPrefHeight(46);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(btn, Priority.NEVER);
    }
 
    /** Safe image loader — returns null on failure rather than crashing. */
    private ImageView loadIcon(String fileName, double size) {
        try {
            Image img = new Image(getClass().getResourceAsStream(fileName));
            ImageView view = new ImageView(img);
            view.setFitWidth(size);
            view.setPreserveRatio(true);
            view.setSmooth(true);
            return view;
        } catch (Exception e) {
            return null;
        }
    }
}