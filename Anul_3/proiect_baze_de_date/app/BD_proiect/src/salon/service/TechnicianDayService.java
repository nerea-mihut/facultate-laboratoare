package salon.service;

import salon.db.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TechnicianDayService {

    public record ServiceOffer(
            int serviceId,
            String name,
            String category,
            BigDecimal price,
            int durationMinutes
    ) {
        @Override
        public String toString() {
            return name;
        }
    }

    public record DayInterval(
            LocalDateTime start,
            LocalDateTime end
    ) {
    }

    public record DayConfiguration(
            LocalDate date,
            String dayType,
            boolean explicitlyConfigured,
            List<DayInterval> workIntervals,
            List<DayInterval> breaks,
            List<ServiceOffer> availableOffers,
            Set<Integer> selectedServiceIds
    ) {
    }

    public static class DayConfigurationException
            extends SQLException {

        public DayConfigurationException(String message) {
            super(message);
        }
    }

    public DayConfiguration loadDay(
            int technicianId,
            LocalDate date
    ) throws SQLException {

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            List<ServiceOffer> offers =
                    loadActiveOffers(connection, technicianId);

            List<DayInterval> work = new ArrayList<>();
            List<DayInterval> breaks = new ArrayList<>();

            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd =
                    date.plusDays(1).atStartOfDay();

            String intervalsSql = """
                    SELECT inceput, sfarsit, tip_interval
                    FROM interval_disponibilitate
                    WHERE id_angajat = ?
                      AND inceput < ?
                      AND sfarsit > ?
                    ORDER BY inceput, id_interval
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(intervalsSql)
            ) {
                statement.setInt(1, technicianId);
                statement.setTimestamp(
                        2, Timestamp.valueOf(dayEnd)
                );
                statement.setTimestamp(
                        3, Timestamp.valueOf(dayStart)
                );

                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        LocalDateTime start =
                                rows.getTimestamp("inceput")
                                        .toLocalDateTime();

                        LocalDateTime end =
                                rows.getTimestamp("sfarsit")
                                        .toLocalDateTime();

                        /*
                         * Afișăm numai partea intervalului
                         * care aparține datei selectate.
                         */
                        start = start.isBefore(dayStart)
                                ? dayStart
                                : start;

                        end = end.isAfter(dayEnd)
                                ? dayEnd
                                : end;

                        DayInterval interval =
                                new DayInterval(start, end);

                        if ("Lucru".equals(
                                rows.getString("tip_interval")
                        )) {
                            work.add(interval);
                        } else {
                            breaks.add(interval);
                        }
                    }
                }
            }

            boolean configured = false;
            String dayType = "Lucru";

            String configurationSql = """
                    SELECT tip_zi
                    FROM configurare_zi_tehnician
                    WHERE id_tehnician = ?
                      AND data = ?
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(configurationSql)
            ) {
                statement.setInt(1, technicianId);
                statement.setDate(
                        2, java.sql.Date.valueOf(date)
                );

                try (ResultSet rows = statement.executeQuery()) {
                    if (rows.next()) {
                        configured = true;
                        dayType = rows.getString("tip_zi");
                    }
                }
            }

            Set<Integer> selectedIds = new HashSet<>();

            if (configured) {
                String selectedSql = """
                        SELECT sz.id_serviciu
                        FROM serviciu_zi_tehnician sz
                        JOIN tehnician_serviciu ts
                            ON ts.id_tehnician = sz.id_tehnician
                           AND ts.id_serviciu = sz.id_serviciu
                        WHERE sz.id_tehnician = ?
                          AND sz.data = ?
                          AND ts.stare_oferta = 'Activa'
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(selectedSql)
                ) {
                    statement.setInt(1, technicianId);
                    statement.setDate(
                            2, java.sql.Date.valueOf(date)
                    );

                    try (ResultSet rows = statement.executeQuery()) {
                        while (rows.next()) {
                            selectedIds.add(
                                    rows.getInt("id_serviciu")
                            );
                        }
                    }
                }

            } else {
                /*
                 * Zilele vechi păstrează oferta generală activă.
                 */
                for (ServiceOffer offer : offers) {
                    selectedIds.add(offer.serviceId());
                }

                boolean fullDayAbsence = breaks.stream()
                        .anyMatch(interval ->
                                interval.start().equals(dayStart)
                                        && interval.end().equals(dayEnd)
                        );

                if (fullDayAbsence) {
                    dayType = "Libera";
                }
            }

            return new DayConfiguration(
                    date,
                    dayType,
                    configured,
                    List.copyOf(work),
                    List.copyOf(breaks),
                    List.copyOf(offers),
                    Set.copyOf(selectedIds)
            );
        }
    }

    private List<ServiceOffer> loadActiveOffers(
            Connection connection,
            int technicianId
    ) throws SQLException {

        String sql = """
                SELECT
                    s.id_serviciu,
                    s.denumire,
                    s.categorie,
                    ts.pret,
                    ts.durata
                FROM tehnician_serviciu ts
                JOIN serviciu s
                    ON s.id_serviciu = ts.id_serviciu
                WHERE ts.id_tehnician = ?
                  AND ts.stare_oferta = 'Activa'
                ORDER BY s.categorie, s.id_serviciu
                """;

        List<ServiceOffer> offers = new ArrayList<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, technicianId);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    offers.add(new ServiceOffer(
                            rows.getInt("id_serviciu"),
                            rows.getString("denumire"),
                            rows.getString("categorie"),
                            rows.getBigDecimal("pret"),
                            rows.getInt("durata")
                    ));
                }
            }
        }

        return offers;
    }

    public void saveWorkingDay(
            int technicianId,
            LocalDate date,
            List<DayInterval> workIntervals,
            List<DayInterval> breaks,
            Set<Integer> serviceIds
    ) throws SQLException {

        if (date == null) {
            throw new DayConfigurationException(
                    "Selectează o dată."
            );
        }

        List<DayInterval> work =
                normalizeIntervals(date, workIntervals);

        List<DayInterval> pauses =
                normalizeIntervals(date, breaks);

        Set<Integer> selected = serviceIds == null
                ? Set.of()
                : Set.copyOf(serviceIds);

        if (work.isEmpty()) {
            throw new DayConfigurationException(
                    "Adaugă cel puțin un interval de lucru."
            );
        }

        if (selected.isEmpty()) {
            throw new DayConfigurationException(
                    "Selectează cel puțin un serviciu."
            );
        }

        for (DayInterval pause : pauses) {
            boolean insideWork = work.stream()
                    .anyMatch(interval ->
                            !pause.start().isBefore(interval.start())
                                    && !pause.end().isAfter(interval.end())
                    );

            if (!insideWork) {
                throw new DayConfigurationException(
                        "Fiecare pauză trebuie să fie "
                                + "în interiorul unui interval de lucru."
                );
            }
        }

        List<DayInterval> effectiveWork =
                subtractBreaks(work, pauses);

        if (effectiveWork.isEmpty()) {
            throw new DayConfigurationException(
                    "Pauzele ocupă întregul program de lucru."
            );
        }

        save(
                technicianId,
                date,
                "Lucru",
                work,
                pauses,
                selected,
                effectiveWork
        );
    }

    public void saveDayOff(
            int technicianId,
            LocalDate date
    ) throws SQLException {

        if (date == null) {
            throw new DayConfigurationException(
                    "Selectează o dată."
            );
        }

        save(
                technicianId,
                date,
                "Libera",
                List.of(),
                List.of(
                        new DayInterval(
                                date.atStartOfDay(),
                                date.plusDays(1).atStartOfDay()
                        )
                ),
                Set.of(),
                List.of()
        );
    }

    private void save(
            int technicianId,
            LocalDate date,
            String dayType,
            List<DayInterval> work,
            List<DayInterval> pauses,
            Set<Integer> selected,
            List<DayInterval> effectiveWork
    ) throws SQLException {

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                lockTechnician(connection, technicianId);

                ensureIntervalsBelongToDay(
                        connection, technicianId, date
                );

                validateSelectedOffers(
                        connection, technicianId, selected
                );

                validateExistingAppointments(
                        connection,
                        technicianId,
                        date,
                        dayType,
                        selected,
                        effectiveWork
                );

                validateExistingCourses(
                        connection,
                        technicianId,
                        date,
                        dayType,
                        effectiveWork
                );

                upsertConfiguration(
                        connection, technicianId, date, dayType
                );

                replaceSelectedServices(
                        connection, technicianId, date, selected
                );

                replaceIntervals(
                        connection,
                        technicianId,
                        date,
                        work,
                        pauses
                );

                connection.commit();

            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw exception;
            }
        }
    }

    private void lockTechnician(
            Connection connection,
            int technicianId
    ) throws SQLException {

        /*
         * Ordinea este angajat, apoi tehnician.
         * Se aliniază cu serviciul de disponibilitate existent.
         */
        String employeeSql = """
                SELECT id_angajat
                FROM angajat
                WHERE id_angajat = ?
                FOR UPDATE
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(employeeSql)
        ) {
            statement.setInt(1, technicianId);

            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new DayConfigurationException(
                            "Angajatul nu există."
                    );
                }
            }
        }

        String technicianSql = """
                SELECT id_angajat
                FROM tehnician
                WHERE id_angajat = ?
                FOR UPDATE
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(technicianSql)
        ) {
            statement.setInt(1, technicianId);

            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new DayConfigurationException(
                            "Contul nu aparține unui tehnician."
                    );
                }
            }
        }
    }

    private List<DayInterval> normalizeIntervals(
            LocalDate date,
            List<DayInterval> intervals
    ) throws SQLException {

        List<DayInterval> sorted = intervals == null
                ? new ArrayList<>()
                : new ArrayList<>(intervals);

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd =
                date.plusDays(1).atStartOfDay();

        for (DayInterval interval : sorted) {
            if (interval == null
                    || interval.start() == null
                    || interval.end() == null
                    || !interval.end().isAfter(interval.start())) {

                throw new DayConfigurationException(
                        "Verifică începutul și sfârșitul intervalelor."
                );
            }

            if (interval.start().isBefore(dayStart)
                    || interval.end().isAfter(dayEnd)) {

                throw new DayConfigurationException(
                        "Intervalele trebuie să aparțină zilei selectate."
                );
            }
        }

        sorted.sort(
                Comparator.comparing(DayInterval::start)
        );

        List<DayInterval> merged = new ArrayList<>();

        for (DayInterval current : sorted) {
            if (merged.isEmpty()) {
                merged.add(current);
                continue;
            }

            DayInterval previous =
                    merged.get(merged.size() - 1);

            if (current.start().isBefore(previous.end())) {
                throw new DayConfigurationException(
                        "Două intervale de același tip se suprapun."
                );
            }

            if (current.start().equals(previous.end())) {
                merged.set(
                        merged.size() - 1,
                        new DayInterval(
                                previous.start(),
                                current.end()
                        )
                );
            } else {
                merged.add(current);
            }
        }

        return merged;
    }

    private List<DayInterval> subtractBreaks(
            List<DayInterval> work,
            List<DayInterval> pauses
    ) {
        List<DayInterval> result = new ArrayList<>();

        for (DayInterval interval : work) {
            LocalDateTime cursor = interval.start();

            for (DayInterval pause : pauses) {
                if (!pause.start().isBefore(interval.end())
                        || !pause.end().isAfter(interval.start())) {
                    continue;
                }

                if (pause.start().isAfter(cursor)) {
                    result.add(
                            new DayInterval(cursor, pause.start())
                    );
                }

                if (pause.end().isAfter(cursor)) {
                    cursor = pause.end();
                }
            }

            if (cursor.isBefore(interval.end())) {
                result.add(
                        new DayInterval(cursor, interval.end())
                );
            }
        }

        return result;
    }

    private void ensureIntervalsBelongToDay(
            Connection connection,
            int technicianId,
            LocalDate date
    ) throws SQLException {

        String sql = """
                SELECT id_interval, inceput, sfarsit
                FROM interval_disponibilitate
                WHERE id_angajat = ?
                  AND inceput < ?
                  AND sfarsit > ?
                FOR UPDATE
                """;

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end =
                date.plusDays(1).atStartOfDay();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, technicianId);
            statement.setTimestamp(2, Timestamp.valueOf(end));
            statement.setTimestamp(3, Timestamp.valueOf(start));

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    LocalDateTime storedStart =
                            rows.getTimestamp("inceput")
                                    .toLocalDateTime();

                    LocalDateTime storedEnd =
                            rows.getTimestamp("sfarsit")
                                    .toLocalDateTime();

                    if (storedStart.isBefore(start)
                            || storedEnd.isAfter(end)) {

                        throw new DayConfigurationException(
                                "Intervalul "
                                        + rows.getInt("id_interval")
                                        + " traversează mai multe zile. "
                                        + "Trebuie separat înainte "
                                        + "de configurarea acestei date."
                        );
                    }
                }
            }
        }
    }

    private void validateSelectedOffers(
            Connection connection,
            int technicianId,
            Set<Integer> selected
    ) throws SQLException {

        Set<Integer> activeIds = new HashSet<>();

        for (ServiceOffer offer :
                loadActiveOffers(connection, technicianId)) {

            activeIds.add(offer.serviceId());
        }

        if (!activeIds.containsAll(selected)) {
            throw new DayConfigurationException(
                    "Un serviciu selectat nu mai este "
                            + "în oferta activă a tehnicianului."
            );
        }
    }

    private void validateExistingAppointments(
            Connection connection,
            int technicianId,
            LocalDate date,
            String dayType,
            Set<Integer> selected,
            List<DayInterval> effectiveWork
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

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd =
                date.plusDays(1).atStartOfDay();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, technicianId);
            statement.setDate(
                    2, java.sql.Date.valueOf(date)
            );

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    int appointmentId =
                            rows.getInt("id_programare");

                    LocalDateTime start =
                            LocalDateTime.of(
                                    rows.getDate("data").toLocalDate(),
                                    rows.getTime("ora").toLocalTime()
                            );

                    int duration =
                            rows.getInt("durata_totala");

                    if (duration <= 0) {
                        if (start.toLocalDate().equals(date)) {
                            throw new DayConfigurationException(
                                    "Programarea " + appointmentId
                                            + " nu are durată validă."
                            );
                        }
                        continue;
                    }

                    LocalDateTime end =
                            start.plusMinutes(duration);

                    if (!start.isBefore(dayEnd)
                            || !end.isAfter(dayStart)) {
                        continue;
                    }

                    if ("Libera".equals(dayType)) {
                        throw new DayConfigurationException(
                                "Ziua are deja programarea "
                                        + appointmentId
                                        + ". Anuleaz-o sau reprogrameaz-o "
                                        + "înainte de a marca ziua liberă."
                        );
                    }

                    LocalDateTime clippedStart =
                            start.isBefore(dayStart)
                                    ? dayStart
                                    : start;

                    LocalDateTime clippedEnd =
                            end.isAfter(dayEnd)
                                    ? dayEnd
                                    : end;

                    if (!isCovered(
                            effectiveWork,
                            clippedStart,
                            clippedEnd
                    )) {
                        throw new DayConfigurationException(
                                "Programarea " + appointmentId
                                        + " ar rămâne în afara programului "
                                        + "sau s-ar suprapune cu o pauză."
                        );
                    }

                    /*
                     * Serviciile zilnice controlează rezervările
                     * care încep în această zi.
                     */
                    if (start.toLocalDate().equals(date)) {
                        validateAppointmentServices(
                                connection,
                                appointmentId,
                                selected
                        );
                    }
                }
            }
        }
    }

    private void validateAppointmentServices(
            Connection connection,
            int appointmentId,
            Set<Integer> selected
    ) throws SQLException {

        String sql = """
                SELECT id_serviciu
                FROM programare_serviciu
                WHERE id_programare = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, appointmentId);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    if (!selected.contains(
                            rows.getInt("id_serviciu")
                    )) {
                        throw new DayConfigurationException(
                                "Un serviciu debifat este deja folosit "
                                        + "în programarea "
                                        + appointmentId
                                        + ". Păstrează-l selectat "
                                        + "sau modifică programarea."
                        );
                    }
                }
            }
        }
    }

    private void validateExistingCourses(
            Connection connection,
            int technicianId,
            LocalDate date,
            String dayType,
            List<DayInterval> effectiveWork
    ) throws SQLException {

        String sql = """
                SELECT
                    s.id_sesiune,
                    i.inceput,
                    i.sfarsit
                FROM interval_sesiune i
                JOIN sesiune_curs s
                    ON s.id_sesiune = i.id_sesiune
                WHERE s.id_trainer = ?
                  AND s.stare <> 'Anulata'
                  AND i.inceput < ?
                  AND i.sfarsit > ?
                """;

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd =
                date.plusDays(1).atStartOfDay();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, technicianId);
            statement.setTimestamp(
                    2, Timestamp.valueOf(dayEnd)
            );
            statement.setTimestamp(
                    3, Timestamp.valueOf(dayStart)
            );

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    int sessionId =
                            rows.getInt("id_sesiune");

                    if ("Libera".equals(dayType)) {
                        throw new DayConfigurationException(
                                "Există un curs în această zi: sesiunea "
                                        + sessionId
                                        + ". Ziua nu poate fi marcată liberă."
                        );
                    }

                    LocalDateTime start =
                            rows.getTimestamp("inceput")
                                    .toLocalDateTime();

                    LocalDateTime end =
                            rows.getTimestamp("sfarsit")
                                    .toLocalDateTime();

                    start = start.isBefore(dayStart)
                            ? dayStart
                            : start;

                    end = end.isAfter(dayEnd)
                            ? dayEnd
                            : end;

                    if (!isCovered(effectiveWork, start, end)) {
                        throw new DayConfigurationException(
                                "Sesiunea de curs "
                                        + sessionId
                                        + " ar rămâne în afara programului "
                                        + "sau s-ar suprapune cu o pauză."
                        );
                    }
                }
            }
        }
    }

    private boolean isCovered(
            List<DayInterval> intervals,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return intervals.stream().anyMatch(interval ->
                !start.isBefore(interval.start())
                        && !end.isAfter(interval.end())
        );
    }

    private void upsertConfiguration(
            Connection connection,
            int technicianId,
            LocalDate date,
            String dayType
    ) throws SQLException {

        String sql = """
                INSERT INTO configurare_zi_tehnician
                    (id_tehnician, data, tip_zi)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    tip_zi = VALUES(tip_zi)
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, technicianId);
            statement.setDate(
                    2, java.sql.Date.valueOf(date)
            );
            statement.setString(3, dayType);
            statement.executeUpdate();
        }
    }

    private void replaceSelectedServices(
            Connection connection,
            int technicianId,
            LocalDate date,
            Set<Integer> selected
    ) throws SQLException {

        String deleteSql = """
                DELETE FROM serviciu_zi_tehnician
                WHERE id_tehnician = ?
                  AND data = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(deleteSql)
        ) {
            statement.setInt(1, technicianId);
            statement.setDate(
                    2, java.sql.Date.valueOf(date)
            );
            statement.executeUpdate();
        }

        String insertSql = """
                INSERT INTO serviciu_zi_tehnician
                    (id_tehnician, data, id_serviciu)
                VALUES (?, ?, ?)
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(insertSql)
        ) {
            for (Integer serviceId : selected) {
                statement.setInt(1, technicianId);
                statement.setDate(
                        2, java.sql.Date.valueOf(date)
                );
                statement.setInt(3, serviceId);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void replaceIntervals(
            Connection connection,
            int technicianId,
            LocalDate date,
            List<DayInterval> work,
            List<DayInterval> pauses
    ) throws SQLException {

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
            statement.setInt(1, technicianId);
            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(date.atStartOfDay())
            );
            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            date.plusDays(1).atStartOfDay()
                    )
            );
            statement.executeUpdate();
        }

        insertIntervals(
                connection, technicianId, work, "Lucru"
        );

        insertIntervals(
                connection, technicianId, pauses, "Absenta"
        );
    }

    private void insertIntervals(
            Connection connection,
            int technicianId,
            List<DayInterval> intervals,
            String type
    ) throws SQLException {

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
            for (DayInterval interval : intervals) {
                statement.setInt(1, technicianId);
                statement.setTimestamp(
                        2,
                        Timestamp.valueOf(interval.start())
                );
                statement.setTimestamp(
                        3,
                        Timestamp.valueOf(interval.end())
                );
                statement.setString(4, type);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }
}