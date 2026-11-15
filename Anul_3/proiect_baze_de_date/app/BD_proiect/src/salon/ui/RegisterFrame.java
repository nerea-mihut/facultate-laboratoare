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

public class RegisterFrame extends JFrame {

    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color INK = new Color(30, 30, 30);
    private static final Color MUTED = new Color(115, 110, 103);
    private static final Color FIELD_BACKGROUND =
            new Color(250, 249, 247);

    private final JTextField firstNameField = new JTextField(24);
    private final JTextField lastNameField = new JTextField(24);
    private final JTextField emailField = new JTextField(24);
    private final JTextField phoneField = new JTextField(24);
    private final JPasswordField passwordField =
            new JPasswordField(24);
    private final JPasswordField confirmPasswordField =
            new JPasswordField(24);

    private final JButton registerButton =
            new JButton("Creează cont");

    private final JButton backButton =
            new JButton("Înapoi la autentificare");

    private final JLabel statusLabel = new JLabel(" ");

    public RegisterFrame() {
        setTitle("Salon & Academie — Creare cont");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(560, 820));
        setSize(680, 900);
        setLocationRelativeTo(null);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(CREAM);
        background.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        setContentPane(background);

        JPanel card = createRoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(
                BorderFactory.createEmptyBorder(32, 42, 32, 42)
        );

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel brand = new JLabel("SALON & ACADEMIE");
        brand.setFont(new Font("SansSerif", Font.BOLD, 12));
        brand.setForeground(MUTED);
        addRow(card, brand, c, 0, 0, 24);

        JLabel heading = new JLabel("Creează un cont.");
        heading.setFont(new Font("SansSerif", Font.BOLD, 30));
        heading.setForeground(INK);
        addRow(card, heading, c, 1, 0, 10);

        JLabel subtitle = new JLabel(
                "Completează datele pentru a te înregistra."
        );
        subtitle.setForeground(MUTED);
        addRow(card, subtitle, c, 2, 0, 28);

        addField(card, c, 3, "Prenume", firstNameField,
                "Introdu prenumele");

        addField(card, c, 5, "Nume", lastNameField,
                "Introdu numele");

        addField(card, c, 7, "Adresă de e-mail", emailField,
                "Introdu adresa de e-mail");

        addField(card, c, 9, "Telefon", phoneField,
                "Introdu numărul de telefon");

        addPasswordField(card, c, 11, "Parolă", passwordField,
                "Introdu parola");

        addPasswordField(card, c, 13, "Confirmă parola",
                confirmPasswordField, "Reintrodu parola");

        JLabel roleInfo = new JLabel(
                "<html><div style='width:330px;'>"
                        + "Rolul contului este stabilit automat în funcție "
                        + "de e-mailul asociat contului tău de angajat. "
                        + "Dacă e-mailul nu aparține unui trainer sau "
                        + "tehnician, vei primi rolul de client."
                        + "</div></html>"
        );
        roleInfo.setForeground(MUTED);
        addRow(card, roleInfo, c, 15, 0, 24);

        registerButton.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        registerButton.setBackground(INK);
        registerButton.setForeground(Color.WHITE);
        registerButton.setPreferredSize(new Dimension(340, 48));
        registerButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        registerButton.addActionListener(e -> register());
        addRow(card, registerButton, c, 16, 0, 12);

        statusLabel.setForeground(MUTED);
        statusLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
        addRow(card, statusLabel, c, 17, 0, 14);

