package salon.ai;

import salon.db.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

/** Descrie sesiunile planificate, pentru sugestii de curs și trainer. */
public final class CourseAdvisor {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public String describeUpcomingSessions() throws SQLException {
        String sql = """
                SELECT
                    c.denumire,
                    c.nivel,
                    s.pret,
                    s.nr_maxim_cursanti,
                    CONCAT(a.prenume, ' ', a.nume) AS trainer,
                    DATE(COALESCE(
                        s.data_sesiune,
                        (SELECT MIN(x.inceput)
                         FROM interval_sesiune x
                         WHERE x.id_sesiune = s.id_sesiune)
                    )) AS data_start,
                    (SELECT COUNT(*)
                     FROM inscriere i
                     WHERE i.id_sesiune = s.id_sesiune
                       AND i.stare NOT IN ('Retrasa', 'Inlocuita')) AS ocupate
                FROM sesiune_curs s
                JOIN curs c
                    ON c.id_curs = s.id_curs
                JOIN trainer t
                    ON t.id_angajat = s.id_trainer
                JOIN angajat a
                    ON a.id_angajat = t.id_angajat
                WHERE s.stare = 'Planificata'
                HAVING data_start IS NULL OR data_start >= CURDATE()
                ORDER BY data_start, c.denumire
                LIMIT 40
                """;

        StringBuilder result = new StringBuilder();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rows = statement.executeQuery()
        ) {
            while (rows.next()) {
                Date start = rows.getDate("data_start");
                int free = Math.max(0, rows.getInt("nr_maxim_cursanti") - rows.getInt("ocupate"));

                BigDecimal price = rows.getBigDecimal("pret");

                result.append("- ")
                        .append(rows.getString("denumire"))
                        .append(" (nivel: ")
                        .append(rows.getString("nivel"))
                        .append("), trainer ")
                        .append(rows.getString("trainer"))
                        .append(", ")
                        .append(start == null
                                ? "data nestabilită"
                                : "începe pe " + start.toLocalDate().format(FORMAT))
                        .append(", preț ")
                        .append(price.stripTrailingZeros().toPlainString())
                        .append(" lei, locuri libere: ")
                        .append(free)
                        .append('\n');
            }
        }

        return result.toString();
    }
}
