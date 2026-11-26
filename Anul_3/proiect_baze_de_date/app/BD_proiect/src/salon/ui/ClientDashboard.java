package salon.ui;

import salon.ai.AiAssistant;
import salon.db.DatabaseConnection;
import salon.service.ClientService;
import salon.ui.client.ClientActivityPanel;
import salon.ui.components.RoundedCard;
import salon.ui.components.UiStyle;

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

    private static final Color CREAM = UiStyle.CREAM;
    private static final Color INK = UiStyle.INK;
    private static final Color MUTED = UiStyle.MUTED;
    private static final Color WHITE = UiStyle.WHITE;
    private static final Color FIELD_BACKGROUND = UiStyle.FIELD;
    private static final Color RED = UiStyle.RED;
    private static final Color GREEN = UiStyle.GREEN;

    private final String userEmail;

    private final CardLayout contentLayout = new CardLayout();

    private final JPanel contentPanel =
            new JPanel(contentLayout);

    private final JTextField firstNameField =
            new JTextField(20);

    private final JTextField lastNameField =
            new JTextField(20);

    private final JTextField emailField =
            new JTextField(20);

    private final JTextField phoneField =
            new JTextField(20);

    private final JButton editDataButton =
            new JButton("Doresc să-mi modific datele");

    private final JButton saveDataButton =
            new JButton("Salvează modificările");

    private final JButton changePasswordButton =
            new JButton("Doresc să-mi modific parola");

    private final JPanel passwordPanel =
            new JPanel(new GridBagLayout());

    private final JPasswordField currentPasswordField =
            new JPasswordField(20);

    private final JPasswordField newPasswordField =
            new JPasswordField(20);

    private final JPasswordField confirmPasswordField =
            new JPasswordField(20);

    private final JButton savePasswordButton =
            new JButton("Salvează parola nouă");

    private final JLabel profileStatusLabel =
            new JLabel(" ");

    private final JLabel passwordStatusLabel =
            new JLabel(" ");

    private final JLabel greetingLabel =
            new JLabel("Bine ai venit!");

    private final AppointmentBookingPanel appointmentPanel;
    private final CourseBookingPanel coursePanel;
    private final ClientActivityPanel activityPanel;

    private Integer clientId;

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

        contentPanel.setBackground(CREAM);

        mainPanel.add(
                createTopBar(),
                BorderLayout.NORTH
        );

        mainPanel.add(
                contentPanel,
                BorderLayout.CENTER
        );

        contentPanel.add(
                createHomePanel(),
                "HOME"
        );

        contentPanel.add(
                wrapWithBackButton(createProfilePanel()),
                "PROFILE"
        );

        appointmentPanel =
                new AppointmentBookingPanel(userEmail);

        contentPanel.add(
                wrapWithBackButton(appointmentPanel),
                "APPOINTMENTS"
        );

        coursePanel =
                new CourseBookingPanel(userEmail);

        contentPanel.add(
                wrapWithBackButton(coursePanel),
                "COURSES"
        );

        activityPanel = new ClientActivityPanel(
                new ClientService(),
                userEmail,
                this::showHome
        );

        contentPanel.add(
                activityPanel,
                "ACTIVITY"
        );

        disableDataEditing();
        loadProfileData();

        AiAssistant.installForClient(
                this,
                this::showAppointments
        );

        showHome();

        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(INK);
        topBar.setBorder(
                BorderFactory.createEmptyBorder(
                        14, 28, 14, 28
                )
        );

        JLabel brand = new JLabel("SALON & ACADEMIE");
        brand.setFont(
                new Font("SansSerif", Font.BOLD, 15)
        );
        brand.setForeground(WHITE);

        JPanel actions = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        6,
                        0
                )
        );
        actions.setOpaque(false);

        JButton profileButton =
                createTopBarButton("Profilul meu");

        profileButton.addActionListener(
                e -> showProfile()
        );

        JButton activityButton =
                createTopBarButton("Activitatea mea");

        activityButton.addActionListener(
                e -> showActivity()
        );

        JButton logoutButton =
                createTopBarButton("Deconectare");

        logoutButton.addActionListener(
                e -> logout()
        );

        actions.add(profileButton);
        actions.add(activityButton);
        actions.add(logoutButton);

        topBar.add(
                brand,
                BorderLayout.WEST
        );

        topBar.add(
                actions,
                BorderLayout.EAST
        );

        return topBar;
    }

    private JButton createTopBarButton(String text) {
        JButton button = new JButton(text);

        button.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );
        button.setForeground(WHITE);
        button.setBackground(INK);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        return button;
    }

    private JPanel wrapWithBackButton(JComponent content) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CREAM);

        JPanel navigation = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        0,
                        0
                )
        );
        navigation.setOpaque(false);
        navigation.setBorder(
                BorderFactory.createEmptyBorder(
                        16, 24, 0, 24
                )
        );

        JButton backButton =
                UiStyle.secondaryButton("< Înapoi");

        backButton.setPreferredSize(
                new Dimension(110, 38)
        );

        backButton.addActionListener(
                e -> showHome()
        );

        navigation.add(backButton);

        wrapper.add(
                navigation,
                BorderLayout.NORTH
        );

        wrapper.add(
                content,
                BorderLayout.CENTER
        );

        return wrapper;
    }

    private JPanel createHomePanel() {
        JPanel background =
                new JPanel(new GridBagLayout());

        background.setBackground(CREAM);
        background.setBorder(
                BorderFactory.createEmptyBorder(
                        30, 30, 30, 30
                )
        );

        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(
                BorderFactory.createEmptyBorder(
                        40, 46, 40, 46
                )
        );
        card.setPreferredSize(
                new Dimension(520, 360)
        );

        GridBagConstraints c =
                createFormConstraints();

        greetingLabel.setFont(
                new Font("SansSerif", Font.BOLD, 30)
        );
        greetingLabel.setForeground(INK);

        addRow(
                card,
                greetingLabel,
                c,
                0,
                0,
                32
        );

        JButton appointmentsButton =
                createMenuButton("Programări salon");

        appointmentsButton.addActionListener(
                e -> showAppointments()
        );

        addRow(
                card,
                appointmentsButton,
                c,
                1,
                0,
                16
        );

        JButton coursesButton =
                createMenuButton("Cursuri academie");

        coursesButton.addActionListener(
                e -> showCourses()
        );

        addRow(
                card,
                coursesButton,
                c,
                2,
                0,
                0
        );

        background.add(card);

        return background;
    }

    private JPanel createProfilePanel() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(CREAM);

        JPanel background =
                new JPanel(new GridBagLayout());

        background.setBackground(CREAM);
        background.setBorder(
                BorderFactory.createEmptyBorder(
                        24, 24, 24, 24
                )
        );

        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(
                BorderFactory.createEmptyBorder(
                        36, 48, 36, 48
                )
        );

        GridBagConstraints c =
                createFormConstraints();

        JLabel heading = new JLabel("Profilul meu");
        heading.setFont(
                new Font("SansSerif", Font.BOLD, 30)
        );
        heading.setForeground(INK);

        addRow(
                card,
                heading,
                c,
                0,
                0,
                24
        );

        addField(
                card,
                c,
                1,
                "Prenume",
                firstNameField
        );

        addField(
                card,
                c,
                3,
                "Nume",
                lastNameField
        );

        addField(
                card,
                c,
                5,
                "Adresă de e-mail",
                emailField
        );

        emailField.setEditable(false);

        addField(
                card,
                c,
                7,
                "Telefon",
                phoneField
        );

        stylePrimaryButton(editDataButton);
        editDataButton.addActionListener(
                e -> enableDataEditing()
        );

        addRow(
                card,
                editDataButton,
                c,
                9,
                8,
                12
        );

        stylePrimaryButton(saveDataButton);
        saveDataButton.addActionListener(
                e -> saveProfileData()
        );
        saveDataButton.setVisible(false);

        addRow(
                card,
                saveDataButton,
                c,
                10,
                0,
                18
        );

        styleStatusLabel(profileStatusLabel);

        addRow(
                card,
                profileStatusLabel,
                c,
                11,
                0,
                24
        );

        stylePrimaryButton(changePasswordButton);

        changePasswordButton.addActionListener(
                e -> togglePasswordSection()
        );

        addRow(
                card,
                changePasswordButton,
                c,
                12,
                0,
                12
        );

        passwordPanel.setOpaque(false);
        passwordPanel.setVisible(false);

        GridBagConstraints passwordConstraints =
                createFormConstraints();

        addField(
                passwordPanel,
                passwordConstraints,
                0,
                "Parola actuală",
                currentPasswordField
        );

        addField(
                passwordPanel,
                passwordConstraints,
                2,
                "Parolă nouă",
                newPasswordField
        );

        addField(
                passwordPanel,
                passwordConstraints,
                4,
                "Confirmă parola nouă",
                confirmPasswordField
        );

        stylePrimaryButton(savePasswordButton);

        savePasswordButton.addActionListener(
                e -> changePassword()
        );

        addRow(
                passwordPanel,
                savePasswordButton,
                passwordConstraints,
                6,
                8,
                12
        );

        styleStatusLabel(passwordStatusLabel);

        addRow(
                passwordPanel,
                passwordStatusLabel,
                passwordConstraints,
                7,
                0,
                0
        );

        addRow(
                card,
                passwordPanel,
                c,
                13,
                0,
                0
        );

        GridBagConstraints cardConstraints =
                new GridBagConstraints();

        cardConstraints.gridx = 0;
        cardConstraints.gridy = 0;
        cardConstraints.weightx = 1;
        cardConstraints.anchor =
                GridBagConstraints.NORTH;

        background.add(
                card,
                cardConstraints
        );

        JScrollPane scroll =
                new JScrollPane(background);

        scroll.setBorder(null);
        scroll.getViewport().setBackground(CREAM);
        scroll.getVerticalScrollBar()
                .setUnitIncrement(18);

        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        page.add(
                scroll,
                BorderLayout.CENTER
        );

        return page;
    }

    private GridBagConstraints createFormConstraints() {
        GridBagConstraints c =
                new GridBagConstraints();

        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        return c;
    }

    private void addField(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            JTextField field
    ) {
        JLabel label = new JLabel(labelText);
        label.setForeground(INK);
        label.setLabelFor(field);

        addRow(
                panel,
                label,
                constraints,
                row,
                0,
                8
        );

        configureField(field);

        addRow(
                panel,
                field,
                constraints,
                row + 1,
                0,
                18
        );
    }

    private void configureField(JTextField field) {
        field.setPreferredSize(
                new Dimension(420, 46)
        );
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(INK);
        field.setCaretColor(INK);
        field.setMargin(
                new Insets(10, 14, 10, 14)
        );
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        button.setBackground(INK);
        button.setForeground(WHITE);
        button.setPreferredSize(
                new Dimension(420, 46)
        );
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );
        button.putClientProperty("JButton.arc", 18);
    }

    private void styleStatusLabel(JLabel label) {
        label.setForeground(MUTED);
        label.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);

        button.setFont(
                new Font("SansSerif", Font.BOLD, 15)
        );
        button.setBackground(FIELD_BACKGROUND);
        button.setForeground(INK);
        button.setPreferredSize(
                new Dimension(420, 58)
        );
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );
        button.putClientProperty("JButton.arc", 18);

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
        constraints.insets =
                new Insets(top, 0, bottom, 0);

        panel.add(
                component,
                constraints
        );
    }

    private void showHome() {
        contentLayout.show(
                contentPanel,
                "HOME"
        );

        setAssistantVisible(true);
    }

    private void showProfile() {
        contentLayout.show(
                contentPanel,
                "PROFILE"
        );

        setAssistantVisible(true);
    }

    private void showAppointments() {
        contentLayout.show(
                contentPanel,
                "APPOINTMENTS"
        );

        setAssistantVisible(false);
    }

    private void showCourses() {
        contentLayout.show(
                contentPanel,
                "COURSES"
        );

        setAssistantVisible(false);
    }

    private void showActivity() {
        contentLayout.show(
                contentPanel,
                "ACTIVITY"
        );

        setAssistantVisible(true);

        activityPanel.refreshData();
    }

    private void setAssistantVisible(boolean visible) {
        for (Component component : getLayeredPane().getComponents()) {
            if (component.getClass().getName()
                    .equals("salon.ai.AiAssistant$PillButton")) {

                component.setVisible(visible);
            }
        }
    }

    private void logout() {
        clearPasswordFields();

        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);

        dispose();
    }

    private void enableDataEditing() {
        if (clientId == null) {
            profileStatusLabel.setForeground(RED);
            profileStatusLabel.setText(
                    "Profilul nu este încărcat. "
                            + "Redeschide aplicația și verifică conexiunea."
            );
            return;
        }

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

    private void disableDataEditing() {
        firstNameField.setEditable(false);
        lastNameField.setEditable(false);
        emailField.setEditable(false);
        phoneField.setEditable(false);

        editDataButton.setVisible(true);
        saveDataButton.setVisible(false);
    }

    private void saveProfileData() {
        if (clientId == null) {
            profileStatusLabel.setForeground(RED);
            profileStatusLabel.setText(
                    "Contul nu este asociat unui profil de client încărcat."
            );
            return;
        }

        String firstName =
                firstNameField.getText().trim();

        String lastName =
                lastNameField.getText().trim();

        String phone =
                phoneField.getText().trim();

        if (firstName.isEmpty()
                || lastName.isEmpty()
                || phone.isEmpty()) {

            profileStatusLabel.setForeground(RED);
            profileStatusLabel.setText(
                    "Completează prenumele, numele și telefonul."
            );
            return;
        }

        if (firstName.length() > 50
                || lastName.length() > 50) {

            profileStatusLabel.setForeground(RED);
            profileStatusLabel.setText(
                    "Prenumele și numele pot avea maximum 50 de caractere."
            );
            return;
        }

        if (!phone.matches("[0-9+\\s-]{6,20}")) {
            profileStatusLabel.setForeground(RED);
            profileStatusLabel.setText(
                    "Telefonul trebuie să conțină 6–20 caractere: "
                            + "cifre, spații, cratime sau +."
            );
            return;
        }

        String sql = """
                UPDATE client
                SET prenume = ?, nume = ?, telefon = ?
                WHERE id_client = ?
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
            statement.setInt(4, clientId);

            statement.executeUpdate();

            greetingLabel.setText(
                    "Bine ai venit, " + firstName + "!"
            );

            disableDataEditing();

            profileStatusLabel.setForeground(GREEN);
            profileStatusLabel.setText(
                    "Datele profilului au fost salvate."
            );

        } catch (SQLException exception) {
            exception.printStackTrace();

            profileStatusLabel.setForeground(RED);

            if (exception.getErrorCode() == 1062) {
                profileStatusLabel.setText(
                        "Numărul de telefon este deja folosit."
                );
            } else {
                profileStatusLabel.setText(
                        "Datele profilului nu au putut fi salvate."
                );
            }
        }
    }

    private void togglePasswordSection() {
        boolean show =
                !passwordPanel.isVisible();

        passwordPanel.setVisible(show);

        changePasswordButton.setText(
                show
                        ? "Ascunde schimbarea parolei"
                        : "Doresc să-mi modific parola"
        );

        passwordStatusLabel.setText(" ");

        if (!show) {
            clearPasswordFields();
        }

        revalidate();
        repaint();
    }

    private void changePassword() {
        char[] currentPassword =
                currentPasswordField.getPassword();

        char[] newPassword =
                newPasswordField.getPassword();

        char[] confirmPassword =
                confirmPasswordField.getPassword();

        if (currentPassword.length == 0
                || newPassword.length == 0
                || confirmPassword.length == 0) {

            clearPasswords(
                    currentPassword,
                    newPassword,
                    confirmPassword
            );

            showPasswordError(
                    "Completează toate câmpurile pentru parolă."
            );
            return;
        }

        if (!Arrays.equals(
                newPassword,
                confirmPassword
        )) {
            clearPasswords(
                    currentPassword,
                    newPassword,
                    confirmPassword
            );

            showPasswordError(
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

            showPasswordError(
                    "Parola nouă trebuie să aibă cel puțin 6 caractere."
            );
            return;
        }

        setPasswordBusy(true);

        passwordStatusLabel.setForeground(MUTED);
        passwordStatusLabel.setText(
                "Se actualizează parola..."
        );

        new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground()
                    throws Exception {

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

                if (!isDisplayable()) {
                    return;
                }

                try {
                    String result = get();

                    if ("CURRENT_PASSWORD_INCORRECT"
                            .equals(result)) {

                        showPasswordError(
                                "Parola actuală este incorectă."
                        );
                        return;
                    }

                    clearPasswordFields();

                    passwordStatusLabel.setForeground(GREEN);
                    passwordStatusLabel.setText(
                            "Parola a fost schimbată cu succes."
                    );

                } catch (Exception exception) {
                    exception.printStackTrace();

                    showPasswordError(
                            "Parola nu a putut fi schimbată."
                    );
                }
            }
        }.execute();
    }

    private String updatePassword(
            char[] currentPassword,
            char[] newPassword
    ) throws Exception {

        String currentHash =
                hashPassword(currentPassword);

        String newHash =
                hashPassword(newPassword);

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            String checkSql = """
                    SELECT id_utilizator
                    FROM utilizator
                    WHERE email = ?
                      AND parola = ?
                    """;

            int userId;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(checkSql)
            ) {
                statement.setString(1, userEmail);
                statement.setString(2, currentHash);

                try (
                        ResultSet result =
                                statement.executeQuery()
                ) {
                    if (!result.next()) {
                        return "CURRENT_PASSWORD_INCORRECT";
                    }

                    userId =
                            result.getInt("id_utilizator");
                }
            }

            String updateSql = """
                    UPDATE utilizator
                    SET parola = ?
                    WHERE id_utilizator = ?
                      AND parola = ?
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(updateSql)
            ) {
                statement.setString(1, newHash);
                statement.setInt(2, userId);
                statement.setString(3, currentHash);

                int updated =
                        statement.executeUpdate();

                if (updated == 0
                        && !newHash.equals(currentHash)) {

                    throw new SQLException(
                            "Parola a fost modificată între timp."
                    );
                }
            }
        }

        return "SUCCESS";
    }

    private void setPasswordBusy(boolean busy) {
        savePasswordButton.setEnabled(!busy);
        currentPasswordField.setEnabled(!busy);
        newPasswordField.setEnabled(!busy);
        confirmPasswordField.setEnabled(!busy);
        changePasswordButton.setEnabled(!busy);

        savePasswordButton.setText(
                busy
                        ? "Se actualizează..."
                        : "Salvează parola nouă"
        );
    }

    private void showPasswordError(String message) {
        passwordStatusLabel.setForeground(RED);
        passwordStatusLabel.setText(message);
    }

    private void clearPasswordFields() {
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void clearPasswords(char[]... passwords) {
        for (char[] password : passwords) {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    private void loadProfileData() {
        String sql = """
                SELECT
                    c.id_client,
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

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                if (!result.next()) {
                    clientId = null;

                    profileStatusLabel.setForeground(RED);
                    profileStatusLabel.setText(
                            "Contul nu este asociat unui client."
                    );
                    return;
                }

                clientId =
                        result.getInt("id_client");

                String firstName =
                        result.getString("prenume");

                firstNameField.setText(firstName);

                lastNameField.setText(
                        result.getString("nume")
                );

                emailField.setText(
                        result.getString("email")
                );

                phoneField.setText(
                        result.getString("telefon")
                );

                if (firstName != null
                        && !firstName.isBlank()) {

                    greetingLabel.setText(
                            "Bine ai venit, "
                                    + firstName
                                    + "!"
                    );
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();

            clientId = null;

            profileStatusLabel.setForeground(RED);
            profileStatusLabel.setText(
                    "Datele profilului nu au putut fi încărcate."
            );
        }
    }

    // Compatibilitate cu autentificarea existentă.
    private String hashPassword(char[] password)
            throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] passwordBytes =
                new String(password).getBytes(
                        StandardCharsets.UTF_8
                );

        byte[] hashedBytes;

        try {
            hashedBytes =
                    digest.digest(passwordBytes);
        } finally {
            Arrays.fill(passwordBytes, (byte) 0);
        }

        try {
            StringBuilder builder =
                    new StringBuilder();

            for (byte value : hashedBytes) {
                builder.append(
                        String.format(
                                "%02x",
                                value & 0xff
                        )
                );
            }

            return builder.toString();

        } finally {
            Arrays.fill(hashedBytes, (byte) 0);
        }
    }
}