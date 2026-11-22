package salon.ui;

import salon.db.DatabaseConnection;
import salon.service.AvailabilityService;
import salon.service.EmployeeAccessService.Access;
import salon.service.TechnicianDayService;
import salon.service.TechnicianDayService.DayConfiguration;
import salon.service.TechnicianDayService.ServiceOffer;
import salon.ui.components.UiStyle;
import salon.ui.technician.TechnicianDayDialog;
import salon.ui.technician.TechnicianDayServicesPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmployeeCalendarPanel extends JPanel {

    private static final int NEUTRAL = 0;
    private static final int AVAILABLE = 1;
    private static final int UNAVAILABLE = 2;
    private static final int COURSE = 3;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Access access;
    private final AvailabilityService availability;

    private final TechnicianDayService days =
            new TechnicianDayService();

    private final Runnable back;
    private final Runnable openCourses;

    private final JPanel grid =
            new JPanel(new GridLayout(0, 7, 8, 8));

    private final JLabel monthLabel =
            UiStyle.label("", 22, true, UiStyle.INK);

    private final JLabel status =
            UiStyle.label(" ", 12, false, UiStyle.MUTED);

    private final JButton previous =
            UiStyle.secondaryButton("‹");

    private final JButton next =
            UiStyle.secondaryButton("›");

    private final JButton refresh =
            UiStyle.secondaryButton("Reîncarcă");

    private YearMonth month = YearMonth.now();
    private boolean busy;

    private Map<LocalDate, Integer> states = new HashMap<>();

    private Map<LocalDate, List<String>> courses =
            new HashMap<>();

    private Map<LocalDate, String> reasons =
            new HashMap<>();

    private record Snapshot(
            Map<LocalDate, Integer> states,
            Map<LocalDate, List<String>> courses,
            Map<LocalDate, String> reasons
    ) {
    }

    public EmployeeCalendarPanel(
            Access access,
            AvailabilityService availability,
            Runnable back,
            Runnable openCourses
    ) {
        this.access = access;
        this.availability = availability;
        this.back = back;
        this.openCourses = openCourses;

        setLayout(new BorderLayout(0, 18));
        setBackground(UiStyle.CREAM);

        setBorder(
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        );

        JPanel header = transparent(new BorderLayout(12, 0));

        JButton home = UiStyle.secondaryButton("Înapoi");

        home.addActionListener(event -> {
            if (!busy) {
                this.back.run();
            }
        });

        header.add(home, BorderLayout.WEST);

        header.add(
                UiStyle.label(
                        "Calendar disponibilitate",
                        26,
                        true,
                        UiStyle.INK
                ),
                BorderLayout.CENTER
        );

        header.add(refresh, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel body = transparent(new BorderLayout(0, 14));
        JPanel navigation = transparent(new BorderLayout());

        monthLabel.setHorizontalAlignment(SwingConstants.CENTER);

        navigation.add(previous, BorderLayout.WEST);
        navigation.add(monthLabel, BorderLayout.CENTER);
        navigation.add(next, BorderLayout.EAST);

        body.add(navigation, BorderLayout.NORTH);

        grid.setOpaque(false);
        body.add(grid, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);

        JPanel footer = transparent(new BorderLayout(8, 10));

        footer.add(
                UiStyle.label(
                        "<html>"
                                + "Verde: loc pentru servicii de bază"
                                + " · Roșu: fără disponibilitate"
                                + " · Bej / CURS: zi ocupată de curs"
                                + "</html>",
                        12,
                        false,
                        UiStyle.MUTED
                ),
                BorderLayout.NORTH
        );

        footer.add(status, BorderLayout.CENTER);

        if (access.trainer()) {
            JButton propose =
                    UiStyle.primaryButton(
                            "Propune / modifică un curs"
                    );

            propose.addActionListener(event -> {
                if (!busy) {
                    this.openCourses.run();
                }
            });

            footer.add(propose, BorderLayout.EAST);
        }

        add(footer, BorderLayout.SOUTH);

        previous.addActionListener(event -> change(-1));
        next.addActionListener(event -> change(1));

        refresh.addActionListener(event -> refreshData());

        addHierarchyListener(event -> {
            if ((event.getChangeFlags()
                    & HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) {

                refreshData();
            }
        });

        render();
    }

    private static JPanel transparent(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private void change(int amount) {
        if (busy) {
            return;
        }

        month = month.plusMonths(amount);

        states = new HashMap<>();
        courses = new HashMap<>();
        reasons = new HashMap<>();

        render();
        refreshData();
    }

    public void refreshData() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshData);
            return;
        }

        if (busy) {
            return;
        }

        YearMonth requested = month;

        status.setForeground(UiStyle.MUTED);
        status.setText("Se încarcă salonul și cursurile...");

        setBusy(true);

        new SwingWorker<Snapshot, Void>() {

            @Override
            protected Snapshot doInBackground() throws Exception {
                Map<LocalDate, List<String>> courseDays =
                        loadCourseDays(requested);

                Map<LocalDate, Integer> loadedStates =
                        new HashMap<>();

                Map<LocalDate, String> loadedReasons =
                        new HashMap<>();

                for (
                        int number = 1;
                        number <= requested.lengthOfMonth();
                        number++
                ) {
                    LocalDate date = requested.atDay(number);

                    int state = NEUTRAL;

                    String reason =
                            "Zi fără program de lucru configurat.";

                    if (courseDays.containsKey(date)) {
                        state = COURSE;

                        reason =
                                "Curs — întreaga zi este "
                                        + "indisponibilă pentru salon.";

                    } else if (access.technician()) {
                        DayConfiguration config =
                                days.loadDay(
                                        access.employeeId(),
                                        date
                                );

                        if ("Libera".equals(config.dayType())) {
                            state = UNAVAILABLE;
                            reason = "Zi marcată liberă.";

                        } else if (
                                !config.workIntervals().isEmpty()
                                        || config.explicitlyConfigured()
                        ) {
                            int shortest =
                                    config.availableOffers()
                                            .stream()
                                            .filter(offer ->
                                                    config.selectedServiceIds()
                                                            .contains(
                                                                    offer.serviceId()
                                                            )
                                            )
                                            .filter(offer ->
                                                    !TechnicianDayServicesPanel
                                                            .isDesign(offer)
                                                            && !TechnicianDayServicesPanel
                                                            .isExtra(offer)
                                            )
                                            .mapToInt(
                                                    ServiceOffer::durationMinutes
                                            )
                                            .filter(duration ->
                                                    duration > 0
                                            )
                                            .min()
                                            .orElse(0);

                            if (config.workIntervals().isEmpty()) {
                                state = UNAVAILABLE;

                                reason =
                                        "Zi configurată, dar fără "
                                                + "interval de lucru.";

                            } else if (shortest == 0) {
                                state = UNAVAILABLE;

                                reason =
                                        "Nu există un serviciu de bază "
                                                + "selectat, activ, cu durată "
                                                + "mai mare decât zero.";

                            } else {
                                boolean hasSlots =
                                        !availability.availableSlots(
                                                access.employeeId(),
                                                date,
                                                shortest
                                        ).isEmpty();

                                if (hasSlots) {
                                    state = AVAILABLE;

                                    reason =
                                            "Disponibilă: există loc pentru "
                                                    + "un serviciu de bază de "
                                                    + shortest
                                                    + " minute.";

                                } else {
                                    state = UNAVAILABLE;

                                    reason =
                                            "Programul este configurat, "
                                                    + "dar calculul disponibilității "
                                                    + "nu a găsit ore pentru "
                                                    + shortest
                                                    + " minute.";
                                }
                            }

                        } else if (!config.breaks().isEmpty()) {
                            state = UNAVAILABLE;

                            reason =
                                    "Există pauze / absențe, "
                                            + "dar nu există program de lucru.";
                        }

                    } else {
                        reason =
                                "Selectează ziua pentru a deschide "
                                        + "Cursurile mele.";
                    }

                    loadedStates.put(date, state);
                    loadedReasons.put(date, reason);
                }

                return new Snapshot(
                        loadedStates,
                        courseDays,
                        loadedReasons
                );
            }

            @Override
            protected void done() {
                try {
                    Snapshot snapshot = get();

                    states = snapshot.states();
                    courses = snapshot.courses();
                    reasons = snapshot.reasons();

                    status.setForeground(UiStyle.MUTED);

                    status.setText(
                            "Calendar actualizat. "
                                    + "Ține mouse-ul peste o zi "
                                    + "pentru starea acesteia."
                    );

                } catch (Exception exception) {
                    exception.printStackTrace();

                    status.setForeground(UiStyle.RED);

                    status.setText(
                            "Calendarul nu a putut fi actualizat: "
                                    + message(exception)
                    );

                } finally {
                    setBusy(false);
                }
            }

        }.execute();
    }

    private Map<LocalDate, List<String>> loadCourseDays(
            YearMonth requested
    ) throws SQLException {

        Map<LocalDate, List<String>> result =
                new HashMap<>();

        String sql =
                "SELECT s.id_sesiune, cu.denumire, "
                        + "i.data_zi, i.inceput, i.sfarsit "
                        + "FROM interval_sesiune i "
                        + "JOIN sesiune_curs s "
                        + "ON s.id_sesiune = i.id_sesiune "
                        + "JOIN curs cu ON cu.id_curs = s.id_curs "
                        + "WHERE s.id_trainer = ? "
                        + "AND s.stare <> 'Anulata' "
                        + "AND ("
                        + "(i.data_zi >= ? AND i.data_zi < ?) "
                        + "OR (i.inceput < ? AND i.sfarsit > ?)"
                        + ")";

        LocalDate firstDay = requested.atDay(1);

        LocalDate nextMonth =
                requested.plusMonths(1).atDay(1);

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, access.employeeId());

            statement.setDate(
                    2,
                    java.sql.Date.valueOf(firstDay)
            );

            statement.setDate(
                    3,
                    java.sql.Date.valueOf(nextMonth)
            );

            statement.setTimestamp(
                    4,
                    Timestamp.valueOf(nextMonth.atStartOfDay())
            );

            statement.setTimestamp(
                    5,
                    Timestamp.valueOf(firstDay.atStartOfDay())
            );

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    java.sql.Date storedDate =
                            rows.getDate("data_zi");

                    Timestamp start =
                            rows.getTimestamp("inceput");

                    Timestamp end =
                            rows.getTimestamp("sfarsit");

                    String text =
                            "#"
                                    + rows.getInt("id_sesiune")
                                    + " · "
                                    + rows.getString("denumire");

                    for (
                            int number = 1;
                            number <= requested.lengthOfMonth();
                            number++
                    ) {
                        LocalDate date =
                                requested.atDay(number);

                        boolean matches =
                                storedDate != null
                                        && storedDate.toLocalDate()
                                        .equals(date);

                        if (start != null && end != null) {
                            matches |=
                                    start.toLocalDateTime()
                                            .isBefore(
                                                    date.plusDays(1)
                                                            .atStartOfDay()
                                            )
                                            && end.toLocalDateTime()
                                            .isAfter(
                                                    date.atStartOfDay()
                                            );
                        }

                        if (matches) {
                            List<String> sessions =
                                    result.computeIfAbsent(
                                            date,
                                            key -> new ArrayList<>()
                                    );

                            if (!sessions.contains(text)) {
                                sessions.add(text);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private void render() {
        monthLabel.setText(
                month.format(
                        DateTimeFormatter.ofPattern(
                                "LLLL yyyy",
                                Locale.forLanguageTag("ro-RO")
                        )
                )
        );

        grid.removeAll();

        for (
                String name
                : new String[]{
                "Lu", "Ma", "Mi", "Jo", "Vi", "Sâ", "Du"
        }
        ) {
            JLabel label =
                    UiStyle.label(
                            name,
                            13,
                            true,
                            UiStyle.MUTED
                    );

            label.setHorizontalAlignment(
                    SwingConstants.CENTER
            );

            grid.add(label);
        }

        int offset =
                month.atDay(1).getDayOfWeek().getValue() - 1;

        int cells =
                ((offset + month.lengthOfMonth() + 6) / 7) * 7;

        for (int index = 0; index < cells; index++) {
            int number = index - offset + 1;

            if (
                    number < 1
                            || number > month.lengthOfMonth()
            ) {
                grid.add(new JLabel());
                continue;
            }

            LocalDate date = month.atDay(number);

            int state =
                    states.getOrDefault(date, NEUTRAL);

            DayButton button =
                    new DayButton(number, state);

            button.setEnabled(!busy);

            button.setToolTipText(
                    date.format(DATE_FORMAT)
                            + " — "
                            + reasons.getOrDefault(
                            date,
                            "Selectează ziua."
                    )
            );

            button.addActionListener(
                    event -> openDay(date)
            );

            grid.add(button);
        }

        grid.revalidate();
        grid.repaint();
    }

    private void openDay(LocalDate date) {
        if (busy) {
            return;
        }

        List<String> sessions = courses.get(date);

        if (sessions != null && !sessions.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    date.format(DATE_FORMAT)
                            + "\n"
                            + String.join("\n", sessions)
                            + "\n\n"
                            + "Ziua este blocată integral pentru salon.\n"
                            + "Gestionează sesiunea din Cursurile mele.",
                    "Zi de curs",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        if (!access.technician()) {
            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Deschizi Cursurile mele pentru a propune "
                            + "o sesiune și a alege zilele în calendar?",
                    "Cursuri",
                    JOptionPane.YES_NO_OPTION
            );

            if (answer == JOptionPane.YES_OPTION) {
                openCourses.run();
            }

            return;
        }

        setBusy(true);

        new SwingWorker<DayConfiguration, Void>() {

            @Override
            protected DayConfiguration doInBackground()
                    throws Exception {

                if (
                        !availability.loadCourseIntervals(
                                access.employeeId(),
                                date
                        ).isEmpty()
                ) {
                    throw new SQLException(
                            "Între timp a fost adăugat un curs. "
                                    + "Reîncarcă calendarul."
                    );
                }

                return days.loadDay(
                        access.employeeId(),
                        date
                );
            }

            @Override
            protected void done() {
                try {
                    DayConfiguration configuration = get();

                    setBusy(false);

                    TechnicianDayDialog dialog =
                            new TechnicianDayDialog(
                                    SwingUtilities.getWindowAncestor(
                                            EmployeeCalendarPanel.this
                                    ),
                                    access.employeeId(),
                                    days,
                                    availability,
                                    configuration
                            );

                    dialog.setVisible(true);

                    refreshData();

                } catch (Exception exception) {
                    setBusy(false);

                    exception.printStackTrace();

                    status.setForeground(UiStyle.RED);
                    status.setText(message(exception));
                }
            }

        }.execute();
    }

    private void setBusy(boolean value) {
        busy = value;

        previous.setEnabled(!value);
        next.setEnabled(!value);
        refresh.setEnabled(!value);

        render();
    }

    private static String message(Throwable exception) {
        Throwable cause = exception;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        return cause.getMessage() == null
                ? "Operația nu a putut fi efectuată."
                : cause.getMessage();
    }

    private static class DayButton extends JButton {

        private final int number;
        private final int state;

        DayButton(int number, int state) {
            super(String.valueOf(number));

            this.number = number;
            this.state = state;

            setPreferredSize(new Dimension(100, 70));
            setMinimumSize(new Dimension(45, 55));

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g =
                    (Graphics2D) graphics.create();

            try {
                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g.setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                );

                int width = getWidth();
                int height = getHeight();

                Color background =
                        state == COURSE
                                ? UiStyle.BEIGE
                                : UiStyle.FIELD;

                g.setColor(background);

                g.fillRoundRect(
                        2,
                        2,
                        width - 5,
                        height - 5,
                        18,
                        18
                );

                if (getModel().isRollover() && isEnabled()) {
                    g.setColor(new Color(0, 0, 0, 9));

                    g.fillRoundRect(
                            2,
                            2,
                            width - 5,
                            height - 5,
                            18,
                            18
                    );
                }

                g.setColor(UiStyle.BORDER);

                g.setStroke(new BasicStroke(1f));

                g.drawRoundRect(
                        2,
                        2,
                        width - 5,
                        height - 5,
                        18,
                        18
                );

                g.setColor(
                        isEnabled()
                                ? UiStyle.INK
                                : UiStyle.MUTED
                );

                g.setFont(
                        new Font(
                                "SansSerif",
                                Font.BOLD,
                                16
                        )
                );

                String text = String.valueOf(number);
                FontMetrics metrics = g.getFontMetrics();

                int baseline =
                        (height - metrics.getHeight()) / 2
                                + metrics.getAscent();

                if (state == COURSE) {
                    baseline -= 9;
                }

                g.drawString(
                        text,
                        (width - metrics.stringWidth(text)) / 2,
                        baseline
                );

                if (state == COURSE) {
                    g.setFont(
                            new Font(
                                    "SansSerif",
                                    Font.BOLD,
                                    11
                            )
                    );

                    FontMetrics courseMetrics =
                            g.getFontMetrics();

                    g.drawString(
                            "CURS",
                            (
                                    width
                                            - courseMetrics.stringWidth("CURS")
                            ) / 2,
                            baseline + 19
                    );
                }

                if (
                        state == AVAILABLE
                                || state == UNAVAILABLE
                ) {
                    g.setColor(
                            state == AVAILABLE
                                    ? UiStyle.GREEN
                                    : UiStyle.RED
                    );

                    int markerWidth =
                            Math.min(
                                    52,
                                    Math.max(12, width - 24)
                            );

                    g.fillRoundRect(
                            (width - markerWidth) / 2,
                            height - 14,
                            markerWidth,
                            5,
                            5,
                            5
                    );
                }

                if (hasFocus()) {
                    g.setColor(UiStyle.MUTED);

                    g.drawRoundRect(
                            6,
                            6,
                            width - 13,
                            height - 13,
                            14,
                            14
                    );
                }

            } finally {
                g.dispose();
            }
        }
    }
}