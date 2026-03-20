package view;
 
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import model.UserSession;
 
/**
 * MyProfilePage — Passenger profile editor.
 * Extends BaseAppPage for sidebar behaviour.
 */
public class MyProfilePage extends BaseAppPage {
 
    // Declared null — assigned in buildContent() before use
    // (super() calls buildContent() before child field initialisers run)
    public TextField firstNameField;
    public TextField lastNameField;
    public TextField addressField;
    public TextField phoneField;
    public Button    saveBtn;
    public Button    discardBtn;
    public Label     messageLabel;
 
    public MyProfilePage() {
        super();        // buildContent() runs here, fields are assigned inside it
        loadUserData(); // safe to call now — fields exist
    }
 
    @Override protected String getActiveNavButton() { return "My Profile"; }
    @Override protected Button[] getNavButtons() {
        return new Button[]{ dashboardBtn, bookRideBtn, myRidesBtn, myProfileBtn };
    }
 
    public void loadUserData() {
        UserSession s = UserSession.getInstance();
        if (s == null) return;
        String full = s.getFullName() != null ? s.getFullName().trim() : "";
        int idx = full.indexOf(' ');
        firstNameField.setText(idx >= 0 ? full.substring(0, idx) : full);
        lastNameField.setText(idx >= 0 ? full.substring(idx + 1) : "");
        try { addressField.setText(s.getAddress()); } catch (Exception ignored) {}
        try { phoneField.setText(s.getPhone());     } catch (Exception ignored) {}
    }
 
    @Override
    protected VBox buildContent() {
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #FDF9E7;");
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(50, 70, 50, 55));
        VBox.setVgrow(root, Priority.ALWAYS);
 
        HBox centreRow = new HBox(55);
        centreRow.setAlignment(Pos.TOP_LEFT);
        centreRow.getChildren().addAll(buildAvatarCol(), buildFormCol());
        root.getChildren().add(centreRow);
        return root;
    }
 
    private VBox buildAvatarCol() {
        VBox col = new VBox(14);
        col.setAlignment(Pos.TOP_CENTER);
        col.setPrefWidth(170); col.setMinWidth(170);
 
        StackPane frame = new StackPane();
        frame.setPrefSize(135, 135);
        frame.setStyle("-fx-background-color: transparent;");
 
        javafx.scene.shape.Circle whiteBg = new javafx.scene.shape.Circle(67.5);
        whiteBg.setFill(javafx.scene.paint.Color.WHITE);
        whiteBg.setEffect(new javafx.scene.effect.DropShadow(
            12, 0, 3, javafx.scene.paint.Color.rgb(0, 0, 0, 0.10)));
        frame.getChildren().add(whiteBg);
 
        try {
            Image img = new Image(getClass().getResourceAsStream("userProfileIcon.png"));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(125); iv.setFitHeight(125);
            iv.setClip(new Circle(62.5, 62.5, 62.5));
            frame.getChildren().add(iv);
        } catch (Exception ignored) {
            Label fb = new Label("👤");
            fb.setStyle("-fx-font-size: 52px;");
            frame.getChildren().add(fb);
        }
 
        UserSession s = UserSession.getInstance();
        String role = (s != null && s.getRole() != null) ? s.getRole() : "Passenger";
        Label roleLabel = new Label(role);
        roleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #666;");
        Label rating = new Label("⭐  4.5");
        rating.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #E6B833;");
 
        col.getChildren().addAll(frame, roleLabel, rating);
        return col;
    }
 
    private VBox buildFormCol() {
        // Initialise all public fields here — safe because buildContent() calls this
        firstNameField = new TextField();
        lastNameField  = new TextField();
        addressField   = new TextField();
        phoneField     = new TextField();
        saveBtn        = new Button("Save Changes");
        discardBtn     = new Button("Discard Changes");
        messageLabel   = new Label("");
 
        VBox col = new VBox(0);
        col.setAlignment(Pos.TOP_LEFT);
 
        Label heading = new Label("Personal Information");
        heading.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: 800; " +
            "-fx-text-fill: #333; -fx-padding: 0 0 20px 0;");
 
        for (TextField tf : new TextField[]{
                firstNameField, lastNameField, addressField, phoneField}) {
            tf.getStyleClass().add("profile-input");
            tf.setPrefWidth(310); tf.setMaxWidth(310); tf.setPrefHeight(46);
        }
        phoneField.textProperty().addListener((obs, old, nw) -> {
            if (!nw.matches("\\d*")) phoneField.setText(old);
        });
 
        VBox fields = new VBox(16);
        fields.getChildren().addAll(
            fieldGroup("First Name",   firstNameField),
            fieldGroup("Last Name",    lastNameField),
            fieldGroup("Address",      addressField),
            fieldGroup("Phone number", phoneField)
        );
 
        messageLabel.setMinHeight(18); messageLabel.setMaxWidth(640);
        messageLabel.getStyleClass().add("message-label");
        VBox.setMargin(messageLabel, new Insets(8, 0, 0, 0));
 
        HBox btnRow = new HBox(16);
        btnRow.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(btnRow, new Insets(22, 0, 0, 0));
 
        discardBtn.setPrefWidth(190); discardBtn.setPrefHeight(46);
        discardBtn.getStyleClass().add("secondary-button");
        discardBtn.setOnAction(e -> { loadUserData(); messageLabel.setText(""); });
 
        saveBtn.setPrefWidth(190); saveBtn.setPrefHeight(46);
        saveBtn.getStyleClass().add("primary-button");
 
        btnRow.getChildren().addAll(discardBtn, saveBtn);
        col.getChildren().addAll(heading, fields, messageLabel, btnRow);
        return col;
    }
 
    private VBox fieldGroup(String text, TextField field) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #444;");
        return new VBox(7, lbl, field);
    }
}