        backButton.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );
        backButton.setForeground(INK);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        backButton.addActionListener(e -> openLoginFrame());
        addRow(card, backButton, c, 18, 0, 0);

        background.add(card);

        getRootPane().setDefaultButton(registerButton);

        setVisible(true);
        SwingUtilities.invokeLater(firstNameField::requestFocusInWindow);
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

    private void configureField(JComponent field) {
        field.setPreferredSize(new Dimension(340, 46));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(INK);
    }

    private void addField(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            JTextField field,
            String placeholder
    ) {
        JLabel label = new JLabel(labelText);
        label.setForeground(INK);
        label.setLabelFor(field);
        addRow(panel, label, constraints, row, 0, 8);

        configureField(field);
        field.putClientProperty(
                "JTextField.placeholderText",
                placeholder
        );
        addRow(panel, field, constraints, row + 1, 0, 18);
    }

    private void addPasswordField(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            JPasswordField field,
            String placeholder
    ) {
        JLabel label = new JLabel(labelText);
        label.setForeground(INK);
        label.setLabelFor(field);
        addRow(panel, label, constraints, row, 0, 8);

        configureField(field);
        field.putClientProperty(
                "JTextField.placeholderText",
                placeholder
        );
        addRow(panel, field, constraints, row + 1, 0, 18);
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

    private void register() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        char[] password = passwordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();

        if (firstName.isEmpty()
                || lastName.isEmpty()
                || email.isEmpty()
                || phone.isEmpty()
                || password.length == 0
                || confirmPassword.length == 0) {

            clearPasswords(password, confirmPassword);

            statusLabel.setForeground(new Color(160, 45, 45));
            statusLabel.setText("Completează toate câmpurile.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            clearPasswords(password, confirmPassword);

            statusLabel.setForeground(new Color(160, 45, 45));
            statusLabel.setText("Introdu o adresă de e-mail validă.");
            return;
        }

        if (!phone.matches("[0-9+\\s-]{6,20}")) {
            clearPasswords(password, confirmPassword);

            statusLabel.setForeground(new Color(160, 45, 45));
            statusLabel.setText(
                    "Numărul de telefon poate conține doar cifre, "
                            + "spații, cratime și prefixul +."
            );
            return;
        }

        if (!Arrays.equals(password, confirmPassword)) {
            clearPasswords(password, confirmPassword);

            statusLabel.setForeground(new Color(160, 45, 45));
            statusLabel.setText("Parolele introduse nu coincid.");
            return;
        }

        setBusy(true);
        statusLabel.setForeground(MUTED);
        statusLabel.setText("Se creează contul...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {

            @Override
            protected String doInBackground() throws Exception {
                try {
                    return createAccount(
                            firstName,
                            lastName,
                            email,
                            phone,
                            password
                    );
                } finally {
                    clearPasswords(password, confirmPassword);
                }
            }

            @Override
            protected void done() {
                setBusy(false);

                try {
                    String role = get();

                    statusLabel.setForeground(new Color(55, 110, 70));
                    statusLabel.setText(
                            "Cont creat cu succes. Rol: " + role + "."
                    );

                    clearFields();

                } catch (Exception exception) {
                    statusLabel.setForeground(new Color(160, 45, 45));
                    statusLabel.setText(
                            "Contul nu a putut fi creat."
                    );

                    Throwable cause = exception.getCause();

                    if (cause instanceof SQLException
                            && cause.getMessage() != null
                            && cause.getMessage().contains("Duplicate")) {

                        statusLabel.setText(
                                "Există deja un cont cu acest e-mail."
                        );
                    } else if (cause instanceof SQLException) {
                        JOptionPane.showMessageDialog(
                                RegisterFrame.this,
                                "Nu s-a putut salva contul.\n"
                                        + "Verifică serverul MariaDB și "
                                        + "configurarea conexiunii.",
                                "Eroare de conectare",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                    exception.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private String createAccount(
            String firstName,
            String lastName,
            String email,
            String phone,
            char[] password
    ) throws Exception {

        String hashedPassword = hashPassword(password);

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                if (accountExists(connection, email)) {
                    connection.rollback();
                    throw new SQLException(
                            "Duplicate entry for email"
                    );
                }

                Integer employeeId = findEmployeeId(connection, email);
                String role;
                Integer clientId = null;

                if (employeeId != null) {
                    role = determineEmployeeRole(connection, employeeId);
                } else {
                    role = "Client";
                    clientId = createClient(
                            connection,
                            firstName,
                            lastName,
                            email,
                            phone
                    );
                }

                String insertUserSql = """
                        INSERT INTO utilizator
                            (email, parola, rol, id_client, id_angajat)
                        VALUES (?, ?, ?, ?, ?)
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(insertUserSql)
                ) {
                    statement.setString(1, email);
                    statement.setString(2, hashedPassword);
                    statement.setString(3, role);
                    statement.setObject(4, clientId);
                    statement.setObject(5, employeeId);

                    statement.executeUpdate();
                }

                connection.commit();
                return role;

            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private Integer findEmployeeId(
            Connection connection,
            String email
    ) throws SQLException {

        String sql = """
                SELECT id_angajat
                FROM angajat
                WHERE email = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id_angajat");
                }

                return null;
            }
        }
    }

    private String determineEmployeeRole(
            Connection connection,
            int employeeId
    ) throws SQLException {

        String sql = """
                SELECT
                    CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM trainer
                            WHERE id_angajat = ?
                        ) THEN 'Trainer'
                        WHEN EXISTS (
                            SELECT 1
                            FROM tehnician
                            WHERE id_angajat = ?
                        ) THEN 'Tehnician'
                        ELSE 'Client'
                    END AS rol
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, employeeId);
            statement.setInt(2, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getString("rol");
            }
        }
    }

    private boolean accountExists(
            Connection connection,
            String email
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM utilizator
                WHERE email = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private Integer createClient(
            Connection connection,
            String firstName,
            String lastName,
            String email,
            String phone
    ) throws SQLException {

        String sql = """
                INSERT INTO client
                    (nume, prenume, telefon, email, alergii)
                VALUES (?, ?, ?, ?, 'Nu')
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                PreparedStatement.RETURN_GENERATED_KEYS
                        )
        ) {
            statement.setString(1, lastName);
            statement.setString(2, firstName);
            statement.setString(3, phone);
            statement.setString(4, email);

            statement.executeUpdate();

            try (
                    ResultSet keys =
                            statement.getGeneratedKeys()
            ) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        return null;
    }

    private void clearPasswords(
            char[] password,
            char[] confirmPassword
    ) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }

        if (confirmPassword != null) {
            Arrays.fill(confirmPassword, '\0');
        }
    }

    private void clearFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
    }

    private void setBusy(boolean busy) {
        registerButton.setEnabled(!busy);
        firstNameField.setEnabled(!busy);
        lastNameField.setEnabled(!busy);
        emailField.setEnabled(!busy);
        phoneField.setEnabled(!busy);
        passwordField.setEnabled(!busy);
        confirmPasswordField.setEnabled(!busy);

        registerButton.setText(
                busy ? "Se creează contul..." : "Creează cont"
        );

        setCursor(
                Cursor.getPredefinedCursor(
                        busy
                                ? Cursor.WAIT_CURSOR
                                : Cursor.DEFAULT_CURSOR
                )
        );
    }

    private void openLoginFrame() {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);

        dispose();
    }

    // Compatibilitate temporară cu autentificarea actuală.
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