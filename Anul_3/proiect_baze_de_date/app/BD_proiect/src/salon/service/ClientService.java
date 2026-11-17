package salon.service;

import salon.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientService {

    public record ActivityTable(
            String[] columns,
            List<Object[]> rows
    ) {
    }

    public ActivityTable loadAppointments(
            String userEmail
    ) throws SQLException {

        String sql = """
                SELECT
                    p.data,
                    p.ora,
                    CONCAT(
                        a.prenume,
                        ' ',
                        a.nume
                    ) AS tehnician,
                    (
                        SELECT GROUP_CONCAT(
                            CONCAT(
                                s.denumire,
                                CASE
                                    WHEN ps.detalii IS NULL
                                         OR ps.detalii = ''
                                    THEN ''
                                    ELSE CONCAT(
                                        ' · ',
                                        ps.detalii
                                    )
                                END
                            )
                            ORDER BY s.id_serviciu
                            SEPARATOR ', '
                        )
                        FROM programare_serviciu ps
                        JOIN serviciu s
                            ON s.id_serviciu = ps.id_serviciu
                        WHERE ps.id_programare = p.id_programare
                    ) AS servicii,
                    (
                        SELECT COALESCE(
                            SUM(ps.pret_aplicat),
                            0
                        )
                        FROM programare_serviciu ps
                        WHERE ps.id_programare = p.id_programare
                    ) AS total,
                    p.stare,
                    p.motiv_anulare
                FROM programare p
                JOIN utilizator u
                    ON u.id_client = p.id_client
                JOIN angajat a
                    ON a.id_angajat = p.id_tehnician
                WHERE u.email = ?
                ORDER BY
                    p.data DESC,
                    p.ora DESC,
                    p.id_programare DESC
                """;

        return query(
                sql,
                userEmail,
                new String[]{
                        "Data",
                        "Ora",
                        "Tehnician",
                        "Servicii",
                        "Total (lei)",
                        "Stare",
                        "Motiv anulare"
                }
        );
    }

    public ActivityTable loadEnrollments(
            String userEmail
    ) throws SQLException {

        String sql = """
                SELECT
                    c.denumire AS curs,
                    COALESCE(
                        i.data_cursului,
                        s.data_sesiune
                    ) AS data_cursului,
                    CONCAT(
                        a.prenume,
                        ' ',
                        a.nume
                    ) AS trainer,
                    i.data_inscrierii,
                    i.stare,
                    i.pret_aplicat,
                    i.metoda_plata,
                    s.stare AS stare_sesiune
                FROM inscriere i
                JOIN utilizator u
                    ON u.id_client = i.id_client
                JOIN sesiune_curs s
                    ON s.id_sesiune = i.id_sesiune
                JOIN curs c
                    ON c.id_curs = s.id_curs
                JOIN angajat a
                    ON a.id_angajat = s.id_trainer
                WHERE u.email = ?
                ORDER BY
                    i.data_inscrierii DESC,
                    i.id_inscriere DESC
                """;

        return query(
                sql,
                userEmail,
                new String[]{
                        "Curs",
                        "Data cursului",
                        "Trainer",
                        "Data înscrierii",
                        "Stare înscriere",
                        "Preț (lei)",
                        "Opțiune plată",
                        "Stare sesiune"
                }
        );
    }

    public ActivityTable loadWaitingList(
            String userEmail
    ) throws SQLException {

        String sql = """
                SELECT
                    c.denumire AS curs,
                    s.data_sesiune,
                    CONCAT(
                        a.prenume,
                        ' ',
                        a.nume
                    ) AS trainer,
                    la.data_adaugarii,
                    s.stare,
                    GREATEST(
                        CAST(
                            s.nr_maxim_cursanti AS SIGNED
                        ) - CAST(
                            (
                                SELECT COUNT(*)
                                FROM inscriere i
                                WHERE i.id_sesiune = s.id_sesiune
                                  AND i.stare NOT IN (
                                      'Retrasa',
                                      'Inlocuita'
                                  )
                            ) AS SIGNED
                        ),
                        0
                    ) AS locuri_disponibile
                FROM lista_asteptare la
                JOIN utilizator u
                    ON u.id_client = la.id_client
                JOIN sesiune_curs s
                    ON s.id_sesiune = la.id_sesiune
                JOIN curs c
                    ON c.id_curs = s.id_curs
                JOIN angajat a
                    ON a.id_angajat = s.id_trainer
                WHERE u.email = ?
                ORDER BY la.data_adaugarii DESC
                """;

        return query(
                sql,
                userEmail,
                new String[]{
                        "Curs",
                        "Data sesiunii",
                        "Trainer",
                        "Adăugat la",
                        "Stare sesiune",
                        "Locuri neocupate"
                }
        );
    }

    private ActivityTable query(
            String sql,
            String userEmail,
            String[] columns
    ) throws SQLException {

        List<Object[]> rows = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, userEmail);

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                while (result.next()) {
                    Object[] row =
                            new Object[columns.length];

                    for (
                            int index = 0;
                            index < columns.length;
                            index++
                    ) {
                        row[index] =
                                result.getObject(index + 1);
                    }

                    rows.add(row);
                }
            }
        }

        return new ActivityTable(
                columns,
                rows
        );
    }
}