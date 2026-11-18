package salon.ui.trainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TrainerHomePanel extends JPanel {

    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color INK = new Color(30, 30, 30);
    private static final Color MUTED = new Color(115, 110, 103);
    private static final Color WHITE = Color.WHITE;

    private final String userEmail;

    public TrainerHomePanel(
            String userEmail,
            Runnable openCourses,
            Runnable openSessions,
            Runnable openAvailability,
            Runnable openProfile
    ) {
        this.userEmail = userEmail;

        setLayout(new BorderLayout());
        setBackground(CREAM);
        setBorder(
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        );

        add(createWelcomeCard(
                openCourses,
                openSessions,
                openAvailability,
                openProfile
        ), BorderLayout.CENTER);
    }

    private JPanel createWelcomeCard(
            Runnable openCourses,
            Runnable openSessions,
            Runnable openAvailability,
            Runnable openProfile
    ) {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(CREAM);
        background.setBorder(
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        );

        JPanel card = createRoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(
                BorderFactory.createEmptyBorder(40, 46, 40, 46)
        );
        card.setPreferredSize(new Dimension(560, 470));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        String firstName = findFirstName();

        String greeting = "Bine ai venit!";

        if (firstName != null && !firstName.isBlank()) {
            greeting = "Bine ai venit, " + firstName + "!";
        }

        JLabel heading = new JLabel(greeting);
        heading.setFont(new Font("SansSerif", Font.BOLD, 30));
        heading.setForeground(INK);

        JLabel subtitle = new JLabel(
                "Gestionează cursurile, sesiunile și disponibilitatea ta."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        addRow(card, heading, c, 0, 0, 8);
        addRow(card, subtitle, c, 1, 0, 32);

        addRow(
                card,
                createMenuButton(
                        "Cursurile mele",
                        openCourses
                ),
                c, 2, 0, 16
        );

        addRow(
                card,
                createMenuButton(
                        "Sesiuni de curs",
                        openSessions
                ),
                c, 3, 0, 16
        );

        addRow(
                card,
                createMenuButton(
                        "Disponibilitate",
                        openAvailability
                ),
                c, 4, 0, 16
        );

        addRow(
                card,
                createMenuButton(
                        "Profilul meu",
                        openProfile
                ),
                c, 5, 0, 0
        );

        background.add(card);
        return background;
    }

    private String findFirstName() {
        String sql = """
                SELECT a.prenume
                FROM utilizator u
                JOIN angajat a
                    ON a.id_angajat = u.id_angajat
                WHERE u.email = ?
                """;

        try (
                java.sql.Connection connection =
                        salon.db.DatabaseConnection.getConnection();
                java.sql.PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, userEmail);

            try (java.sql.ResultSet result =
                         statement.executeQuery()) {

                if (result.next()) {
                    return result.getString("prenume");
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    private JButton createMenuButton(
            String text,
            Runnable action
    ) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(INK);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setPreferredSize(new Dimension(420, 48));
        button.addActionListener(e -> action.run());
        return button;
    }

    private void addRow(
            JPanel panel,
            JComponent component,
            GridBagConstraints constraints,
            int row,
            int top,
            int bottom
    ) {
        constraints.gridy = row;
        constraints.insets = new Insets(top, 0, bottom, 0);
        panel.add(component, constraints);
    }

    private JPanel createRoundedCard() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);

                Graphics2D g = (Graphics2D) graphics.create();

                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g.setColor(WHITE);
                g.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 30, 30
                );

                g.dispose();
            }
        };

        panel.setOpaque(false);
        return panel;
    }
}