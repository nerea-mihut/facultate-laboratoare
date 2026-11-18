package salon.ui.trainer;

import salon.db.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TrainerAvailabilityPanel extends JPanel {

    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color INK = new Color(30, 30, 30);
    private static final Color MUTED = new Color(115, 110, 103);
    private static final Color WHITE = Color.WHITE;
    private static final Color FIELD = new Color(250, 249, 247);
    private static final Color BORDER = new Color(225, 220, 213);
    private static final Color GREEN = new Color(88, 150, 105);
    private static final Color RED = new Color(160, 45, 45);

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final String userEmail;
    private final Runnable showHome;

    private final JPanel intervalsPanel = new JPanel();
    private final JLabel statusLabel = new JLabel(" ");

    private Integer trainerId;

    public TrainerAvailabilityPanel(
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

        loadIntervals();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JButton backButton = createSecondaryButton("Înapoi");
        backButton.addActionListener(e -> showHome.run());

        JLabel title = createLabel(
                "Disponibilitate", 24, true, INK
        );

        JButton addButton = createPrimaryButton(
                "Adaugă interval"
        );
        addButton.addActionListener(e -> openIntervalDialog(null));

        header.add(backButton, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);
        header.add(addButton, BorderLayout.EAST);

        return header;
    }

    private JComponent createCenter() {
        intervalsPanel.setLayout(
                new BoxLayout(intervalsPanel, BoxLayout.Y_AXIS)
        );
        intervalsPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(intervalsPanel);
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

    private void loadIntervals() {
        intervalsPanel.removeAll();

        if (trainerId == null) {
            intervalsPanel.add(createLabel(
                    "Contul nu este asociat unui trainer.",
                    13, false, RED
            ));

            intervalsPanel.revalidate();
            intervalsPanel.repaint();
            return;
        }

        String sql = """
                SELECT
                    id_interval,
                    inceput,
                    sfarsit,
                    tip_interval
                FROM interval_disponibilitate
                WHERE id_angajat = ?
                ORDER BY inceput
                """;

        List<IntervalItem> intervals = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, trainerId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    intervals.add(new IntervalItem(
                            result.getInt("id_interval"),
                            result.getTimestamp("inceput")
                                    .toLocalDateTime(),
                            result.getTimestamp("sfarsit")
                                    .toLocalDateTime(),
                            result.getString("tip_interval")
                    ));
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            showError("Intervalele nu au putut fi încărcate.");
        }

        if (intervals.isEmpty()) {
            intervalsPanel.add(createLabel(
                    "Nu ai configurat încă intervale de disponibilitate.",
                    13, false, MUTED
            ));
        }

        for (IntervalItem interval : intervals) {
            intervalsPanel.add(createIntervalCard(interval));
            intervalsPanel.add(Box.createVerticalStrut(10));
        }

        intervalsPanel.revalidate();
        intervalsPanel.repaint();
    }

    private JPanel createIntervalCard(IntervalItem interval) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(FIELD);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true),
                        BorderFactory.createEmptyBorder(
                                14, 18, 14, 18
                        )
                )
        );
        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 80)
        );
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        String type = "Lucru".equals(interval.type())
                ? "Lucru"
                : "Pauză / absență";

        JLabel text = createLabel(
                interval.start().format(DATE_TIME_FORMAT)
                        + "  —  "
                        + interval.end().format(DATE_TIME_FORMAT)
                        + "  •  " + type,
                13, false, INK
        );

        JButton deleteButton = createSecondaryButton("Șterge");
        deleteButton.addActionListener(e ->
                deleteInterval(interval.id())
        );

        card.add(text, BorderLayout.CENTER);
        card.add(deleteButton, BorderLayout.EAST);

        return card;
    }

    private void deleteInterval(int intervalId) {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Ștergi acest interval?",
                "Ștergere interval",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

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
            statement.setInt(2, trainerId);
            statement.executeUpdate();

            loadIntervals();
            showSuccess("Intervalul a fost șters.");

        } catch (SQLException exception) {
            exception.printStackTrace();
            showError("Intervalul nu a putut fi șters.");
        }
    }

    private void openIntervalDialog(IntervalItem existing) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                existing == null
                        ? "Adaugă interval"
                        : "Modifică interval",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE
        );
        dialog.setSize(480, 420);
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

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        JTextField startField = new JTextField(
                existing == null
                        ? LocalDateTime.now()
                        .plusDays(1)
                        .withHour(9)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                        .format(formatter)
                        : existing.start().format(formatter)
        );

        JTextField endField = new JTextField(
                existing == null
                        ? LocalDateTime.now()
                        .plusDays(1)
                        .withHour(16)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                        .format(formatter)
                        : existing.end().format(formatter)
        );

        JComboBox<String> typeBox = new JComboBox<>(
                new String[]{"Lucru", "Absenta"}
        );

        if (existing != null) {
            typeBox.setSelectedItem(existing.type());
        }

        styleField(startField);
        styleField(endField);

        JLabel message = createLabel(
                "Format: yyyy-MM-dd HH:mm",
                12, false, MUTED
        );

        addFormRow(form, c, 0,
                createLabel("Început", 13, false, INK));
        addFormRow(form, c, 1, startField);

        addFormRow(form, c, 2,
                createLabel("Sfârșit", 13, false, INK));
        addFormRow(form, c, 3, endField);

        addFormRow(form, c, 4,
                createLabel("Tip interval", 13, false, INK));
        addFormRow(form, c, 5, typeBox);

        JButton saveButton = createPrimaryButton("Salvează");
        addFormRow(form, c, 6, saveButton);
        addFormRow(form, c, 7, message);

        saveButton.addActionListener(e -> {
            try {
                LocalDateTime start = LocalDateTime.parse(
                        startField.getText().trim(),
                        formatter
                );

                LocalDateTime end = LocalDateTime.parse(
                        endField.getText().trim(),
                        formatter
                );

                String type = (String) typeBox.getSelectedItem();

                if (!end.isAfter(start)) {
                    throw new IllegalArgumentException(
                            "Sfârșitul trebuie să fie după început."
                    );
                }

                saveInterval(
                        existing == null ? null : existing.id(),
                        start,
                        end,
                        type
                );

                dialog.dispose();
                loadIntervals();
                showSuccess("Intervalul a fost salvat.");

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

    private void saveInterval(
            Integer intervalId,
            LocalDateTime start,
            LocalDateTime end,
            String type
    ) throws SQLException {

        String sql = intervalId == null
                ? """
                INSERT INTO interval_disponibilitate
                    (id_angajat, inceput, sfarsit, tip_interval)
                VALUES (?, ?, ?, ?)
                """
                : """
                UPDATE interval_disponibilitate
                SET inceput = ?,
                    sfarsit = ?,
                    tip_interval = ?
                WHERE id_interval = ?
                  AND id_angajat = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            if (intervalId == null) {
                statement.setInt(1, trainerId);
                statement.setTimestamp(2, Timestamp.valueOf(start));
                statement.setTimestamp(3, Timestamp.valueOf(end));
                statement.setString(4, type);
            } else {
                statement.setTimestamp(1, Timestamp.valueOf(start));
                statement.setTimestamp(2, Timestamp.valueOf(end));
                statement.setString(3, type);
                statement.setInt(4, intervalId);
                statement.setInt(5, trainerId);
            }

            statement.executeUpdate();
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

    private record IntervalItem(
            int id,
            LocalDateTime start,
            LocalDateTime end,
            String type
    ) {
    }
}