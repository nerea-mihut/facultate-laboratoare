package salon.ui.trainer;

import salon.db.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TrainerCoursesPanel extends JPanel {

    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color INK = new Color(30, 30, 30);
    private static final Color MUTED = new Color(115, 110, 103);
    private static final Color WHITE = Color.WHITE;
    private static final Color FIELD = new Color(250, 249, 247);
    private static final Color BORDER = new Color(225, 220, 213);

    private final String userEmail;
    private final Runnable showHome;

    private final JPanel coursesPanel = new JPanel();
    private final JLabel statusLabel = new JLabel(" ");

    public TrainerCoursesPanel(
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

        add(createLeftPanel(), BorderLayout.WEST);
        add(createRightPanel(), BorderLayout.CENTER);

        loadCourses();
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(300, 0));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JButton backButton = createSecondaryButton("Înapoi");
        backButton.addActionListener(e -> showHome.run());

        JLabel title = createLabel(
                "Cursurile mele", 24, true, INK
        );

        header.add(backButton, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);

        coursesPanel.setLayout(
                new BoxLayout(coursesPanel, BoxLayout.Y_AXIS)
        );
        coursesPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(coursesPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CREAM);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(header, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(18), BorderLayout.CENTER);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = createLabel(
                "Configurare curs", 24, true, INK
        );
        title.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 8, 0)
        );

        JLabel subtitle = createLabel(
                "Selectează un curs pentru a seta numărul de zile "
                        + "și durata zilnică.",
                13, false, MUTED
        );

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(subtitle);

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(MUTED);
        statusLabel.setBorder(
                BorderFactory.createEmptyBorder(14, 0, 0, 0)
        );

        panel.add(top, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadCourses() {
        coursesPanel.removeAll();

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

        List<CourseItem> courses = new ArrayList<>();

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
            showError("Cursurile nu au putut fi încărcate.");
        }

        if (courses.isEmpty()) {
            coursesPanel.add(createLabel(
                    "Nu există cursuri înregistrate.",
                    13, false, MUTED
            ));
        }

        for (CourseItem course : courses) {
            coursesPanel.add(createCourseCard(course));
            coursesPanel.add(Box.createVerticalStrut(12));
        }

        coursesPanel.revalidate();
        coursesPanel.repaint();
    }

    private JPanel createCourseCard(CourseItem course) {
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

        JLabel name = createLabel(
                course.name(), 16, true, INK
        );
        JLabel details = createLabel(
                course.days() + " zi(le)  •  "
                        + course.dailyDuration() + " min/zi  •  "
                        + course.level(),
                13, false, MUTED
        );

        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        details.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(name);
        info.add(Box.createVerticalStrut(6));
        info.add(details);

        JButton editButton = createSecondaryButton("Modifică");
        editButton.addActionListener(e ->
                openCourseDialog(course)
        );

        card.add(info, BorderLayout.CENTER);
        card.add(editButton, BorderLayout.EAST);

        return card;
    }

    private void openCourseDialog(CourseItem course) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Modifică cursul",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE
        );
        dialog.setSize(480, 380);
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

        JTextField daysField = new JTextField(
                String.valueOf(course.days())
        );
        JTextField durationField = new JTextField(
                String.valueOf(course.dailyDuration())
        );

        styleField(daysField);
        styleField(durationField);

        JLabel message = createLabel(" ", 12, false, MUTED);

        addFormRow(form, c, 0,
                createLabel("Nume curs", 13, false, INK));
        addFormRow(form, c, 1,
                createReadOnlyField(course.name()));
        addFormRow(form, c, 2,
                createLabel("Număr de zile", 13, false, INK));
        addFormRow(form, c, 3, daysField);
        addFormRow(form, c, 4,
                createLabel("Durată zilnică (minute)",
                        13, false, INK));
        addFormRow(form, c, 5, durationField);

        JButton saveButton = createPrimaryButton("Salvează");
        addFormRow(form, c, 6, saveButton);
        addFormRow(form, c, 7, message);

        saveButton.addActionListener(e -> {
            try {
                int days = Integer.parseInt(
                        daysField.getText().trim()
                );

                int duration = Integer.parseInt(
                        durationField.getText().trim()
                );

                if (days < 1 || days > 30) {
                    throw new IllegalArgumentException(
                            "Numărul de zile trebuie să fie între 1 și 30."
                    );
                }

                if (duration < 30 || duration > 720) {
                    throw new IllegalArgumentException(
                            "Durata zilnică trebuie să fie între 30 și 720 minute."
                    );
                }

                updateCourse(course.id(), days, duration);

                dialog.dispose();
                loadCourses();

                showSuccess("Cursul a fost actualizat.");

            } catch (Exception exception) {
                message.setForeground(new Color(160, 45, 45));
                message.setText(exception.getMessage());
            }
        });

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    private void updateCourse(
            int courseId,
            int days,
            int dailyDuration
    ) throws SQLException {

        String sql = """
                UPDATE curs
                SET nr_zile = ?,
                    durata_zilnica_min = ?
                WHERE id_curs = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, days);
            statement.setInt(2, dailyDuration);
            statement.setInt(3, courseId);
            statement.executeUpdate();
        }
    }

    private JTextField createReadOnlyField(String value) {
        JTextField field = new JTextField(value);
        field.setEditable(false);
        styleField(field);
        return field;
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
        button.setPreferredSize(new Dimension(220, 44));
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
        statusLabel.setForeground(new Color(160, 45, 45));
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        statusLabel.setForeground(new Color(88, 150, 105));
        statusLabel.setText(message);
    }

    private record CourseItem(
            int id,
            String name,
            String level,
            int days,
            int dailyDuration
    ) {
    }
}