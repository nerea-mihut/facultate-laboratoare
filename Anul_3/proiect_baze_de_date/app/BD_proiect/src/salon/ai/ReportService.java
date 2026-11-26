package salon.ai;

import salon.db.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/** Rapoarte calculate exclusiv prin interogări SQL parametrizate. */
public final class ReportService {

    public record TechnicianReport(
            int finished,
            int clients,
            BigDecimal revenue,
            int confirmed,
            int cancelled,
            int noShow
    ) {
    }

    public record TrainerReport(
            int sessions,
            int students,
            BigDecimal value
    ) {
    }

    public TechnicianReport technician(int technicianId, LocalDate from, LocalDate to)
            throws SQLException {

        String sql = """
                SELECT
                    COUNT(DISTINCT CASE WHEN p.stare = 'Finalizata'
                        THEN p.id_programare END) AS finalizate,
                    COUNT(DISTINCT CASE WHEN p.stare = 'Finalizata'
                        THEN p.id_client END) AS clienti,
                    COALESCE(SUM(CASE WHEN p.stare = 'Finalizata'
                        THEN ps.pret_aplicat END), 0) AS venit,
                    COUNT(DISTINCT CASE WHEN p.stare = 'Confirmata'
                        THEN p.id_programare END) AS confirmate,
                    COUNT(DISTINCT CASE WHEN p.stare = 'Anulata'
                        THEN p.id_programare END) AS anulate,
                    COUNT(DISTINCT CASE WHEN p.stare = 'Neprezentare'
                        THEN p.id_programare END) AS neprezentari
                FROM programare p
                LEFT JOIN programare_serviciu ps
                    ON ps.id_programare = p.id_programare
                WHERE p.id_tehnician = ?
                  AND p.data BETWEEN ? AND ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, technicianId);
            statement.setDate(2, Date.valueOf(from));
            statement.setDate(3, Date.valueOf(to));

            try (ResultSet rows = statement.executeQuery()) {
                rows.next();

                return new TechnicianReport(
                        rows.getInt("finalizate"),
                        rows.getInt("clienti"),
                        rows.getBigDecimal("venit"),
                        rows.getInt("confirmate"),
                        rows.getInt("anulate"),
                        rows.getInt("neprezentari")
                );
            }
        }
    }

    public TrainerReport trainer(int trainerId, LocalDate from, LocalDate to)
            throws SQLException {

        String startOfSession = """
                DATE(COALESCE(
                    s.data_sesiune,
                    (SELECT MIN(x.inceput)
                     FROM interval_sesiune x
                     WHERE x.id_sesiune = s.id_sesiune)
                ))
                """;

        String sessionsSql = """
                SELECT COUNT(*) AS total
                FROM sesiune_curs s
                WHERE s.id_trainer = ?
                  AND s.stare <> 'Anulata'
                  AND
                """ + startOfSession + " BETWEEN ? AND ?";

        String studentsSql = """
                SELECT
                    COUNT(DISTINCT i.id_client) AS cursanti,
                    COALESCE(SUM(i.pret_aplicat), 0) AS valoare
                FROM inscriere i
                JOIN sesiune_curs s
                    ON s.id_sesiune = i.id_sesiune
                WHERE s.id_trainer = ?
                  AND s.stare <> 'Anulata'
                  AND i.stare NOT IN ('Retrasa', 'Inlocuita')
                  AND DATE(COALESCE(
                        i.data_cursului,
                        s.data_sesiune,
                        (SELECT MIN(x.inceput)
                         FROM interval_sesiune x
                         WHERE x.id_sesiune = s.id_sesiune)
                      )) BETWEEN ? AND ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            int sessions;

            try (PreparedStatement statement = connection.prepareStatement(sessionsSql)) {
                statement.setInt(1, trainerId);
                statement.setDate(2, Date.valueOf(from));
                statement.setDate(3, Date.valueOf(to));

                try (ResultSet rows = statement.executeQuery()) {
                    rows.next();
                    sessions = rows.getInt("total");
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(studentsSql)) {
                statement.setInt(1, trainerId);
                statement.setDate(2, Date.valueOf(from));
                statement.setDate(3, Date.valueOf(to));

                try (ResultSet rows = statement.executeQuery()) {
                    rows.next();

                    return new TrainerReport(
                            sessions,
                            rows.getInt("cursanti"),
                            rows.getBigDecimal("valoare")
                    );
                }
            }
        }
    }
}
