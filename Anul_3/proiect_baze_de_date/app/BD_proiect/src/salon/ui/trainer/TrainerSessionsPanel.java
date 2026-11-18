package salon.ui.trainer;

import salon.db.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TrainerSessionsPanel extends JPanel {

    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color INK = new Color(30, 30, 30);
    private static final Color MUTED = new Color(115, 110, 103);
    private static final Color WHITE = Color.WHITE;
    private static final Color FIELD = new Color(250, 249, 247);
    private static final Color BORDER = new Color(225, 220, 213);
    private static final Color GREEN = new Color(88, 150, 105);
    private static final Color RED = new Color(160, 45, 45);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final String userEmail;
    private final Runnable showHome;

    private final JPanel sessionsPanel = new JPanel();
    private final JLabel statusLabel = new JLabel(" ");

    private Integer trainerId;

    public TrainerSessionsPanel(
            String userEmail,
            Runnable showHome
    ) {
        this.userEmail = userEmail;
        this.showHome = showHome;

        setLayout(new BorderLayout(24, 0));
        setBackground(CREAM);
        setBorder(
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        );

        trainerId = findTrainerId();

        add(createHeader(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        loadSessions();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JButton backButton = createSecondaryButton("Înapoi");
        backButton.addActionListener(e -> showHome.run());

        JLabel title = createLabel(
                "Sesiuni de curs", 24, true, INK
        );

        JButton createButton = createPrimaryButton(
                "Creează sesiune"
        );
        createButton.addActionListener(e -> openCreateDialog());

        header.add(backButton, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);
        header.add(createButton, BorderLayout.EAST);

        return header;
    }

    private JComponent createCenter() {
        sessionsPanel.setLayout(
                new BoxLayout(sessionsPanel, BoxLayout.Y_AXIS)
        );
        sessionsPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(sessionsPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CREAM);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        return scroll;
    }

    private Integer findTrainerId() {
        String sql = """
                SELECT u.id_angajat
                FROM utilizator u
                JOIN trainer t
                    ON t.id_angajat = u.id_angajat
                WHERE u.email = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, userEmail);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("id_angajat");
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    private void loadSessions() {
        sessionsPanel.removeAll();

        if (trainerId == null) {
            sessionsPanel.add(createLabel(
                    "Contul nu este asociat unui trainer.",
                    13, false, RED
            ));

            sessionsPanel.revalidate();
            sessionsPanel.repaint();
            return;
        }

        String sql = """
                SELECT
                    s.id_sesiune,
                    c.denumire,
                    s.pret,
                    s.nr_maxim_cursanti,
                    s.stare,
                    MIN(i.inceput) AS prima_zi,
                    MAX(i.sfarsit) AS ultima_zi,
                    COUNT(i.id_interval) AS nr_zile
                FROM sesiune_curs s
                JOIN curs c
                    ON c.id_curs = s.id_curs
                JOIN interval_sesiune i
                    ON i.id_sesiune = s.id_sesiune
                WHERE s.id_trainer = ?
                GROUP BY
                    s.id_sesiune,
                    c.denumire,
                    s.pret,
                    s.nr_maxim_cursanti,
                    s.stare
                ORDER BY prima_zi DESC
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, trainerId);

            try (ResultSet result = statement.executeQuery()) {
                boolean hasSessions = false;

                while (result.next()) {
                    hasSessions = true;

                    sessionsPanel.add(createSessionCard(
                            result.getInt("id_sesiune"),
                            result.getString("denumire"),
                            result.getBigDecimal("pret"),
                            result.getInt("nr_maxim_cursanti"),
                            result.getString("stare"),
                            result.getTimestamp("prima_zi")
                                    .toLocalDateTime()
                                    .toLocalDate(),
                            result.getInt("nr_zile")
                    ));

                    sessionsPanel.add(
                            Box.createVerticalStrut(12)
                    );
                }

                if (!hasSessions) {
                    sessionsPanel.add(createLabel(
                            "Nu ai creat încă nicio sesiune.",
                            13, false, MUTED
                    ));
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            showError("Sesiunile nu au putut fi încărcate.");
        }

        sessionsPanel.revalidate();
        sessionsPanel.repaint();
    }

    private JPanel createSessionCard(
            int sessionId,
            String courseName,
            BigDecimal price,
            int capacity,
            String status,
            LocalDate firstDay,
            int dayCount
    ) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(FIELD);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true),
                        BorderFactory.createEmptyBorder(
                                16, 18, 16, 18
                        )
                )
        );
        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 130)
        );
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel title = createLabel(courseName, 16, true, INK);
        JLabel details = createLabel(
                firstDay.format(DATE_FORMAT)
                        + "  •  " + dayCount + " zi(le)  •  "
                        + price.stripTrailingZeros().toPlainString()
                        + " RON  •  " + capacity + " locuri",
                13, false, MUTED
        );
        JLabel sessionStatusLabel = createLabel(
                status, 13, true,
                "Anulata".equals(status) ? RED : GREEN
        );

        info.add(title);
        info.add(Box.createVerticalStrut(6));
        info.add(details);
        info.add(Box.createVerticalStrut(4));
        info.add(sessionStatusLabel);

        JButton cancelButton = createSecondaryButton("Anulează");

        if ("Anulata".equals(status)) {
            cancelButton.setEnabled(false);
        } else {
            cancelButton.addActionListener(e ->
                    cancelSession(sessionId)
            );
        }

        card.add(info, BorderLayout.CENTER);
        card.add(cancelButton, BorderLayout.EAST);

        return card;
    }

    private void cancelSession(int sessionId) {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Anulezi această sesiune?",
                "Anulare sesiune",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = """
                UPDATE sesiune_curs
                SET stare = 'Anulata'
                WHERE id_sesiune = ?
                  AND id_trainer = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, sessionId);
            statement.setInt(2, trainerId);
            statement.executeUpdate();

            loadSessions();
            showSuccess("Sesiunea a fost anulată.");

        } catch (SQLException exception) {
            exception.printStackTrace();
            showError("Sesiunea nu a putut fi anulată.");
        }
    }

    private void openCreateDialog() {
        if (trainerId == null) {
            showError("Contul nu este asociat unui trainer.");
            return;
        }

        List<CourseItem> courses = loadCourses();

        if (courses.isEmpty()) {
            showError(
                    "Nu există cursuri. Creează mai întâi un curs."
            );
            return;
        }

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Creează sesiune de curs",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE
        );
        dialog.setSize(520, 580);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(
                BorderFactory.createEmptyBorder(26, 30, 26, 30)
        );

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JComboBox<CourseItem> courseBox =
                new JComboBox<>(courses.toArray(new CourseItem[0]));

        JTextField startDateField = new JTextField(
                LocalDate.now().plusDays(1)
                        .format(DateTimeFormatter
                                .ofPattern("yyyy-MM-dd"))
        );

        JTextField startTimeField = new JTextField("09:00");
        JTextField endTimeField = new JTextField("16:00");
        JTextField priceField = new JTextField("0");
        JTextField capacityField = new JTextField("8");

        styleField(startDateField);
        styleField(startTimeField);
        styleField(endTimeField);
        styleField(priceField);
        styleField(capacityField);

        JLabel message = createLabel(" ", 12, false, MUTED);

        addFormRow(form, c, 0,
                createLabel("Curs", 13, false, INK));
        addFormRow(form, c, 1, courseBox);

        addFormRow(form, c, 2,
                createLabel("Data de început (yyyy-MM-dd)",
                        13, false, INK));
        addFormRow(form, c, 3, startDateField);

        addFormRow(form, c, 4,
                createLabel("Ora de început", 13, false, INK));
        addFormRow(form, c, 5, startTimeField);

        addFormRow(form, c, 6,
                createLabel("Ora de sfârșit", 13, false, INK));
        addFormRow(form, c, 7, endTimeField);

        addFormRow(form, c, 8,
                createLabel("Preț (RON)", 13, false, INK));
        addFormRow(form, c, 9, priceField);

        addFormRow(form, c, 10,
                createLabel("Număr maxim de cursanți",
                        13, false, INK));
        addFormRow(form, c, 11, capacityField);

        JButton saveButton = createPrimaryButton(
                "Salvează sesiunea"
        );
        addFormRow(form, c, 12, saveButton);
        addFormRow(form, c, 13, message);

        saveButton.addActionListener(e -> {
            try {
                CourseItem course =
                        (CourseItem) courseBox.getSelectedItem();

                LocalDate startDate = LocalDate.parse(
                        startDateField.getText().trim()
                );

                LocalTime startTime = LocalTime.parse(
                        startTimeField.getText().trim()
                );

                LocalTime endTime = LocalTime.parse(
                        endTimeField.getText().trim()
                );

                BigDecimal price = new BigDecimal(
                        priceField.getText().trim()
                );

                int capacity = Integer.parseInt(
                        capacityField.getText().trim()
                );

                if (course == null) {
                    throw new IllegalArgumentException(
                            "Selectează cursul."
                    );
                }

                if (!endTime.isAfter(startTime)) {
                    throw new IllegalArgumentException(
                            "Ora de sfârșit trebuie să fie după ora de început."
                    );
                }

                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException(
                            "Prețul nu poate fi negativ."
                    );
                }

                if (capacity < 1 || capacity > 8) {
                    throw new IllegalArgumentException(
                            "Numărul de cursanți trebuie să fie între 1 și 8."
                    );
                }

                createSession(
                        course.id(),
                        startDate,
                        startTime,
                        endTime,
                        price,
                        capacity
                );

                dialog.dispose();
                loadSessions();
                showSuccess("Sesiunea a fost creată.");

            } catch (Exception exception) {
                message.setForeground(RED);
                message.setText(
                        exception.getMessage() == null
                                ? "Verifică datele introduse."
                                : exception.getMessage()
                );
            }
        });

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    private List<CourseItem> loadCourses() {
        List<CourseItem> courses = new ArrayList<>();

        String sql = """
                SELECT
                    id_curs,
                    denumire,
                    nivel,
                    nr_zile,
                    durata_zilnica_min
                FROM curs
                ORDER BY denumire
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                courses.add(new CourseItem(
                        result.getInt("id_curs"),
                        result.getString("denumire"),
                        result.getString("nivel"),
                        result.getInt("nr_zile"),
                        result.getInt("durata_zilnica_min")
                ));
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return courses;
    }

    private void createSession(
            int courseId,
            LocalDate startDate,
            LocalTime startTime,
            LocalTime endTime,
            BigDecimal price,
            int capacity
    ) throws SQLException {

        CourseItem course = findCourse(courseId);

        if (course == null) {
            throw new SQLException("Cursul nu mai există.");
        }

        int days = course.days();
        int dailyDuration = course.dailyDuration();

        long requestedMinutes = Duration
                .between(startTime, endTime)
                .toMinutes();

        if (requestedMinutes < dailyDuration) {
            throw new IllegalArgumentException(
                    "Intervalul zilnic este prea scurt. "
                            + "Cursul necesită minimum "
                            + dailyDuration + " minute pe zi."
            );
        }

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                int sessionId;

                String insertSession = """
                        INSERT INTO sesiune_curs
                            (
                                id_curs,
                                id_trainer,
                                pret,
                                nr_maxim_cursanti,
                                stare
                            )
                        VALUES (?, ?, ?, ?, 'Planificata')
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        insertSession,
                                        Statement.RETURN_GENERATED_KEYS
                                )
                ) {
                    statement.setInt(1, courseId);
                    statement.setInt(2, trainerId);
                    statement.setBigDecimal(3, price);
                    statement.setInt(4, capacity);
                    statement.executeUpdate();

                    try (ResultSet keys =
                                 statement.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                    "Nu s-a obținut ID-ul sesiunii."
                            );
                        }

                        sessionId = keys.getInt(1);
                    }
                }

                String insertInterval = """
                        INSERT INTO interval_sesiune
                            (id_sesiune, inceput, sfarsit)
                        VALUES (?, ?, ?)
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        insertInterval
                                )
                ) {
                    for (int i = 0; i < days; i++) {
                        LocalDate day = startDate.plusDays(i);

                        statement.setInt(1, sessionId);
                        statement.setTimestamp(2, Timestamp.valueOf(
                                day.atTime(startTime)
                        ));
                        statement.setTimestamp(3, Timestamp.valueOf(
                                day.atTime(endTime)
                        ));

                        statement.addBatch();
                    }

                    statement.executeBatch();
                }

                connection.commit();

            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private CourseItem findCourse(int courseId) throws SQLException {
        String sql = """
                SELECT
                    id_curs,
                    denumire,
                    nivel,
                    nr_zile,
                    durata_zilnica_min
                FROM curs
                WHERE id_curs = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, courseId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return new CourseItem(
                        result.getInt("id_curs"),
                        result.getString("denumire"),
                        result.getString("nivel"),
                        result.getInt("nr_zile"),
                        result.getInt("durata_zilnica_min")
                );
            }
        }
    }

    private void styleField(JTextField field) {
        field.setPreferredSize(new Dimension(320, 42));
        field.setBackground(FIELD);
        field.setForeground(INK);
        field.setCaretColor(INK);
        field.setMargin(new Insets(8, 12, 8, 12));
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints c,
            int row,
            JComponent component
    ) {
        c.gridy = row;
        c.insets = new Insets(0, 0, 10, 0);
        panel.add(component, c);
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(INK);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setPreferredSize(new Dimension(200, 42));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setBackground(FIELD);
        button.setForeground(INK);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setPreferredSize(new Dimension(110, 38));
        return button;
    }

    private JLabel createLabel(
            String text,
            int size,
            boolean bold,
            Color color
    ) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(
                "SansSerif",
                bold ? Font.BOLD : Font.PLAIN,
                size
        ));
        label.setForeground(color);
        return label;
    }

    private void showError(String message) {
        statusLabel.setForeground(RED);
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        statusLabel.setForeground(GREEN);
        statusLabel.setText(message);
    }

    private record CourseItem(
            int id,
            String name,
            String level,
            int days,
            int dailyDuration
    ) {
        @Override
        public String toString() {
            return name + " — " + days + " zi(le)";
        }
    }
}