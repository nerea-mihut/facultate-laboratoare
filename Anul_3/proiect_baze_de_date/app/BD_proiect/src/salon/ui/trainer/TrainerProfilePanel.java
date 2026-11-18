package salon.ui.trainer;

import salon.db.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrainerProfilePanel extends JPanel {

    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color INK = new Color(30, 30, 30);
    private static final Color MUTED = new Color(115, 110, 103);
    private static final Color WHITE = Color.WHITE;
    private static final Color FIELD = new Color(250, 249, 247);
    private static final Color BORDER = new Color(225, 220, 213);
    private static final Color GREEN = new Color(88, 150, 105);
    private static final Color RED = new Color(160, 45, 45);

    private final String userEmail;
    private final Runnable showHome;

    private final JTextField firstNameField = new JTextField(20);
    private final JTextField lastNameField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);

    private final JLabel statusLabel = new JLabel(" ");

    private Integer trainerId;

    public TrainerProfilePanel(
            String userEmail,
            Runnable showHome
    ) {
        this.userEmail = userEmail;
        this.showHome = showHome;

        setLayout(new BorderLayout());
        setBackground(CREAM);
        setBorder(
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        );

        trainerId = findTrainerId();

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        loadProfile();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JButton backButton = createSecondaryButton("Înapoi");
        backButton.addActionListener(e -> showHome.run());

        JLabel title = createLabel(
                "Profilul meu", 24, true, INK
        );

        header.add(backButton, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);

        return header;
    }

    private JPanel createForm() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setOpaque(false);

        JPanel card = createRoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(
                BorderFactory.createEmptyBorder(34, 40, 34, 40)
        );
        card.setPreferredSize(new Dimension(620, 520));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        addFormRow(card, c, 0,
                createLabel("Prenume", 13, false, INK));
        styleField(firstNameField);
        addFormRow(card, c, 1, firstNameField);

        addFormRow(card, c, 2,
                createLabel("Nume", 13, false, INK));
        styleField(lastNameField);
        addFormRow(card, c, 3, lastNameField);

        addFormRow(card, c, 4,
                createLabel("Telefon", 13, false, INK));
        styleField(phoneField);
        addFormRow(card, c, 5, phoneField);

        addFormRow(card, c, 6,
                createLabel("E-mail", 13, false, INK));
        styleField(emailField);
        emailField.setEditable(false);
        addFormRow(card, c, 7, emailField);

        JButton saveButton = createPrimaryButton(
                "Salvează modificările"
        );
        saveButton.addActionListener(e -> saveProfile());

        addFormRow(card, c, 8, saveButton);

        background.add(card);
        return background;
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

    private void loadProfile() {
        if (trainerId == null) {
            showError("Contul nu este asociat unui trainer.");
            return;
        }

        String sql = """
                SELECT
                    prenume,
                    nume,
                    telefon,
                    email
                FROM angajat
                WHERE id_angajat = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, trainerId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    firstNameField.setText(
                            result.getString("prenume")
                    );
                    lastNameField.setText(
                            result.getString("nume")
                    );
                    phoneField.setText(
                            result.getString("telefon")
                    );
                    emailField.setText(
                            result.getString("email")
                    );
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            showError("Profilul nu a putut fi încărcat.");
        }
    }

    private void saveProfile() {
        if (trainerId == null) {
            showError("Contul nu este asociat unui trainer.");
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (firstName.isEmpty()
                || lastName.isEmpty()
                || phone.isEmpty()) {

            showError("Completează prenumele, numele și telefonul.");
            return;
        }

        String sql = """
                UPDATE angajat
                SET prenume = ?,
                    nume = ?,
                    telefon = ?
                WHERE id_angajat = ?
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
            statement.setInt(4, trainerId);
            statement.executeUpdate();

            showSuccess("Profilul a fost actualizat.");

        } catch (SQLException exception) {
            exception.printStackTrace();
            showError("Profilul nu a putut fi actualizat.");
        }
    }

    private void styleField(JTextField field) {
        field.setPreferredSize(new Dimension(340, 44));
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
        c.insets = new Insets(0, 0, 12, 0);
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
        button.setPreferredSize(new Dimension(320, 46));
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

    private void showError(String message) {
        statusLabel.setForeground(RED);
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        statusLabel.setForeground(GREEN);
        statusLabel.setText(message);
    }
}