package salon.service;

import salon.db.DatabaseConnection;
import salon.ui.model.RegistryRecord;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SterilizationService {

    public List<RegistryRecord> loadRecords(int employeeId) {
        String sql = """
                SELECT
                    id_inregistrare,
                    data_sterilizarii,
                    aparat,
                    material_sterilizat,
                    temperatura,
                    ora_inceperii,
                    ora_terminarii,
                    referinta_test,
                    id_angajat
                FROM registru_sterilizare
                WHERE id_angajat = ?
                ORDER BY data_sterilizarii DESC,
                         ora_inceperii DESC
                """;

        List<RegistryRecord> result = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, employeeId);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(
                            new RegistryRecord(
                                    rows.getInt("id_inregistrare"),
                                    rows.getDate("data_sterilizarii")
                                            .toLocalDate(),
                                    rows.getString("aparat"),
                                    rows.getString(
                                            "material_sterilizat"
                                    ),
                                    rows.getBigDecimal("temperatura"),
                                    rows.getTime("ora_inceperii")
                                            .toLocalTime(),
                                    rows.getTime("ora_terminarii")
                                            .toLocalTime(),
                                    rows.getString("referinta_test"),
                                    rows.getInt("id_angajat")
                            )
                    );
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return result;
    }

    public void saveRecord(
            int employeeId,
            LocalDate date,
            String device,
            String material,
            BigDecimal temperature,
            LocalTime startTime,
            LocalTime endTime,
            String testReference
    ) throws SQLException {

        String sql = """
                INSERT INTO registru_sterilizare
                    (
                        data_sterilizarii,
                        aparat,
                        material_sterilizat,
                        temperatura,
                        ora_inceperii,
                        ora_terminarii,
                        referinta_test,
                        id_angajat
                    )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setDate(1, Date.valueOf(date));
            statement.setString(2, device);
            statement.setString(3, material);
            statement.setBigDecimal(4, temperature);
            statement.setTime(5, Time.valueOf(startTime));
            statement.setTime(6, Time.valueOf(endTime));
            statement.setString(7, testReference);
            statement.setInt(8, employeeId);

            statement.executeUpdate();
        }
    }

    public String saveImage(File imageFile) throws IOException {
        Path imagesFolder = Path.of("registry_images");

        if (!Files.exists(imagesFolder)) {
            Files.createDirectories(imagesFolder);
        }

        String originalName = imageFile.getName();

        String extension = "";

        int dotIndex = originalName.lastIndexOf('.');

        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }

        String fileName =
                "martor_" + System.currentTimeMillis() + extension;

        Path destination = imagesFolder.resolve(fileName);

        Files.copy(
                imageFile.toPath(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );

        return destination.toString();
    }
}