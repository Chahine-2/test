import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AuthView {
    private final AuthService authService;
    private final String appBg;
    private final String authCard;
    private final String inputStyle;
    private final String primaryStyle;
    private final String ghostStyle;

    public AuthView(
            AuthService authService,
            String appBg,
            String authCard,
            String inputStyle,
            String primaryStyle,
            String ghostStyle
    ) {
        this.authService = authService;
        this.appBg = appBg;
        this.authCard = authCard;
        this.inputStyle = inputStyle;
        this.primaryStyle = primaryStyle;
        this.ghostStyle = ghostStyle;
    }

    public Parent buildLogin(
            String message,
            boolean success,
            Consumer<User> onAuthSuccess,
            Runnable onShowSignup,
            BiConsumer<String, Boolean> onShowLogin
    ) {
        Label title = heading("Welcome Back");
        Label subtitle = subheading("Sign in to continue your dashboard");

        TextField loginField = new TextField();
        loginField.setPromptText("Username or email");
        styleField(loginField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleField(passwordField);

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Password");
        styleField(visiblePasswordField);
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        StackPane passwordStack = new StackPane(passwordField, visiblePasswordField);

        CheckBox showPassword = new CheckBox("Show password");
        showPassword.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        showPassword.setOnAction(e -> {
            boolean show = showPassword.isSelected();
            passwordField.setManaged(!show);
            passwordField.setVisible(!show);
            visiblePasswordField.setManaged(show);
            visiblePasswordField.setVisible(show);
        });

        Label banner = banner(message, success);

        Button signIn = new Button("Sign In");
        signIn.setMaxWidth(Double.MAX_VALUE);
        signIn.setPrefHeight(52);
        signIn.setStyle(primaryStyle);
        signIn.setOnAction(e -> authService.authenticate(loginField.getText().trim(), passwordField.getText())
                .ifPresentOrElse(
                        onAuthSuccess,
                        () -> onShowLogin.accept("Invalid username or password", false)
                ));

        Button goSignup = new Button("Create an account");
        goSignup.setStyle(ghostStyle);
        goSignup.setOnAction(e -> onShowSignup.run());

        VBox form = new VBox(12,
                brandHeader(),
                spacer(6),
                title,
                subtitle,
                spacer(6),
                sectionLabel("Login"),
                loginField,
                sectionLabel("Password"),
                passwordStack,
                showPassword,
                banner,
                signIn,
                rowCentered(new Label("New here?"), goSignup)
        );
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(460);
        form.setPadding(new Insets(34, 36, 30, 36));
        form.setStyle(authCard);

        StackPane root = new StackPane(form);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle(appBg);
        return root;
    }

    public Parent buildSignup(Runnable onBackToLogin, BiConsumer<String, Boolean> onShowLogin) {
        Label title = heading("Create Account");
        Label subtitle = subheading("Register and choose a role to continue");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        styleField(usernameField);

        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        styleField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleField(passwordField);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm password");
        styleField(confirmField);

        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(Role.values());
        roleCombo.setPromptText("Select role");
        roleCombo.setPrefHeight(54);
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        roleCombo.setStyle(inputStyle);

        Label banner = banner("", true);
        banner.setVisible(false);
        banner.setManaged(false);

        Button signUp = new Button("Sign Up");
        signUp.setMaxWidth(Double.MAX_VALUE);
        signUp.setPrefHeight(52);
        signUp.setStyle(primaryStyle);
        signUp.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();
            Role role = roleCombo.getValue();

            if (username.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank() || role == null) {
                updateBanner(banner, "Please fill all fields and choose a role.", false);
                return;
            }
            if (!password.equals(confirm)) {
                updateBanner(banner, "Passwords do not match.", false);
                return;
            }

            authService.registerUser(username, email, password, role)
                    .ifPresentOrElse(
                            user -> onShowLogin.accept("Account created successfully. Please sign in.", true),
                            () -> updateBanner(banner, "Username or email already exists.", false)
                    );
        });

        Button back = new Button("Back to login");
        back.setStyle(ghostStyle);
        back.setOnAction(e -> onBackToLogin.run());

        VBox form = new VBox(12,
                brandHeader(),
                spacer(6),
                title,
                subtitle,
                spacer(6),
                sectionLabel("Username"),
                usernameField,
                sectionLabel("Email"),
                emailField,
                sectionLabel("Password"),
                passwordField,
                sectionLabel("Confirm Password"),
                confirmField,
                sectionLabel("Role"),
                roleCombo,
                banner,
                signUp,
                rowCentered(new Label("Already have an account?"), back)
        );
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(500);
        form.setPadding(new Insets(34, 36, 30, 36));
        form.setStyle(authCard);

        StackPane root = new StackPane(form);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle(appBg);
        return root;
    }

    private VBox brandHeader() {
        Label name = new Label("EduCore");
        name.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #4f46e5;");
        Label sub = new Label("Learning Platform");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        StackPane logo = new StackPane(new Label("\uD83C\uDF93"));
        logo.setPrefSize(62, 62);
        logo.setStyle("-fx-background-color: linear-gradient(to bottom right, #2563eb, #9333ea); -fx-background-radius: 18;");
        ((Label) logo.getChildren().get(0)).setStyle("-fx-font-size: 24px;");
        HBox row = new HBox(14, logo, new VBox(2, name, sub));
        row.setAlignment(Pos.CENTER_LEFT);
        return new VBox(row);
    }

    private Label heading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        return label;
    }

    private Label subheading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b;");
        return label;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        return label;
    }

    private Region spacer(double height) {
        Region region = new Region();
        region.setMinHeight(height);
        region.setPrefHeight(height);
        return region;
    }

    private HBox rowCentered(Label left, Button right) {
        HBox row = new HBox(4, left, right);
        row.setAlignment(Pos.CENTER);
        left.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        return row;
    }

    private void styleField(TextField field) {
        field.setPrefHeight(52);
        field.setStyle(inputStyle);
    }

    private Label banner(String message, boolean success) {
        Label label = new Label(message == null ? "" : message);
        if (message == null || message.isBlank()) {
            label.setVisible(false);
            label.setManaged(false);
        } else {
            label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + (success ? "#16a34a" : "#dc2626") + ";");
        }
        return label;
    }

    private void updateBanner(Label banner, String message, boolean success) {
        banner.setText(message);
        banner.setVisible(true);
        banner.setManaged(true);
        banner.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + (success ? "#16a34a" : "#dc2626") + ";");
    }
}

