package salon.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Apel către un API de chat compatibil cu formatul /chat/completions. */
public final class AiClient {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String ask(String systemPrompt, String userMessage)
            throws IOException, InterruptedException {

        if (!AiConfig.enabled()) {
            throw new IOException("Asistentul AI nu este configurat: lipsește cheia API.");
        }

        String body = "{\"model\":" + MiniJson.quote(AiConfig.model())
                + ",\"messages\":["
                + "{\"role\":\"system\",\"content\":" + MiniJson.quote(systemPrompt) + "},"
                + "{\"role\":\"user\",\"content\":" + MiniJson.quote(userMessage) + "}"
                + "]}";

        HttpRequest request = HttpRequest.newBuilder(URI.create(AiConfig.apiUrl()))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + AiConfig.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new IOException("Serviciul AI a răspuns cu codul " + response.statusCode() + ".");
        }

        try {
            Map<String, Object> root = MiniJson.map(MiniJson.parse(response.body()));
            List<Object> choices = MiniJson.list(root.get("choices"));

            if (choices.isEmpty()) {
                throw new IOException("Serviciul AI a returnat un răspuns gol.");
            }

            Map<String, Object> message =
                    MiniJson.map(MiniJson.map(choices.get(0)).get("message"));

            Object content = message.get("content");

            if (!(content instanceof String)) {
                throw new IOException("Serviciul AI a returnat un format neașteptat.");
            }

            return ((String) content).replaceAll("\\[\\d+\\]", "").trim();

        } catch (IllegalArgumentException exception) {
            throw new IOException("Serviciul AI a returnat un format neașteptat.", exception);
        }
    }
}
