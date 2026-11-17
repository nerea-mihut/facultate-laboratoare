package salon.service;

import salon.db.DatabaseConnection;
import salon.ui.model.AppointmentItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    public List<AppointmentItem> loadTechnicianAppointments(
            int technicianId
    ) {
        String sql = """
                SELECT
                    p.id_programare,
                    p.data,
                    p.ora,
                    p.stare,
                    p.motiv_anulare,
                    c.nume,
                    c.prenume,
                    c.telefon,
                    c.email,
                    c.alergii,
                    c.detalii_alergii,
                    COALESCE(SUM(ps.durata_aplicata), 0) AS durata_totala,
                    COALESCE(SUM(ps.pret_aplicat), 0) AS pret_total
                FROM programare p
                JOIN client c
                    ON c.id_client = p.id_client
                LEFT JOIN programare_serviciu ps
                    ON ps.id_programare = p.id_programare
                WHERE p.id_tehnician = ?
                GROUP BY
                    p.id_programare,
                    p.data,
                    p.ora,
                    p.stare,
                    p.motiv_anulare,
                    c.nume,
                    c.prenume,
                    c.telefon,
                    c.email,
                    c.alergii,
                    c.detalii_alergii
                ORDER BY p.data DESC, p.ora DESC
                """;

        List<AppointmentItem> result = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, technicianId);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(
                            new AppointmentItem(
                                    rows.getInt("id_programare"),
                                    rows.getDate("data").toLocalDate(),
                                    rows.getTime("ora").toLocalTime(),
                                    rows.getString("stare"),
                                    rows.getString("motiv_anulare"),
                                    rows.getString("nume"),
                                    rows.getString("prenume"),
                                    rows.getString("telefon"),
                                    rows.getString("email"),
                                    rows.getString("alergii"),
                                    rows.getString("detalii_alergii"),
                                    rows.getInt("durata_totala"),
                                    rows.getBigDecimal("pret_total")
                            )
                    );
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return result;
    }

    public void updateStatus(
            int appointmentId,
            int technicianId,
            String newStatus,
            String cancellationReason
    ) throws SQLException {

        String sql = """
                UPDATE programare
                SET stare = ?, motiv_anulare = ?
                WHERE id_programare = ?
                  AND id_tehnician = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, newStatus);
            statement.setString(2, cancellationReason);
            statement.setInt(3, appointmentId);
            statement.setInt(4, technicianId);

            statement.executeUpdate();
        }
    }
}