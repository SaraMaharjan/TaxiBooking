package view;
 
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.RideBooking;
import model.UserSession;
 
/**
 * DashboardPage — Passenger home screen.
 * Extends BaseAppPage for sidebar, nav, and avatar via inheritance.
 *
 * Call refreshHistory() each time this scene is shown so the
 * ride history reflects the passenger's actual completed rides.
 */
public class DashboardPage extends BaseAppPage {
 
    private VBox historyList;
 
    public DashboardPage() { super(); }
 
    @Override protected String getActiveNavButton() { return "Dashboard"; }
    @Override protected Button[] getNavButtons() {
        return new Button[]{ dashboardBtn, bookRideBtn, myRidesBtn, myProfileBtn };
    }
 
    // ── Call from Main.java whenever this scene is shown ─────────────
    public void refreshHistory() {
        UserSession session = UserSession.getInstance();
        if (session == null || historyList == null) return;
 
        new Thread(() -> {
            RideBooking model = new RideBooking();
            java.util.List<RideBooking.RideHistoryRow> rows =
                model.getRideHistory(session.getEmail());
 
            Platform.runLater(() -> {
                historyList.getChildren().clear();
 
                if (rows.isEmpty()) {
                    Label empty = new Label("No ride history yet. Book your first ride!");
                    empty.setStyle(
                        "-fx-font-size: 14px; -fx-text-fill: #AAAAAA; " +
                        "-fx-font-weight: 600; -fx-padding: 10px 0;");
                    historyList.getChildren().add(empty);
                } else {
                    for (RideBooking.RideHistoryRow row : rows) {
                        historyList.getChildren().add(historyCard(row));
                    }
                }
            });
        }).start();
    }
 
    @Override
    protected VBox buildContent() {
        // Initialise historyList here — safe after super()
        historyList = new VBox(14);
 
        VBox contentArea = new VBox(28);
        contentArea.setPadding(new Insets(50));
        contentArea.setMaxWidth(Double.MAX_VALUE);
        contentArea.setStyle("-fx-background-color: white;");
 
        // ── Search bar ────────────────────────────────────────────────
        HBox searchBar = new HBox(15);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setMaxWidth(600);
        searchBar.setPadding(new Insets(12, 15, 12, 20));
        searchBar.getStyleClass().add("search-container");
 
        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 18px; -fx-text-fill: #888;");
 
        TextField searchField = new TextField();
        searchField.setPromptText("Where to?");
        searchField.setStyle("-fx-background-color: transparent; -fx-font-size: 16px;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
 
        ImageView searchTaxiIcon = new ImageView();
        try {
            Image taxiImg = new Image(getClass().getResourceAsStream("halfTaxi.png"));
            searchTaxiIcon.setImage(taxiImg);
            searchTaxiIcon.setFitHeight(35);
            searchTaxiIcon.setPreserveRatio(true);
            searchTaxiIcon.setSmooth(true);
        } catch (Exception ignored) {}
 
        searchBar.getChildren().addAll(searchIcon, searchField, searchTaxiIcon);
 
        // ── History section ───────────────────────────────────────────
        Label historyTitle = new Label("History");
        historyTitle.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #555;");
 
        // Initial placeholder while loading
        Label loading = new Label("Loading history…");
        loading.setStyle("-fx-font-size: 13px; -fx-text-fill: #AAAAAA;");
        historyList.getChildren().add(loading);
 
        ScrollPane scroll = new ScrollPane(historyList);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background-color: transparent; -fx-background: transparent; " +
            "-fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
 
        VBox historySection = new VBox(14, historyTitle, scroll);
        VBox.setVgrow(historySection, Priority.ALWAYS);
 
        contentArea.getChildren().addAll(searchBar, historySection);
        return contentArea;
    }
 
    // ── Build one history card from a RideHistoryRow ──────────────────
    private HBox historyCard(RideBooking.RideHistoryRow row) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 20, 14, 20));
        card.getStyleClass().add("history-card");
 
        // Vehicle icon
        ImageView logo = new ImageView();
        try {
            Image img = new Image(getClass().getResourceAsStream(row.getIconFile()));
            logo.setImage(img);
            logo.setFitWidth(55);
            logo.setPreserveRatio(true);
            logo.setSmooth(true);
        } catch (Exception ignored) {
            // icon not found — leave blank
        }
 
        // Text info
        VBox info = new VBox(5);
 
        // Destination as the title
        Label destLabel = new Label(row.dropoff);
        destLabel.setStyle(
            "-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #444;");
 
        // Date, time, fare on one row
        HBox metaRow = new HBox(16);
        metaRow.setAlignment(Pos.CENTER_LEFT);
 
        Label dateLbl = new Label("📅 " + row.rideDate);
        dateLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
 
        Label timeLbl = new Label("🕒 " + row.rideTime);
        timeLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
 
        Label fareLbl = new Label(row.getFareStr());
        fareLbl.setStyle("-fx-text-fill: #555; -fx-font-size: 12px; -fx-font-weight: 700;");
 
        metaRow.getChildren().addAll(dateLbl, timeLbl, fareLbl);
        info.getChildren().addAll(destLabel, metaRow);
 
        // Status badge — only show Cancelled in red, completed rides show nothing
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
 
        HBox cardContent = new HBox(15);
        cardContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cardContent, Priority.ALWAYS);
        cardContent.getChildren().addAll(logo, info, spacer);
 
        if ("cancelled".equalsIgnoreCase(row.status)) {
            Label badge = new Label("Cancelled");
            badge.setStyle(
                "-fx-background-color: #FFEBEE; -fx-background-radius: 10px; " +
                "-fx-text-fill: #C62828; -fx-font-size: 11px; " +
                "-fx-font-weight: 700; -fx-padding: 4px 10px;");
            cardContent.getChildren().add(badge);
        }
 
        card.getChildren().add(cardContent);
        return card;
    }
}