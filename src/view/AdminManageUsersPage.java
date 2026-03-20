package view;
 
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.AdminStats;
 
/**
 * AdminManageUsersPage — Admin view of all registered users.
 * Extends BaseAppPage for sidebar behaviour.
 *
 * Shows every user with name, email, role badge, phone.
 * Includes a Delete button per row (wired to DB via AdminStats).
 * Call refreshUsers() each time this scene is shown.
 */
public class AdminManageUsersPage extends BaseAppPage {
 
    private VBox userTable;
 
    public AdminManageUsersPage() { super(); }
 
    @Override protected String getActiveNavButton() { return "Manage Users"; }
    @Override protected Button[] getNavButtons() {
        return new Button[]{ dashboardBtn, manageUsersBtn, allRidesBtn, reportsBtn, myProfileBtn };
    }
 
    public void refreshUsers() {
        new Thread(() -> {
            AdminStats stats = new AdminStats();
            stats.load();
            Platform.runLater(() -> populateTable(stats));
        }).start();
    }
 
    @Override
    protected VBox buildContent() {
        userTable = new VBox(10);
 
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 35, 30, 35));
        content.setStyle("-fx-background-color: #FDF9E7;");
        HBox.setHgrow(content, Priority.ALWAYS);
 
        // ── Title + subtitle ─────────────────────────────────────────
        Label title = new Label("Manage Users");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: #444;");
        Label subtitle = new Label("View and manage all registered passengers and drivers.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");
 
        // ── Table header ─────────────────────────────────────────────
        HBox header = buildHeader();
 
        // ── Scrollable user list ──────────────────────────────────────
        ScrollPane scroll = new ScrollPane(userTable);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background-color: transparent; -fx-background: transparent; " +
            "-fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
 
        Label loading = new Label("Loading users…");
        loading.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 14px; -fx-padding: 20px;");
        userTable.getChildren().add(loading);
 
        VBox tableSection = new VBox(8, header, scroll);
        VBox.setVgrow(tableSection, Priority.ALWAYS);
 
        content.getChildren().addAll(title, subtitle, tableSection);
        return content;
    }
 
    private HBox buildHeader() {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-background-color: #F2C94C; -fx-background-radius: 10px;");
 
        String[] cols   = { "Name", "Email", "Role", "Phone", "Address", "Action" };
        double[] widths = { 160,     200,     100,    130,     160,        90 };
 
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #FFFFFF;");
            lbl.setPrefWidth(widths[i]);
            row.getChildren().add(lbl);
        }
        return row;
    }
 
    private void populateTable(AdminStats stats) {
        userTable.getChildren().clear();
 
        if (stats.getUsers().isEmpty()) {
            Label empty = new Label("No users found.");
            empty.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 14px; -fx-padding: 16px;");
            userTable.getChildren().add(empty);
            return;
        }
 
        for (AdminStats.UserRow user : stats.getUsers()) {
            HBox row = new HBox();
            row.setPadding(new Insets(12, 16, 12, 16));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");
 
            // Name
            Label nameLbl = cell(user.fullName, 160, "#333", true);
 
            // Email
            Label emailLbl = cell(user.email, 200, "#666", false);
 
            // Role badge
            Label roleLbl = new Label(user.role);
            roleLbl.setPrefWidth(100);
            roleLbl.setStyle(
                "-fx-background-color: " + roleColor(user.role) + "; " +
                "-fx-background-radius: 20px; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-font-weight: 700; " +
                "-fx-padding: 4px 10px; -fx-max-width: 80px;");
 
            // Phone
            Label phoneLbl = cell(
                user.phone != null && !user.phone.isEmpty() ? user.phone : "—",
                130, "#555", false);
 
            // Address
            Label addrLbl = cell(
                user.address != null && !user.address.isEmpty() ? user.address : "—",
                160, "#555", false);
 
            // Delete button
            Button deleteBtn = new Button("Delete");
            deleteBtn.setPrefWidth(80);
            deleteBtn.setStyle(
                "-fx-background-color: #FFEBEE; -fx-background-radius: 8px; " +
                "-fx-text-fill: #E53935; -fx-font-size: 12px; " +
                "-fx-font-weight: 700; -fx-cursor: hand;");
            deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                "-fx-background-color: #E53935; -fx-background-radius: 8px; " +
                "-fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-font-weight: 700; -fx-cursor: hand;"));
            deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
                "-fx-background-color: #FFEBEE; -fx-background-radius: 8px; " +
                "-fx-text-fill: #E53935; -fx-font-size: 12px; " +
                "-fx-font-weight: 700; -fx-cursor: hand;"));
            deleteBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete User");
                confirm.setHeaderText(null);
                confirm.setContentText(
                    "Delete " + user.fullName + "? This cannot be undone.");
                confirm.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        AdminStats s = new AdminStats();
                        if (s.deleteUser(user.id)) {
                            userTable.getChildren().remove(row);
                        }
                    }
                });
            });
 
            row.getChildren().addAll(
                nameLbl, emailLbl, roleLbl, phoneLbl, addrLbl, deleteBtn);
            userTable.getChildren().add(row);
        }
    }
 
    private Label cell(String text, double width,
                        String color, boolean bold) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: " + color + "; " +
            (bold ? "-fx-font-weight: 700;" : ""));
        l.setWrapText(false);
        return l;
    }
 
    private String roleColor(String role) {
        switch (role) {
            case "Driver":    return "#E6B833";
            case "Admin":     return "#9C27B0";
            default:          return "#4A90D9";  // Passenger
        }
    }
}