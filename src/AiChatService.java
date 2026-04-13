import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiChatService {
    private static final String SYSTEM_PROMPT = "You are a friendly, helpful AI chatbot inside a desktop app. " +
            "Chat naturally about anything the user wants: questions, ideas, school, hobbies, advice, jokes, planning, and daily life. " +
            "Ask short follow-up questions when helpful. Keep replies concise unless the user asks for detail. " +
            "Do not mention that you are a rule-based bot. If you do not know something, say so honestly and offer to help in another way.";

    private static final Pattern CONTENT_PATTERN = Pattern.compile("\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"", Pattern.DOTALL);

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private final String apiKey = System.getenv("OPENAI_API_KEY");
    private final String baseUrl = normalizeBaseUrl(env("OPENAI_BASE_URL", "https://api.openai.com"));
    private final String model = env("OPENAI_MODEL", "gpt-4o-mini");

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String getModeLabel() {
        return isConfigured()
                ? "AI mode: " + model + " (online)"
                : "Local mode: set OPENAI_API_KEY to enable real AI";
    }

    public CompletableFuture<String> replyAsync(List<ChatMessage> conversation, String userMessage) {
        Objects.requireNonNull(conversation, "conversation");
        Objects.requireNonNull(userMessage, "userMessage");

        synchronized (conversation) {
            conversation.add(new ChatMessage("user", userMessage));
        }

        if (!isConfigured()) {
            String reply = localReply(userMessage, conversation);
            synchronized (conversation) {
                conversation.add(new ChatMessage("assistant", reply));
            }
            return CompletableFuture.completedFuture(reply);
        }

        List<ChatMessage> snapshot;
        synchronized (conversation) {
            snapshot = new ArrayList<>(conversation);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String reply = requestOpenAiReply(snapshot);
                synchronized (conversation) {
                    conversation.add(new ChatMessage("assistant", reply));
                }
                return reply;
            } catch (Exception ex) {
                String fallback = localReply(userMessage, conversation);
                synchronized (conversation) {
                    conversation.add(new ChatMessage("assistant", fallback));
                }
                return fallback;
            }
        });
    }

    private String requestOpenAiReply(List<ChatMessage> conversation) throws IOException, InterruptedException {
        String body = buildRequestBody(conversation);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(chatCompletionsUrl()))
                .timeout(Duration.ofSeconds(40))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("AI request failed with HTTP " + response.statusCode() + ": " + response.body());
        }

        String content = extractAssistantContent(response.body());
        if (content == null || content.isBlank()) {
            throw new IOException("AI response did not contain assistant content.");
        }
        return content.trim();
    }

    private String buildRequestBody(List<ChatMessage> conversation) {
        StringBuilder json = new StringBuilder();
        json.append('{');
        json.append("\"model\":\"").append(escapeJson(model)).append("\",");
        json.append("\"temperature\":0.7,");
        json.append("\"messages\":[");
        json.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(SYSTEM_PROMPT)).append("\"}");

        int start = Math.max(0, conversation.size() - 20);
        for (int i = start; i < conversation.size(); i++) {
            ChatMessage message = conversation.get(i);
            if (message == null || message.role() == null || message.content() == null) {
                continue;
            }
            json.append(',');
            json.append('{')
                    .append("\"role\":\"").append(escapeJson(message.role())).append("\",")
                    .append("\"content\":\"").append(escapeJson(message.content())).append("\"")
                    .append('}');
        }

        json.append("]}");
        return json.toString();
    }

    private String extractAssistantContent(String responseBody) {
        Matcher matcher = CONTENT_PATTERN.matcher(responseBody);
        if (matcher.find()) {
            return unescapeJsonString(matcher.group(1));
        }
        return null;
    }

    private String chatCompletionsUrl() {
        if (baseUrl.endsWith("/v1")) {
            return baseUrl + "/chat/completions";
        }
        return baseUrl + "/v1/chat/completions";
    }

    private String normalizeBaseUrl(String value) {
        String trimmed = value == null ? "https://api.openai.com" : value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String localReply(String userMessage, List<ChatMessage> conversation) {
        String lower = userMessage.toLowerCase();

        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Hi! I’m here and ready to chat about anything you want.";
        }
        if (lower.contains("how are you")) {
            return "I’m doing well and ready to help. How are you today?";
        }
        if (lower.contains("your name") || lower.contains("who are you")) {
            return "I’m your AI chatbot assistant.";
        }
        if (lower.contains("joke")) {
            return "Why did the computer get cold? It left its Windows open.";
        }
        if (lower.contains("study") || lower.contains("learn")) {
            return "A simple study flow is: review, practice, and repeat. If you want, I can help you make a study plan.";
        }
        if (lower.contains("idea") || lower.contains("project")) {
            return "Tell me more about your idea and I’ll help you shape it into something useful.";
        }
        if (lower.contains("advice") || lower.contains("help")) {
            return "I can give you advice, explain a topic, brainstorm ideas, or just keep chatting with you.";
        }
        if (lower.endsWith("?")) {
            return "That’s a good question. I can help explain it step by step if you want a simpler version.";
        }

        ChatMessage lastUser = lastUserMessage(conversation);
        if (lastUser != null && !lastUser.content().isBlank()) {
            return "I hear you: \"" + lastUser.content() + "\". Tell me a bit more and I’ll keep going with you.";
        }

        return "Tell me more and I’ll keep the conversation going.";
    }

    private ChatMessage lastUserMessage(List<ChatMessage> conversation) {
        for (int i = conversation.size() - 1; i >= 0; i--) {
            ChatMessage message = conversation.get(i);
            if (message != null && "user".equalsIgnoreCase(message.role())) {
                return message;
            }
        }
        return null;
    }

    private String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String escapeJson(String value) {
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }

    private String unescapeJsonString(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\' || i + 1 >= value.length()) {
                out.append(c);
                continue;
            }

            char next = value.charAt(++i);
            switch (next) {
                case '"' -> out.append('"');
                case '\\' -> out.append('\\');
                case '/' -> out.append('/');
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'u' -> {
                    if (i + 4 <= value.length() - 1) {
                        String hex = value.substring(i + 1, i + 5);
                        out.append((char) Integer.parseInt(hex, 16));
                        i += 4;
                    }
                }
                default -> out.append(next);
            }
        }
        return out.toString();
    }
}


