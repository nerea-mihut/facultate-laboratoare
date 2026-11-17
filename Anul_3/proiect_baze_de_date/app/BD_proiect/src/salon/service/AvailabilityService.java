package salon.service;

import salon.db.DatabaseConnection;
import salon.ui.model.TimeInterval;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class AvailabilityService {

    public record StoredInterval(
            int id,
            LocalDateTime start,
            LocalDateTime end,
            String type
    ) {
    }

    public static class AvailabilityException
            extends RuntimeException {

        public AvailabilityException(
                String message,
                Throwable cause
        ) {
            super(message, cause);
        }
    }

    public List<StoredInterval> loadStoredDayIntervals(
            int employeeId,
            LocalDate date
    ) {
        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            return loadStoredIntervals(
                    connection,
                    employeeId,
                    date
            );

        } catch (SQLException exception) {
            throw new AvailabilityException(
                    "Intervalele nu au putut fi încărcate.",
                    exception
            );
        }
    }

    private List<StoredInterval> loadStoredIntervals(
            Connection connection,
            int employeeId,
            LocalDate date
    ) throws SQLException {

        String sql = """
                SELECT
                    id_interval,
                    inceput,
                    sfarsit,
                    tip_interval
                FROM interval_disponibilitate
                WHERE id_angajat = ?
                  AND inceput < ?
                  AND sfarsit > ?
                ORDER BY inceput, id_interval
                """;

        List<StoredInterval> result = new ArrayList<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, employeeId);

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            date.plusDays(1).atStartOfDay()
                    )
            );

            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            date.atStartOfDay()
                    )
            );

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new StoredInterval(
                            rows.getInt("id_interval"),
                            rows.getTimestamp("inceput")
                                    .toLocalDateTime(),
                            rows.getTimestamp("sfarsit")
                                    .toLocalDateTime(),
                            rows.getString("tip_interval")
                    ));
                }
            }
        }

        return result;
    }

    public List<TimeInterval> loadDayIntervals(
            int employeeId,
            LocalDate date
    ) {
        return loadStoredDayIntervals(employeeId, date)
                .stream()
                .map(item -> new TimeInterval(
                        item.start(),
                        item.end(),
                        item.type()
                ))
                .toList();
    }

    public List<TimeInterval> loadWorkIntervals(
            int employeeId,
            LocalDate date
    ) {
        return loadDayIntervals(employeeId, date)
                .stream()
                .filter(item ->
                        "Lucru".equals(item.type())
                )
                .toList();
    }

    public List<TimeInterval> loadAbsenceIntervals(
            int employeeId,
            LocalDate date
    ) {
        return loadDayIntervals(employeeId, date)
                .stream()
                .filter(item ->
                        "Absenta".equals(item.type())
                )
                .toList();
    }

    public void saveInterval(
            int employeeId,
            LocalDateTime start,
            LocalDateTime end,
            String type
    ) throws SQLException {

        writeInterval(
                employeeId,
                null,
                start,
                end,
                type
        );
    }

    public void updateInterval(
            int employeeId,
            int intervalId,
            LocalDateTime start,
            LocalDateTime end,
            String type
    ) throws SQLException {

        writeInterval(
                employeeId,
                intervalId,
                start,
                end,
                type
        );
    }

    private void validateInterval(
            LocalDateTime start,
            LocalDateTime end,
            String type
    ) {
        if (start == null
                || end == null
                || !end.isAfter(start)) {

            throw new IllegalArgumentException(
                    "Ora de sfârșit trebuie să fie după început."
            );
        }

        boolean sameDay =
                start.toLocalDate().equals(end.toLocalDate());

        boolean endsAtNextMidnight =
                end.equals(
                        start.toLocalDate()
                                .plusDays(1)
                                .atStartOfDay()
                );

        if (!sameDay && !endsAtNextMidnight) {
            throw new IllegalArgumentException(
                    "Intervalul trebuie să aparțină unei singure zile."
            );
        }

        if (!"Lucru".equals(type)
                && !"Absenta".equals(type)) {

            throw new IllegalArgumentException(
                    "Tipul intervalului nu este valid."
            );
        }
    }

    private void writeInterval(
            int employeeId,
            Integer intervalId,
            LocalDateTime start,
            LocalDateTime end,
            String type
    ) throws SQLException {

        validateInterval(start, end, type);

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                lockEmployee(connection, employeeId);

                /*
                 * Două intervale de același tip nu se suprapun.
                 * O absență poate fi în interiorul programului de lucru.
                 */
                String overlapSql = """
                        SELECT id_interval
                        FROM interval_disponibilitate
                        WHERE id_angajat = ?
                          AND tip_interval = ?
                          AND inceput < ?
                          AND sfarsit > ?
                          AND (? IS NULL OR id_interval <> ?)
                        FOR UPDATE
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(overlapSql)
                ) {
                    statement.setInt(1, employeeId);
                    statement.setString(2, type);
                    statement.setTimestamp(
                            3, Timestamp.valueOf(end)
                    );
                    statement.setTimestamp(
                            4, Timestamp.valueOf(start)
                    );
                    statement.setObject(5, intervalId);
                    statement.setObject(6, intervalId);

                    try (ResultSet rows = statement.executeQuery()) {
                        if (rows.next()) {
                            throw new SQLException(
                                    "Există deja un interval de același "
                                            + "tip care se suprapune. "
                                            + "Modifică intervalul existent."
                            );
                        }
                    }
                }

                if ("Absenta".equals(type)) {
                    ensureNoBookings(
                            connection,
                            employeeId,
                            start,
                            end
                    );
                }

                if (intervalId == null) {
                    String sql = """
                            INSERT INTO interval_disponibilitate
                                (
                                    id_angajat,
                                    inceput,
                                    sfarsit,
                                    tip_interval
                                )
                            VALUES (?, ?, ?, ?)
                            """;

                    try (
                            PreparedStatement statement =
                                    connection.prepareStatement(sql)
                    ) {
                        statement.setInt(1, employeeId);
                        statement.setTimestamp(
                                2, Timestamp.valueOf(start)
                        );
                        statement.setTimestamp(
                                3, Timestamp.valueOf(end)
                        );
                        statement.setString(4, type);
                        statement.executeUpdate();
                    }

                } else {
                    String sql = """
                            UPDATE interval_disponibilitate
                            SET inceput = ?,
                                sfarsit = ?,
                                tip_interval = ?
                            WHERE id_interval = ?
                              AND id_angajat = ?
                            """;

                    try (
                            PreparedStatement statement =
                                    connection.prepareStatement(sql)
                    ) {
                        statement.setTimestamp(
                                1, Timestamp.valueOf(start)
                        );
                        statement.setTimestamp(
                                2, Timestamp.valueOf(end)
                        );
                        statement.setString(3, type);
                        statement.setInt(4, intervalId);
                        statement.setInt(5, employeeId);

                        if (statement.executeUpdate() == 0) {
                            /*
                             * Un UPDATE fără schimbări poate întoarce 0.
                             * Verificăm dacă intervalul mai există.
                             */
                            ensureIntervalExists(
                                    connection,
                                    employeeId,
                                    intervalId
                            );
                        }
                    }
                }

                connection.commit();

            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    private void ensureIntervalExists(
            Connection connection,
            int employeeId,
            int intervalId
    ) throws SQLException {

        String sql = """
                SELECT id_interval
                FROM interval_disponibilitate
                WHERE id_interval = ?
                  AND id_angajat = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, intervalId);
            statement.setInt(2, employeeId);

            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new SQLException(
                            "Intervalul nu mai există."
                    );
                }
            }
        }
    }

    public void deleteStoredInterval(
            int employeeId,
            int intervalId
    ) throws SQLException {

        String sql = """
                DELETE FROM interval_disponibilitate
                WHERE id_interval = ?
                  AND id_angajat = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, intervalId);
            statement.setInt(2, employeeId);

            if (statement.executeUpdate() == 0) {
                throw new SQLException(
                        "Intervalul nu mai există."
                );
            }
        }
    }

    /*
     * Compatibilitate cu apelurile vechi.
     * Panoul nou folosește ștergerea după id_interval.
     */
    public void deleteInterval(
            int employeeId,
            TimeInterval interval
    ) throws SQLException {

        String sql = """
                DELETE FROM interval_disponibilitate
                WHERE id_angajat = ?
                  AND inceput = ?
                  AND sfarsit = ?
                  AND tip_interval = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, employeeId);
            statement.setTimestamp(
                    2, Timestamp.valueOf(interval.start())
            );
            statement.setTimestamp(
                    3, Timestamp.valueOf(interval.end())
            );
            statement.setString(4, interval.type());
            statement.executeUpdate();
        }
    }

    public void markDayOff(
            int employeeId,
            LocalDate date
    ) throws SQLException {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                lockEmployee(connection, employeeId);

                ensureNoBookings(
                        connection, employeeId, start, end
                );

                List<StoredInterval> intervals =
                        loadStoredIntervals(
                                connection, employeeId, date
                        );

                /*
                 * Nu tăiem automat intervale care traversează
                 * limita zilei și afectează o altă dată.
                 */
                for (StoredInterval interval : intervals) {
                    if (interval.start().isBefore(start)
                            || interval.end().isAfter(end)) {

                        throw new SQLException(
                                "Există un interval care traversează "
                                        + "mai multe zile. Modifică-l "
                                        + "înainte de a marca ziua liberă."
                        );
                    }
                }

                String deleteSql = """
                        DELETE FROM interval_disponibilitate
                        WHERE id_angajat = ?
                          AND inceput >= ?
                          AND sfarsit <= ?
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(deleteSql)
                ) {
                    statement.setInt(1, employeeId);
                    statement.setTimestamp(
                            2, Timestamp.valueOf(start)
                    );
                    statement.setTimestamp(
                            3, Timestamp.valueOf(end)
                    );
                    statement.executeUpdate();
                }

                String insertSql = """
                        INSERT INTO interval_disponibilitate
                            (
                                id_angajat,
                                inceput,
                                sfarsit,
                                tip_interval
                            )
                        VALUES (?, ?, ?, 'Absenta')
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(insertSql)
                ) {
                    statement.setInt(1, employeeId);
                    statement.setTimestamp(
                            2, Timestamp.valueOf(start)
                    );
                    statement.setTimestamp(
                            3, Timestamp.valueOf(end)
                    );
                    statement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    private void lockEmployee(
            Connection connection,
            int employeeId
    ) throws SQLException {

        String sql = """
                SELECT id_angajat
                FROM angajat
                WHERE id_angajat = ?
                FOR UPDATE
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, employeeId);

            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new SQLException(
                            "Angajatul nu există."
                    );
                }
            }
        }
    }

    private void ensureNoBookings(
            Connection connection,
            int employeeId,
            LocalDateTime start,
            LocalDateTime end
    ) throws SQLException {

        List<TimeInterval> busy = new ArrayList<>();

        LocalDate first = start.toLocalDate();
        LocalDate last = end.minusNanos(1).toLocalDate();

        for (LocalDate date = first;
             !date.isAfter(last);
             date = date.plusDays(1)) {

            busy.addAll(
                    loadAppointmentIntervals(
                            connection, employeeId, date
                    )
            );

            busy.addAll(
                    loadCourseIntervals(
                            connection, employeeId, date
                    )
            );
        }

        for (TimeInterval occupied : busy) {
            if (start.isBefore(occupied.end())
                    && end.isAfter(occupied.start())) {

                throw new SQLException(
                        "Intervalul se suprapune cu o programare "
                                + "sau cu un curs. Rezolvă mai întâi "
                                + "programarea/cursul existent."
                );
            }
        }
    }

    public List<TimeInterval> loadAppointmentIntervals(
            int employeeId,
            LocalDate date
    ) {
        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            return loadAppointmentIntervals(
                    connection, employeeId, date
            );

        } catch (SQLException exception) {
            throw new AvailabilityException(
                    "Programările nu au putut fi încărcate.",
                    exception
            );
        }
    }

    private List<TimeInterval> loadAppointmentIntervals(
            Connection connection,
            int employeeId,
            LocalDate date
    ) throws SQLException {

        String sql = """
                SELECT
                    p.id_programare,
                    p.data,
                    p.ora,
                    COALESCE(
                        SUM(ps.durata_aplicata), 0
                    ) AS durata_totala
                FROM programare p
                LEFT JOIN programare_serviciu ps
                    ON ps.id_programare = p.id_programare
                WHERE p.id_tehnician = ?
                  AND p.stare <> 'Anulata'
                  AND p.data <= ?
                GROUP BY
                    p.id_programare,
                    p.data,
                    p.ora
                ORDER BY p.data, p.ora
                """;

        List<TimeInterval> result = new ArrayList<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, employeeId);
            statement.setDate(
                    2, java.sql.Date.valueOf(date)
            );

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    LocalDateTime start = LocalDateTime.of(
                            rows.getDate("data").toLocalDate(),
                            rows.getTime("ora").toLocalTime()
                    );

                    int duration =
                            rows.getInt("durata_totala");

                    if (duration <= 0) {
                        if (start.toLocalDate().equals(date)) {
                            throw new SQLException(
                                    "Programarea "
                                            + rows.getInt("id_programare")
                                            + " nu are durată validă."
                            );
                        }
                        continue;
                    }

                    LocalDateTime end =
                            start.plusMinutes(duration);

                    if (start.isBefore(
                            date.plusDays(1).atStartOfDay()
                    ) && end.isAfter(date.atStartOfDay())) {

                        result.add(new TimeInterval(
                                start, end, "Programare"
                        ));
                    }
                }
            }
        }

        return result;
    }

    public List<TimeInterval> loadCourseIntervals(
            int employeeId,
            LocalDate date
    ) {
        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            return loadCourseIntervals(
                    connection, employeeId, date
            );

        } catch (SQLException exception) {
            throw new AvailabilityException(
                    "Cursurile nu au putut fi încărcate.",
                    exception
            );
        }
    }

    private List<TimeInterval> loadCourseIntervals(
            Connection connection,
            int employeeId,
            LocalDate date
    ) throws SQLException {

        String sql = """
                SELECT i.inceput, i.sfarsit
                FROM interval_sesiune i
                JOIN sesiune_curs s
                    ON s.id_sesiune = i.id_sesiune
                WHERE s.id_trainer = ?
                  AND s.stare <> 'Anulata'
                  AND i.inceput < ?
                  AND i.sfarsit > ?
                ORDER BY i.inceput
                """;

        List<TimeInterval> result = new ArrayList<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, employeeId);
            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            date.plusDays(1).atStartOfDay()
                    )
            );
            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(date.atStartOfDay())
            );

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new TimeInterval(
                            rows.getTimestamp("inceput")
                                    .toLocalDateTime(),
                            rows.getTimestamp("sfarsit")
                                    .toLocalDateTime(),
                            "Curs"
                    ));
                }
            }
        }

        return result;
    }

    public List<LocalDateTime> availableSlots(
            int employeeId,
            LocalDate date,
            int durationMinutes
    ) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Durata trebuie să fie mai mare decât zero."
            );
        }

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            List<StoredInterval> stored =
                    loadStoredIntervals(
                            connection, employeeId, date
                    );

            List<TimeInterval> work = new ArrayList<>();
            List<TimeInterval> busy = new ArrayList<>();

            for (StoredInterval item : stored) {
                TimeInterval interval = new TimeInterval(
                        item.start(), item.end(), item.type()
                );

                if ("Lucru".equals(item.type())) {
                    work.add(interval);
                } else {
                    busy.add(interval);
                }
            }

            busy.addAll(loadAppointmentIntervals(
                    connection, employeeId, date
            ));

            busy.addAll(loadCourseIntervals(
                    connection, employeeId, date
            ));

            work = mergeWorkIntervals(work);

            TreeSet<LocalDateTime> slots = new TreeSet<>();

            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd =
                    date.plusDays(1).atStartOfDay();
            LocalDateTime now = LocalDateTime.now();

            for (TimeInterval interval : work) {
                LocalDateTime start =
                        interval.start().isAfter(dayStart)
                                ? interval.start()
                                : dayStart;

                LocalDateTime end =
                        interval.end().isBefore(dayEnd)
                                ? interval.end()
                                : dayEnd;

                for (LocalDateTime candidate = start;
                     !candidate.plusMinutes(durationMinutes)
                             .isAfter(end);
                     candidate = candidate.plusMinutes(30)) {

                    if (!candidate.isAfter(now)) {
                        continue;
                    }

                    LocalDateTime candidateEnd =
                            candidate.plusMinutes(durationMinutes);

                    boolean occupied = false;

                    for (TimeInterval item : busy) {
                        if (candidate.isBefore(item.end())
                                && candidateEnd.isAfter(item.start())) {
                            occupied = true;
                            break;
                        }
                    }

                    if (!occupied) {
                        slots.add(candidate);
                    }
                }
            }

            return new ArrayList<>(slots);

        } catch (SQLException exception) {
            throw new AvailabilityException(
                    "Disponibilitatea nu a putut fi calculată.",
                    exception
            );
        }
    }

    private List<TimeInterval> mergeWorkIntervals(
            List<TimeInterval> intervals
    ) {
        List<TimeInterval> sorted =
                new ArrayList<>(intervals);

        sorted.sort(
                Comparator.comparing(TimeInterval::start)
        );

        List<TimeInterval> merged = new ArrayList<>();

        for (TimeInterval current : sorted) {
            if (merged.isEmpty()) {
                merged.add(current);
                continue;
            }

            TimeInterval previous =
                    merged.get(merged.size() - 1);

            if (!current.start().isAfter(previous.end())) {
                LocalDateTime end =
                        current.end().isAfter(previous.end())
                                ? current.end()
                                : previous.end();

                merged.set(
                        merged.size() - 1,
                        new TimeInterval(
                                previous.start(), end, "Lucru"
                        )
                );
            } else {
                merged.add(current);
            }
        }

        return merged;
    }

    private void rollback(
            Connection connection,
            Exception original
    ) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            original.addSuppressed(exception);
        }
    }
}