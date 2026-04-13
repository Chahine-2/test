import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DashboardChatOverlay {
    private final AiChatService aiChatService;
    private final String inputStyle;
    private final String primaryStyle;
    private final String panelStyle;
    private final Function<User, String> firstNameResolver;

    public DashboardChatOverlay(
            AiChatService aiChatService,
            String inputStyle,
            String primaryStyle,
            String panelStyle,
            Function<User, String> firstNameResolver
    ) {
        this.aiChatService = aiChatService;
        this.inputStyle = inputStyle;
        this.primaryStyle = primaryStyle;
        this.panelStyle = panelStyle;
        this.firstNameResolver = firstNameResolver;
    }

    public Node build(User user, String section) {
        VBox chatPanel = buildChatPanel(user, section);
        chatPanel.setVisible(false);
        chatPanel.setManaged(false);

        Button chatButton = new Button("\uD83D\uDCAC");
        chatButton.setPrefSize(64, 64);
        chatButton.setMinSize(64, 64);
        chatButton.setMaxSize(64, 64);
        chatButton.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #9333ea); -fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.30), 20, 0.18, 0, 8);");

        VBox dock = new VBox(12, chatPanel, chatButton);
        dock.setAlignment(Pos.BOTTOM_RIGHT);
        dock.setFillWidth(false);
        dock.setMaxWidth(360);
        dock.setPickOnBounds(false);

        final double[] pressSceneX = new double[1];
        final double[] pressSceneY = new double[1];
        final double[] startTranslateX = new double[1];
        final double[] startTranslateY = new double[1];

        chatButton.setOnMousePressed(e -> {
            pressSceneX[0] = e.getSceneX();
            pressSceneY[0] = e.getSceneY();
            startTranslateX[0] = dock.getTranslateX();
            startTranslateY[0] = dock.getTranslateY();
        });

        chatButton.setOnMouseDragged(e -> {
            dock.setTranslateX(startTranslateX[0] + (e.getSceneX() - pressSceneX[0]));
            dock.setTranslateY(startTranslateY[0] + (e.getSceneY() - pressSceneY[0]));
        });

        chatButton.setOnMouseClicked(e -> {
            if (!e.isStillSincePress()) {
                return;
            }

            boolean show = !chatPanel.isVisible();
            chatPanel.setVisible(show);
            chatPanel.setManaged(show);
            if (show) {
                chatPanel.toFront();
                chatButton.toFront();
            }
        });

        StackPane overlay = new StackPane(dock);
        overlay.setPickOnBounds(false);
        StackPane.setAlignment(dock, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(dock, new Insets(0, 24, 24, 24));
        return overlay;
    }

    private VBox buildChatPanel(User user, String section) {
        Label title = new Label("AI Chatbot Assistant");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label subtitle = new Label("Talk to me about anything: questions, ideas, study help, hobbies, or everyday life.");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        subtitle.setWrapText(true);

        Label mode = new Label(aiChatService.getModeLabel());
        mode.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (aiChatService.isConfigured() ? "#16a34a" : "#d97706") + "; -fx-font-weight: bold;");
        mode.setWrapText(true);

        TextArea transcript = new TextArea();
        transcript.setEditable(false);
        transcript.setWrapText(true);
        transcript.setPrefRowCount(8);
        transcript.setPrefWidth(320);
        transcript.setMaxWidth(Double.MAX_VALUE);
        transcript.setStyle("-fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: #dbe4f0; -fx-border-width: 1; -fx-font-size: 13px; -fx-text-fill: #0f172a;");
        transcript.setText(chatWelcomeMessage(user, section));

        List<ChatMessage> conversation = new ArrayList<>();

        TextField input = new TextField();
        input.setPromptText("Type a message...");
        input.setPrefHeight(46);
        input.setStyle(inputStyle);

        Button send = new Button("Send");
        final Button[] sendRef = {send};

        Runnable sendMessage = () -> {
            String message = input.getText().trim();
            if (message.isBlank()) {
                return;
            }

            appendChatMessage(transcript, "You", message);
            input.clear();
            sendRef[0].setDisable(true);
            mode.setText(aiChatService.isConfigured() ? "AI is thinking..." : aiChatService.getModeLabel());

            aiChatService.replyAsync(conversation, message).whenComplete((reply, error) -> Platform.runLater(() -> {
                String answer = error == null ? reply : "Sorry, I had trouble generating a reply. " + error.getMessage();
                appendChatMessage(transcript, "AI", answer);
                mode.setText(aiChatService.getModeLabel());
                sendRef[0].setDisable(false);
            }));
        };

        send.setPrefHeight(46);
        send.setStyle(primaryStyle);
        send.setOnAction(e -> sendMessage.run());
        input.setOnAction(e -> sendMessage.run());

        HBox quickActions = new HBox(8,
                chatShortcut("Hello", input, sendMessage, "Hello, how are you?"),
                chatShortcut("Advice", input, sendMessage, "Give me advice for today"),
                chatShortcut("Fun", input, sendMessage, "Tell me something interesting")
        );

        HBox inputRow = new HBox(8, input, send);
        HBox.setHgrow(input, Priority.ALWAYS);

        VBox panel = new VBox(12,
                title,
                subtitle,
                mode,
                transcript,
                quickActions,
                inputRow
        );
        panel.setPadding(new Insets(18));
        panel.setPrefWidth(360);
        panel.setMaxWidth(360);
        panel.setStyle(panelStyle);
        return panel;
    }

    private Button chatShortcut(String text, TextField input, Runnable sendMessage, String preset) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #3730a3; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-cursor: hand;");
        button.setOnAction(e -> {
            input.setText(preset);
            sendMessage.run();
        });
        return button;
    }

    private void appendChatMessage(TextArea transcript, String sender, String message) {
        if (!transcript.getText().isBlank()) {
            transcript.appendText("\n\n");
        }
        transcript.appendText(sender + ": " + message);
        transcript.positionCaret(transcript.getText().length());
    }

    private String chatWelcomeMessage(User user, String section) {
        return "Hello " + firstNameResolver.apply(user) + ", I am your chatbot assistant.\n" +
                "You are currently viewing: " + section + "\n" +
                "I can talk with you about almost anything.\n\n" +
                "Try saying hello, asking a question, sharing an idea, or telling me what you need help with.";
    }
}

