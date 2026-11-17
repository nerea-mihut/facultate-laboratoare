package salon.ui;

import salon.db.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class LoginFrame extends JFrame {

    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color INK = new Color(30, 30, 30);
    private static final Color MUTED = new Color(115, 110, 103);
    private static final Color FIELD_BACKGROUND =
            new Color(250, 249, 247);

    private final JTextField emailField = new JTextField(24);
    private final JPasswordField passwordField =
            new JPasswordField(24);

    private final JButton loginButton =
            new JButton("Autentificare");

    private final JButton createAccountButton =
            new JButton("Creează cont");

    private final JLabel statusLabel = new JLabel(" ");

    public LoginFrame() {
        setTitle("Salon & Academie — Autentificare");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(560, 660));
        setSize(680, 740);
        setLocationRelativeTo(null);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(CREAM);
        background.setBorder(
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        );

        setContentPane(background);

        JPanel card = createRoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(
                BorderFactory.createEmptyBorder(40, 42, 40, 42)
        );

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel brand = new JLabel("SALON & ACADEMIE");
        brand.setFont(new Font("SansSerif", Font.BOLD, 12));
        brand.setForeground(MUTED);
        addRow(card, brand, c, 0, 0, 26);

        JLabel heading = new JLabel("Bine ai revenit.");
        heading.setFont(new Font("SansSerif", Font.BOLD, 30));
        heading.setForeground(INK);
        addRow(card, heading, c, 1, 0, 10);

        JLabel subtitle = new JLabel(
                "Autentifică-te pentru a continua."
        );
        subtitle.setForeground(MUTED);
        addRow(card, subtitle, c, 2, 0, 32);

        JLabel emailLabel = new JLabel("Adresă de e-mail");
        emailLabel.setForeground(INK);
        emailLabel.setLabelFor(emailField);
        addRow(card, emailLabel, c, 3, 0, 8);

        configureField(emailField);
        emailField.putClientProperty(
                "JTextField.placeholderText",
                "Introdu adresa de e-mail"
        );
        addRow(card, emailField, c, 4, 0, 22);

        JLabel passwordLabel = new JLabel("Parolă");
        passwordLabel.setForeground(INK);
        passwordLabel.setLabelFor(passwordField);
        addRow(card, passwordLabel, c, 5, 0, 8);

        configureField(passwordField);
        passwordField.putClientProperty(
                "JTextField.placeholderText",
                "Introdu parola"
        );
        addRow(card, passwordField, c, 6, 0, 14);

        char originalEchoChar = passwordField.getEchoChar();

        JCheckBox showPassword = new JCheckBox("Arată parola");
        showPassword.setOpaque(false);
        showPassword.setForeground(MUTED);
        showPassword.addActionListener(e ->
                passwordField.setEchoChar(
                        showPassword.isSelected()
                                ? (char) 0
                                : originalEchoChar
                )
        );
        addRow(card, showPassword, c, 7, 0, 22);

        loginButton.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        loginButton.setBackground(INK);
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(340, 48));
        loginButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        loginButton.addActionListener(e -> login());
        addRow(card, loginButton, c, 8, 0, 12);

        statusLabel.setForeground(MUTED);
        statusLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
        addRow(card, statusLabel, c, 9, 0, 14);

        createAccountButton.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );
        createAccountButton.setForeground(INK);
        createAccountButton.setContentAreaFilled(false);
        createAccountButton.setBorderPainted(false);
        createAccountButton.setFocusPainted(false);
        createAccountButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        createAccountButton.setText(
                "Nu ai cont? Creează cont"
        );
        createAccountButton.addActionListener(e ->
                openRegisterFrame()
        );
        addRow(card, createAccountButton, c, 10, 0, 0);

        background.add(card);

        getRootPane().setDefaultButton(loginButton);

        setVisible(true);
        SwingUtilities.invokeLater(emailField::requestFocusInWindow);
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

                g.setColor(Color.WHITE);
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

    private void configureField(JTextField field) {
        field.setPreferredSize(new Dimension(340, 46));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(INK);
        field.setCaretColor(INK);
        field.setMargin(new Insets(10, 14, 10, 14));
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

    private void openRegisterFrame() {
        RegisterFrame registerFrame = new RegisterFrame();
        registerFrame.setVisible(true);

        dispose();
    }

    private void login() {
        String email = emailField.getText().trim();
        char[] password = passwordField.getPassword();

        if (email.isEmpty() || password.length == 0) {
            Arrays.fill(password, '\0');

            statusLabel.setForeground(new Color(160, 45, 45));
            statusLabel.setText("Completează e-mailul și parola.");
            return;
        }

        setBusy(true);
        statusLabel.setForeground(MUTED);
        statusLabel.setText("Se verifică datele...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {

            @Override
            protected String doInBackground() throws Exception {
                try {
                    return authenticate(email, password);
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                setBusy(false);

                try {
                    String role = get();

                    if (role == null) {
                        statusLabel.setForeground(
                                new Color(160, 45, 45)
                        );
                        statusLabel.setText(
                                "E-mail sau parolă incorectă."
                        );

                        passwordField.setText("");
                        passwordField.requestFocusInWindow();
                        return;
                    }

                    passwordField.setText("");
                    statusLabel.setForeground(
                            new Color(55, 110, 70)
                    );
                    statusLabel.setText(
                            "Autentificare reușită."
                    );

                    openWindowForRole(role, email);

                } catch (Exception exception) {
                    statusLabel.setForeground(
                            new Color(160, 45, 45)
                    );
                    statusLabel.setText(
                            "Autentificarea nu a putut fi finalizată."
                    );

                    Throwable cause = exception.getCause();

                    if (cause instanceof SQLException) {
                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "Nu s-a putut verifica autentificarea.\n"
                                        + "Verifică serverul MariaDB și "
                                        + "configurarea conexiunii.",
                                "Eroare de conectare",
                                JOptionPane.ERROR_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "A apărut o eroare la autentificare.",
                                "Eroare",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                    exception.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private String authenticate(
            String email,
            char[] password
    ) throws Exception {

        String sql = """
                SELECT rol
                FROM utilizator
                WHERE email = ? AND parola = ?
                """;

        String hashedPassword = hashPassword(password);

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);
            statement.setString(2, hashedPassword);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("rol");
                }

                return null;
            }
        }
    }

    private void setBusy(boolean busy) {
        loginButton.setEnabled(!busy);
        emailField.setEnabled(!busy);
        passwordField.setEnabled(!busy);

        loginButton.setText(
                busy ? "Se conectează..." : "Autentificare"
        );

        setCursor(
                Cursor.getPredefinedCursor(
                        busy
                                ? Cursor.WAIT_CURSOR
                                : Cursor.DEFAULT_CURSOR
                )
        );
    }

    private void openWindowForRole(
            String role,
            String email
    ) {
        if ("Client".equals(role)) {
            ClientDashboard clientDashboard =
                    new ClientDashboard(email);

            clientDashboard.setVisible(true);
            dispose();

            return;
        }

        if ("Tehnician".equals(role)) {
            TechnicianDashboard technicianDashboard =
                    new TechnicianDashboard(email);

            technicianDashboard.setVisible(true);
            dispose();

            return;
        }

        if ("Trainer".equals(role)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Interfața trainerului va fi implementată "
                            + "în pasul următor.",
                    "Autentificare reușită",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Bine ai venit!\nRolul contului: " + role
                        + "\n\nInterfața acestui rol urmează "
                        + "să fie implementată.",
                "Autentificare reușită",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Compatibilitate temporară cu exemplul anterior.
    // Se va înlocui înainte de utilizarea unor parole reale.
    private String hashPassword(char[] password) throws Exception {
        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] passwordBytes =
                new String(password).getBytes(StandardCharsets.UTF_8);

        byte[] hashedBytes;

        try {
            hashedBytes = digest.digest(passwordBytes);
        } finally {
            Arrays.fill(passwordBytes, (byte) 0);
        }

        StringBuilder builder = new StringBuilder();

        for (byte value : hashedBytes) {
            builder.append(String.format("%02x", value & 0xff));
        }

        return builder.toString();
    }
}