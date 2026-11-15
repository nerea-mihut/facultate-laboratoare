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

public class ClientDashboard extends JFrame {

    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color INK = new Color(30, 30, 30);
    private static final Color MUTED = new Color(115, 110, 103);
    private static final Color WHITE = Color.WHITE;
    private static final Color FIELD_BACKGROUND =
            new Color(250, 249, 247);

    private final String userEmail;

    private final JPanel contentPanel = new JPanel(new CardLayout());

    private final JTextField firstNameField = new JTextField(20);
    private final JTextField lastNameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);

    private final JButton editDataButton =
            new JButton("Doresc să-mi modific datele");

    private final JButton saveDataButton =
            new JButton("Salvează modificările");

    private final JButton changePasswordButton =
            new JButton("Doresc să-mi modific parola");

    private final JPanel passwordPanel = new JPanel(new GridBagLayout());

    private final JPasswordField currentPasswordField =
            new JPasswordField(20);
    private final JPasswordField newPasswordField =
            new JPasswordField(20);
    private final JPasswordField confirmPasswordField =
            new JPasswordField(20);

    private final JButton savePasswordButton =
            new JButton("Salvează parola nouă");

    private final JLabel profileStatusLabel = new JLabel(" ");
    private final JLabel passwordStatusLabel = new JLabel(" ");

    public ClientDashboard(String userEmail) {
        this.userEmail = userEmail;

        setTitle("Salon & Academie — Panou client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 780));
        setSize(1200, 820);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CREAM);

        setContentPane(mainPanel);

        mainPanel.add(createTopBar(), BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(createHomePanel(), "HOME");
        contentPanel.add(createProfilePanel(), "PROFILE");

        loadProfileData();

        showHome();

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

        JButton homeButton = createTopBarButton("Acasă");

        homeButton.addActionListener(e -> showHome());

        JButton profileButton = createTopBarButton(
                "\uD83D\uDC64  Profilul meu"
        );

        profileButton.addActionListener(e -> showProfile());

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

        actions.add(homeButton);
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

    private JPanel createHomePanel() {
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

        String clientFirstName = findClientFirstName(userEmail);

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

    private JPanel createProfilePanel() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(CREAM);
        background.setBorder(
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        );

        JPanel card = createRoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(
                BorderFactory.createEmptyBorder(36, 48, 36, 48)
        );
        card.setPreferredSize(new Dimension(720, 640));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel heading = new JLabel("Profilul meu");
        heading.setFont(new Font("SansSerif", Font.BOLD, 30));
        heading.setForeground(INK);
        addRow(card, heading, c, 0, 0, 24);

        addField(card, c, 1, "Prenume", firstNameField, true);
        addField(card, c, 3, "Nume", lastNameField, true);
        addField(card, c, 5, "Adresă de e-mail", emailField, false);
        addField(card, c, 7, "Telefon", phoneField, true);

        editDataButton.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        editDataButton.setBackground(INK);
        editDataButton.setForeground(WHITE);
        editDataButton.setPreferredSize(new Dimension(360, 46));
        editDataButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        editDataButton.addActionListener(e -> enableDataEditing());
        addRow(card, editDataButton, c, 9, 8, 12);

        saveDataButton.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        saveDataButton.setBackground(INK);
        saveDataButton.setForeground(WHITE);
        saveDataButton.setPreferredSize(new Dimension(360, 46));
        saveDataButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        saveDataButton.addActionListener(e -> saveProfileData());
        saveDataButton.setVisible(false);
        addRow(card, saveDataButton, c, 10, 0, 18);

        profileStatusLabel.setForeground(MUTED);
        profileStatusLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
        addRow(card, profileStatusLabel, c, 11, 0, 24);

        changePasswordButton.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        changePasswordButton.setBackground(INK);
        changePasswordButton.setForeground(WHITE);
        changePasswordButton.setPreferredSize(new Dimension(360, 46));
        changePasswordButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        changePasswordButton.addActionListener(e ->
                togglePasswordSection()
        );
        addRow(card, changePasswordButton, c, 12, 0, 12);

        passwordPanel.setOpaque(false);
        passwordPanel.setVisible(false);

        GridBagConstraints passwordConstraints = new GridBagConstraints();
        passwordConstraints.gridx = 0;
        passwordConstraints.weightx = 1;
        passwordConstraints.fill = GridBagConstraints.HORIZONTAL;
        passwordConstraints.anchor = GridBagConstraints.WEST;

        addPasswordField(
                passwordPanel,
                passwordConstraints,
                0,
                "Parola actuală",
                currentPasswordField
        );

        addPasswordField(
                passwordPanel,
                passwordConstraints,
                2,
                "Parolă nouă",
                newPasswordField
        );

        addPasswordField(
                passwordPanel,
                passwordConstraints,
                4,
                "Confirmă parola nouă",
                confirmPasswordField
        );

        savePasswordButton.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        savePasswordButton.setBackground(INK);
        savePasswordButton.setForeground(WHITE);
        savePasswordButton.setPreferredSize(new Dimension(360, 46));
        savePasswordButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        savePasswordButton.addActionListener(e -> changePassword());
        addRow(
                passwordPanel,
                savePasswordButton,
                passwordConstraints,
                6,
                8,
                12
        );

        passwordStatusLabel.setForeground(MUTED);
        passwordStatusLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
        addRow(
                passwordPanel,
                passwordStatusLabel,
                passwordConstraints,
                7,
                0,
                0
        );

        addRow(card, passwordPanel, c, 13, 0, 0);

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

    private void addField(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            JTextField field,
            boolean editable
    ) {
        JLabel label = new JLabel(labelText);
        label.setForeground(INK);
        label.setLabelFor(field);
        addRow(panel, label, constraints, row, 0, 8);

        configureField(field);
        field.setEditable(editable);
        addRow(panel, field, constraints, row + 1, 0, 18);
    }

    private void addPasswordField(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            JPasswordField field
    ) {
        JLabel label = new JLabel(labelText);
        label.setForeground(INK);
        label.setLabelFor(field);
        addRow(panel, label, constraints, row, 0, 8);

        configureField(field);
        addRow(panel, field, constraints, row + 1, 0, 18);
    }

    private void configureField(JTextField field) {
        field.setPreferredSize(new Dimension(420, 46));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(INK);
        field.setCaretColor(INK);
        field.setMargin(new Insets(10, 14, 10, 14));
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);

        button.setFont(
                new Font("SansSerif", Font.BOLD, 15)
        );
        button.setBackground(FIELD_BACKGROUND);
        button.setForeground(INK);
        button.setPreferredSize(new Dimension(420, 58));
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

    private void showHome() {
        CardLayout layout = (CardLayout) contentPanel.getLayout();
        layout.show(contentPanel, "HOME");
    }

    private void showProfile() {
        CardLayout layout = (CardLayout) contentPanel.getLayout();
        layout.show(contentPanel, "PROFILE");
    }

    private void enableDataEditing() {
        firstNameField.setEditable(true);
        lastNameField.setEditable(true);
        phoneField.setEditable(true);

        editDataButton.setVisible(false);
        saveDataButton.setVisible(true);

        profileStatusLabel.setForeground(MUTED);
        profileStatusLabel.setText(
                "Poți modifica prenumele, numele și telefonul."
        );
    }

    private void saveProfileData() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (firstName.isEmpty()
                || lastName.isEmpty()
                || phone.isEmpty()) {

            profileStatusLabel.setForeground(new Color(160, 45, 45));
            profileStatusLabel.setText(
                    "Completează prenumele, numele și telefonul."
            );
            return;
        }

        if (!phone.matches("[0-9+\\s-]{6,20}")) {
            profileStatusLabel.setForeground(new Color(160, 45, 45));
            profileStatusLabel.setText(
                    "Numărul de telefon poate conține doar cifre, "
                            + "spații, cratime și prefixul +."
            );
            return;
        }

        String sql = """
                UPDATE client
                SET prenume = ?, nume = ?, telefon = ?
                WHERE email = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, phone);
            statement.setString(4, userEmail);

            int updatedRows = statement.executeUpdate();

            if (updatedRows > 0) {
                profileStatusLabel.setForeground(
                        new Color(55, 110, 70)
                );
                profileStatusLabel.setText(
                        "Datele profilului au fost actualizate."
                );

                disableDataEditing();
            } else {
                profileStatusLabel.setForeground(new Color(160, 45, 45));
                profileStatusLabel.setText(
                        "Datele profilului nu au putut fi actualizate."
                );
            }

        } catch (SQLException exception) {
            exception.printStackTrace();

            profileStatusLabel.setForeground(new Color(160, 45, 45));
            profileStatusLabel.setText(
                    "Datele profilului nu au putut fi actualizate."
            );
        }
    }

    private void disableDataEditing() {
        firstNameField.setEditable(false);
        lastNameField.setEditable(false);
        phoneField.setEditable(false);

        editDataButton.setVisible(true);
        saveDataButton.setVisible(false);
    }

    private void togglePasswordSection() {
        boolean isVisible = passwordPanel.isVisible();

        passwordPanel.setVisible(!isVisible);

        if (!isVisible) {
            changePasswordButton.setText(
                    "Ascunde schimbarea parolei"
            );

            passwordStatusLabel.setText(" ");
        } else {
            changePasswordButton.setText(
                    "Doresc să-mi modific parola"
            );

            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        }

        revalidate();
        repaint();
    }

    private void changePassword() {
        char[] currentPassword = currentPasswordField.getPassword();
        char[] newPassword = newPasswordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();

        if (currentPassword.length == 0
                || newPassword.length == 0
                || confirmPassword.length == 0) {

            clearPasswords(
                    currentPassword,
                    newPassword,
                    confirmPassword
            );

            passwordStatusLabel.setForeground(new Color(160, 45, 45));
            passwordStatusLabel.setText(
                    "Completează toate câmpurile pentru parolă."
            );
            return;
        }

        if (!Arrays.equals(newPassword, confirmPassword)) {
            clearPasswords(
                    currentPassword,
                    newPassword,
                    confirmPassword
            );

            passwordStatusLabel.setForeground(new Color(160, 45, 45));
            passwordStatusLabel.setText(
                    "Parola nouă și confirmarea ei nu coincid."
            );
            return;
        }

        if (newPassword.length < 6) {
            clearPasswords(
                    currentPassword,
                    newPassword,
                    confirmPassword
            );

            passwordStatusLabel.setForeground(new Color(160, 45, 45));
            passwordStatusLabel.setText(
                    "Parola nouă trebuie să aibă cel puțin 6 caractere."
            );
            return;
        }

        setPasswordBusy(true);
        passwordStatusLabel.setForeground(MUTED);
        passwordStatusLabel.setText("Se actualizează parola...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {

            @Override
            protected String doInBackground() throws Exception {
                try {
                    return updatePassword(
                            currentPassword,
                            newPassword
                    );
                } finally {
                    clearPasswords(
                            currentPassword,
                            newPassword,
                            confirmPassword
                    );
                }
            }

            @Override
            protected void done() {
                setPasswordBusy(false);

                try {
                    String result = get();

                    if ("CURRENT_PASSWORD_INCORRECT".equals(result)) {
                        passwordStatusLabel.setForeground(
                                new Color(160, 45, 45)
                        );
                        passwordStatusLabel.setText(
                                "Parola actuală este incorectă."
                        );
                        return;
                    }

                    passwordStatusLabel.setForeground(
                            new Color(55, 110, 70)
                    );
                    passwordStatusLabel.setText(
                            "Parola a fost schimbată cu succes."
                    );

                    currentPasswordField.setText("");
                    newPasswordField.setText("");
                    confirmPasswordField.setText("");

                } catch (Exception exception) {
                    passwordStatusLabel.setForeground(
                            new Color(160, 45, 45)
                    );
                    passwordStatusLabel.setText(
                            "Parola nu a putut fi schimbată."
                    );

                    exception.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private String updatePassword(
            char[] currentPassword,
            char[] newPassword
    ) throws Exception {

        String currentHash = hashPassword(currentPassword);
        String newHash = hashPassword(newPassword);

        String sql = """
                UPDATE utilizator
                SET parola = ?
                WHERE email = ? AND parola = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, newHash);
            statement.setString(2, userEmail);
            statement.setString(3, currentHash);

            int updatedRows = statement.executeUpdate();

            if (updatedRows == 0) {
                return "CURRENT_PASSWORD_INCORRECT";
            }

            return "SUCCESS";
        }
    }

    private void setPasswordBusy(boolean busy) {
        savePasswordButton.setEnabled(!busy);
        currentPasswordField.setEnabled(!busy);
        newPasswordField.setEnabled(!busy);
        confirmPasswordField.setEnabled(!busy);

        savePasswordButton.setText(
                busy ? "Se actualizează..." : "Salvează parola nouă"
        );
    }

    private void clearPasswords(
            char[] currentPassword,
            char[] newPassword,
            char[] confirmPassword
    ) {
        if (currentPassword != null) {
            Arrays.fill(currentPassword, '\0');
        }

        if (newPassword != null) {
            Arrays.fill(newPassword, '\0');
        }

        if (confirmPassword != null) {
            Arrays.fill(confirmPassword, '\0');
        }
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

    private void loadProfileData() {
        String sql = """
                SELECT
                    c.prenume,
                    c.nume,
                    c.email,
                    c.telefon
                FROM client c
                JOIN utilizator u
                    ON u.id_client = c.id_client
                WHERE u.email = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, userEmail);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    firstNameField.setText(
                            resultSet.getString("prenume")
                    );

                    lastNameField.setText(
                            resultSet.getString("nume")
                    );

                    emailField.setText(
                            resultSet.getString("email")
                    );

                    phoneField.setText(
                            resultSet.getString("telefon")
                    );
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();

            profileStatusLabel.setForeground(new Color(160, 45, 45));
            profileStatusLabel.setText(
                    "Datele profilului nu au putut fi încărcate."
            );
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