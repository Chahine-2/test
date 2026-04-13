import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;

public class AppUi extends Application {
    private static final String APP_BG = "-fx-background-color: linear-gradient(to bottom right, #eef2ff, #f8fafc);";
    private static final String AUTH_CARD = "-fx-background-color: white; -fx-background-radius: 28; -fx-border-radius: 28; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.20), 36, 0.18, 0, 14);";
    private static final String INPUT_STYLE = "-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 14; -fx-background-radius: 14; -fx-font-size: 15px; -fx-text-fill: #0f172a; -fx-padding: 0 16 0 16;";
    private static final String PRIMARY_STYLE = "-fx-background-color: linear-gradient(to right, #2563eb, #9333ea); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 14; -fx-cursor: hand;";
    private static final String GHOST_STYLE = "-fx-background-color: transparent; -fx-text-fill: #2563eb; -fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 0;";
    private static final String SIDEBAR_STYLE = "-fx-background-color: linear-gradient(to bottom, #0f172a, #111827);";
    private static final String PANEL_STYLE = "-fx-background-color: white; -fx-background-radius: 24; -fx-border-radius: 24; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 26, 0.12, 0, 10);";

    private final AuthService authService = new AuthService();
    private final AuthView authView = new AuthView(
            authService,
            APP_BG,
            AUTH_CARD,
            INPUT_STYLE,
            PRIMARY_STYLE,
            GHOST_STYLE
    );
    private final AiChatService aiChatService = new AiChatService();
    private final DashboardView dashboardView = new DashboardView(
            SIDEBAR_STYLE,
            PANEL_STYLE,
            this::showDashboard,
            () -> showLogin(null, false),
            (user, avatarPath) -> authService.updateAvatarPath(user.getUsername(), avatarPath)
    );
    private final DashboardChatOverlay chatOverlay = new DashboardChatOverlay(
            aiChatService,
            INPUT_STYLE,
            PRIMARY_STYLE,
            PANEL_STYLE,
            this::displayFirstName
    );

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("EduCore");
        applyWindowedMode();
        showLogin(null, false);
    }

    private void showLogin(String message, boolean success) {
        setScene(authView.buildLogin(
                message,
                success,
                user -> showDashboard(user, "Dashboard"),
                this::showSignup,
                this::showLogin
        ));
    }

    private void showSignup() {
        setScene(authView.buildSignup(
                () -> showLogin(null, false),
                this::showLogin
        ));
    }

    private void showDashboard(User user, String section) {
        List<String> sections = dashboardView.allowedSections(user.getRole());
        String active = "Dashboard".equals(section) || sections.contains(section) ? section : sections.get(0);

        BorderPane shell = dashboardView.buildShell(user, active);
        StackPane root = new StackPane(shell, chatOverlay.build(user, active));
        root.setStyle("-fx-background-color: #e5eefc;");

        primaryStage.setScene(new Scene(root));
        applyWindowedMode();
        primaryStage.setTitle("EduCore - " + user.getRole() + " Dashboard");
        primaryStage.show();
    }

    private void setScene(Parent root) {
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String displayName(User user) {
        return user.getEmail() != null && !user.getEmail().isBlank() ? user.getEmail() : user.getUsername();
    }

    private String displayFirstName(User user) {
        String source = user.getUsername();
        if (source == null || source.isBlank()) {
            source = displayName(user);
        }
        String clean = source.contains("@") ? source.substring(0, source.indexOf('@')) : source;
        clean = clean.replace('.', ' ').replace('_', ' ').trim();
        if (clean.isBlank()) {
            return "Student";
        }
        String[] parts = clean.split("\\s+");
        String first = parts[0];
        return first.substring(0, 1).toUpperCase() + first.substring(1).toLowerCase();
    }

    private void applyWindowedMode() {
        primaryStage.setFullScreen(false);
        primaryStage.setMaximized(false);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(820);
        primaryStage.centerOnScreen();
    }
}
