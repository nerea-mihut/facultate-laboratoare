package salon.ai;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Citește setările asistentului AI din variabile de mediu sau din fișierul ai.properties
 * (în folderul de lucru al aplicației). Cheia API nu se pune niciodată în cod.
 */
public final class AiConfig {

    private static final Properties FILE = load();

    private AiConfig() {
    }

    private static Properties load() {
        Properties properties = new Properties();
        Path path = Path.of("ai.properties");

        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                properties.load(in);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        return properties;
    }

    private static String value(String envName, String propertyName, String defaultValue) {
        String value = System.getenv(envName);

        if (value == null || value.isBlank()) {
            value = FILE.getProperty(propertyName);
        }

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    public static String apiKey() {
        return value("AI_API_KEY", "api.key", "");
    }

    public static String apiUrl() {
        return value("AI_API_URL", "api.url", "https://api.perplexity.ai/chat/completions");
    }

    public static String model() {
        return value("AI_MODEL", "api.model", "sonar");
    }

    public static boolean enabled() {
        return !apiKey().isEmpty();
    }
}
