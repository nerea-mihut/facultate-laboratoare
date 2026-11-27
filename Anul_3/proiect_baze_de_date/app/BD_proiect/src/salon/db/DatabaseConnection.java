package salon.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final Properties FILE = load();

    private static Properties load() {
        Properties properties = new Properties();
        Path path = Path.of("db.properties");

        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                properties.load(in);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        return properties;
    }

    private static String value(
            String envName,
            String propertyName,
            String defaultValue
    ) {
        String value = System.getenv(envName);

        if (value == null || value.isBlank()) {
            value = FILE.getProperty(propertyName);
        }

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    public static Connection getConnection() throws SQLException {
        String host = value("DB_HOST", "db.host", "localhost");
        String port = value("DB_PORT", "db.port", "3306");
        String name = value("DB_NAME", "db.name", "proiect_salon");

        String user = value("DB_USER", "db.user", "root");
        String password = value("DB_PASSWORD", "db.password", "");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + name;

        return DriverManager.getConnection(url, user, password);
    }
}