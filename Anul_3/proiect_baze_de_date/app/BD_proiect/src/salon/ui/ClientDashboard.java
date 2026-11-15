package salon.ui;

import salon.db.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientDashboard extends JFrame {

    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color INK = new Color(30, 30, 30);
    private static final Color MUTED = new Color(115, 110, 103);
    private static final Color WHITE = Color.WHITE;

    private final String userEmail;
    private final String clientFirstName;

    public ClientDashboard(String userEmail) {
        this.userEmail = userEmail;
        this.clientFirstName = findClientFirstName(userEmail);

        setTitle("Salon & Academie — Panou client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 700));
        setSize(1100, 760);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CREAM);

        setContentPane(mainPanel);

        mainPanel.add(createTopBar(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(INK);
        topBar.setBorder(
                BorderFactory.createEmptyBorder(14, 28, 14, 28)
        );

        JLabel brand = new JLabel("SALON & ACADEMIE");
        brand.setFont(new Font("SansSerif", Font.BOLD, 15));
        brand.setForeground(WHITE);

        topBar.add(brand, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(
                FlowLayout.RIGHT,
                10,
                0
        ));

        actions.setOpaque(false);

        JButton profileButton = createTopBarButton(
                "\uD83D\uDC64  Profilul meu"
        );

        profileButton.addActionListener(e -> openProfile());

        JButton activityButton = createTopBarButton(
                "\uD83D\uDCC5  Activitatea mea"
        );

        activityButton.addActionListener(e ->
                showMessage(
                        "Activitatea mea",
                        "Aici vor apărea programările tale, dacă există, "
                                + "și înscrierile la cursuri, dacă există."
                )
        );

        actions.add(profileButton);
        actions.add(activityButton);

        topBar.add(actions, BorderLayout.EAST);

        return topBar;
    }

    private JButton createTopBarButton(String text) {
        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setForeground(WHITE);
        button.setBackground(INK);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    private JPanel createCenterPanel() {
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
        card.setPreferredSize(new Dimension(520, 360));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        String greeting = "Bine ai venit!";

        if (clientFirstName != null && !clientFirstName.isBlank()) {
            greeting = "Bine ai venit, " + clientFirstName + "!";
        }

        JLabel heading = new JLabel(greeting);
        heading.setFont(new Font("SansSerif", Font.BOLD, 30));
        heading.setForeground(INK);
        addRow(card, heading, c, 0, 0, 32);

        JButton appointmentsButton =
                createMenuButton("Programări salon");

        appointmentsButton.addActionListener(e ->
                showMessage(
                        "Programări salon",
                        "Aici vei putea alege tehnicianul, "
                                + "serviciile dorite și un interval disponibil."
                )
        );

        addRow(card, appointmentsButton, c, 1, 0, 16);

        JButton coursesButton =
                createMenuButton("Cursuri academie");

        coursesButton.addActionListener(e ->
                showMessage(
                        "Cursuri academie",
                        "Aici vei putea consulta cursurile și sesiunile "
                                + "disponibile, apoi te vei putea înscrie."
                )
        );

        addRow(card, coursesButton, c, 2, 0, 0);

        background.add(card);

        return background;
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
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        28,
                        28
                );

                g.dispose();
            }
        };

        panel.setOpaque(false);
        return panel;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);

        button.setFont(
                new Font("SansSerif", Font.BOLD, 15)
        );
        button.setBackground(new Color(250, 249, 247));
        button.setForeground(INK);
        button.setPreferredSize(new Dimension(360, 58));
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setFocusPainted(false);

        return button;
    }

    private void addRow(
            JPanel panel,
            Component component,
            GridBagConstraints constraints,
            int row,
            int top,
            int bottom
    ) {
        constraints.gridy = row;
        constraints.insets = new Insets(top, 0, bottom, 0);
        panel.add(component, constraints);
    }

    private String findClientFirstName(String email) {
        String sql = """
                SELECT prenume
                FROM client
                WHERE email = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("prenume");
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    private void openProfile() {
        Object[] options = {"Deconectare", "Închide"};

        int result = JOptionPane.showOptionDialog(
                this,
                "Profilul meu\n\n"
                        + "E-mail: " + userEmail + "\n"
                        + "Prenume: "
                        + (clientFirstName == null
                        || clientFirstName.isBlank()
                        ? "—"
                        : clientFirstName),
                "Profilul meu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (result == 0) {
            openLoginFrame();
        }
    }

    private void showMessage(
            String title,
            String message
    ) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void openLoginFrame() {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);

        dispose();
    }
}