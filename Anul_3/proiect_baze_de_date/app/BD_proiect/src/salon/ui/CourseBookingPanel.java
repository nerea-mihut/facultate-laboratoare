package salon.ui;

import salon.db.DatabaseConnection;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CourseBookingPanel extends JPanel {

    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color INK = new Color(30, 30, 30);
    private static final Color MUTED = new Color(115, 110, 103);
    private static final Color WHITE = Color.WHITE;
    private static final Color FIELD_BACKGROUND =
            new Color(250, 249, 247);
    private static final Color BORDER =
            new Color(225, 220, 213);
    private static final Color GREEN = new Color(88, 150, 105);
    private static final Color RED = new Color(160, 45, 45);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String userEmail;

    private final JPanel courseListPanel = new JPanel();

    private final JPanel sessionListPanel = new JPanel();

    private final JCheckBox showAllCheckBox =
            new JCheckBox("Arată tot, inclusiv sesiunile ocupate");

    private final JLabel sessionStatusLabel = new JLabel(" ");

    private CourseItem selectedCourse;
    private Integer clientId;

    public CourseBookingPanel(String userEmail) {
        this.userEmail = userEmail;

        setLayout(new BorderLayout());
        setBackground(CREAM);
        setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        JPanel card = createRoundedCard();
        card.setLayout(new BorderLayout());
        card.setBorder(
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        );

        card.add(
                createCourseListPanel(),
                BorderLayout.WEST
        );

        card.add(
                createSessionPanel(),
                BorderLayout.CENTER
        );

        add(card, BorderLayout.CENTER);

        clientId = findClientId();

        loadCourses();
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
                        30,
                        30
                );

                g.dispose();
            }
        };

        panel.setOpaque(false);
        return panel;
    }

    private JPanel createCourseListPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 24)
        );

        JLabel title = new JLabel("Cursuri");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(INK);
        title.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 18, 0)
        );

        courseListPanel.setLayout(new BoxLayout(
                courseListPanel,
                BoxLayout.Y_AXIS
        ));
        courseListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(courseListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(WHITE);

        leftPanel.add(title, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        return leftPanel;
    }

    private JPanel createSessionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);

        JLabel title = new JLabel("Sesiuni disponibile");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(INK);
        title.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 8, 0)
        );

        JLabel subtitle = new JLabel(
                "Selectează un curs din stânga pentru a vedea "
                        + "datele disponibile."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        subtitle.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 20, 0)
        );

        showAllCheckBox.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );
        showAllCheckBox.setForeground(INK);
        showAllCheckBox.setOpaque(false);
        showAllCheckBox.addActionListener(e ->
                loadSessions()
        );

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(
                topPanel,
                BoxLayout.Y_AXIS
        ));
        topPanel.add(title);
        topPanel.add(subtitle);
        topPanel.add(showAllCheckBox);

        sessionListPanel.setLayout(new BoxLayout(
                sessionListPanel,
                BoxLayout.Y_AXIS
        ));
        sessionListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(sessionListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(WHITE);

        sessionStatusLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
        sessionStatusLabel.setForeground(MUTED);
        sessionStatusLabel.setBorder(
                BorderFactory.createEmptyBorder(14, 0, 0, 0)
        );

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(sessionStatusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadCourses() {
        courseListPanel.removeAll();

        String sql = """
                SELECT id_curs, denumire, nivel
                FROM curs
                ORDER BY denumire
                """;

        List<CourseItem> courses = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                courses.add(
                        new CourseItem(
                                resultSet.getInt("id_curs"),
                                resultSet.getString("denumire"),
                                resultSet.getString("nivel")
                        )
                );
            }

        } catch (SQLException exception) {
            exception.printStackTrace();

            sessionStatusLabel.setForeground(RED);
            sessionStatusLabel.setText(
                    "Cursurile nu au putut fi încărcate."
            );
        }

        courses.sort((first, second) ->
                Integer.compare(
                        getCourseOrder(first.name()),
                        getCourseOrder(second.name())
                )
        );

        for (CourseItem course : courses) {
            courseListPanel.add(
                    createCourseButton(course)
            );
            courseListPanel.add(
                    Box.createVerticalStrut(10)
            );
        }

        courseListPanel.revalidate();
        courseListPanel.repaint();
    }

    private String normalizeCourseName(String courseName) {
        if (courseName == null) {
            return "";
        }

        return courseName
                .toLowerCase(java.util.Locale.ROOT)
                .replace("ă", "a")
                .replace("â", "a")
                .replace("î", "i")
                .replace("ș", "s")
                .replace("ş", "s")
                .replace("ț", "t")
                .replace("ţ", "t")
                .replace("nivel", "")
                .replaceAll("[^a-z0-9]", "");
    }

    private int getCourseOrder(String courseName) {
        String name = normalizeCourseName(courseName);

        if (name.contains("baza1")) {
            return 1;
        }

        if (name.contains("baza2")) {
            return 2;
        }

        if (name.contains("baza3")) {
            return 3;
        }

        if (name.contains("mastertechnician")
                || name.contains("mastertech")) {

            return 4;
        }

        if (name.contains("extreme")) {
            return 5;
        }

        if (name.contains("trainerintraining")
                || name.contains("trainersintraining")) {

            return 7;
        }

        if (name.contains("trainer")) {
            return 6;
        }

        if (name.contains("intretineri")) {
            return 8;
        }

        if (name.contains("salonwork")) {
            return 9;
        }

        return 99;
    }

    private String getCourseDisplayName(String courseName) {
        String name = courseName;

        String[] wordsToRemove = {
                "intermediar",
                "avansat",
                "avansați",
                "avansati",
                "începător",
                "incepator",
                "nivel"
        };

        for (String word : wordsToRemove) {
            name = name.replaceAll(
                    "(?i)\\b" + word + "\\b",
                    ""
            );
        }

        name = name.replaceAll("\\s*-\\s*", " - ");
        name = name.replaceAll("\\s{2,}", " ");
        name = name.trim();

        if (name.endsWith("-")) {
            name = name.substring(0, name.length() - 1).trim();
        }

        if (name.startsWith("-")) {
            name = name.substring(1).trim();
        }

        if (name.isBlank()) {
            return courseName;
        }

        return name;
    }

    private JButton createCourseButton(
            CourseItem course
    ) {
        String displayName =
                getCourseDisplayName(course.name());

        JButton button = new RoundedCourseButton(
                displayName
        );

        button.addActionListener(e ->
                selectCourse(course)
        );

        return button;
    }

    private void selectCourse(
            CourseItem course
    ) {
        selectedCourse = course;

        sessionStatusLabel.setText(" ");
        sessionStatusLabel.setForeground(MUTED);

        loadSessions();
    }

    private void loadSessions() {
        sessionListPanel.removeAll();

        if (selectedCourse == null) {
            return;
        }

        boolean showAll = showAllCheckBox.isSelected();

        String sql = """
                SELECT
                    s.id_sesiune,
                    s.data_sesiune,
                    s.durata,
                    s.pret,
                    s.nr_maxim_cursanti,
                    s.stare,
                    CONCAT(a.prenume, ' ', a.nume) AS trainer
                FROM sesiune_curs s
                JOIN trainer t
                    ON t.id_angajat = s.id_trainer
                JOIN angajat a
                    ON a.id_angajat = t.id_angajat
                WHERE s.id_curs = ?
                    AND s.stare <> 'Anulata'
                ORDER BY s.data_sesiune
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, selectedCourse.id());

            try (ResultSet resultSet = statement.executeQuery()) {
                boolean hasSessions = false;

                while (resultSet.next()) {
                    int sessionId =
                            resultSet.getInt("id_sesiune");

                    LocalDate sessionDate = null;

                    if (resultSet.getDate("data_sesiune") != null) {
                        sessionDate = resultSet
                                .getDate("data_sesiune")
                                .toLocalDate();
                    }

                    int duration =
                            resultSet.getInt("durata");

                    double price =
                            resultSet.getBigDecimal("pret")
                                    .doubleValue();

                    int maxParticipants =
                            resultSet.getInt("nr_maxim_cursanti");

                    String status =
                            resultSet.getString("stare");

                    String trainer =
                            resultSet.getString("trainer");

                    int takenSeats =
                            countActiveEnrollments(
                                    connection,
                                    sessionId
                            );

                    int availableSeats =
                            maxParticipants - takenSeats;

                    boolean isFull = availableSeats <= 0;

                    if (!showAll && isFull) {
                        continue;
                    }

                    hasSessions = true;

                    sessionListPanel.add(
                            createSessionCard(
                                    sessionId,
                                    sessionDate,
                                    duration,
                                    price,
                                    maxParticipants,
                                    availableSeats,
                                    status,
                                    trainer,
                                    isFull
                            )
                    );

                    sessionListPanel.add(
                            Box.createVerticalStrut(14)
                    );
                }

                if (!hasSessions) {
                    JLabel noSessions = new JLabel(
                            "Nu există sesiuni disponibile "
                                    + "pentru acest curs."
                    );
                    noSessions.setForeground(MUTED);
                    noSessions.setAlignmentX(
                            Component.LEFT_ALIGNMENT
                    );

                    sessionListPanel.add(noSessions);
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();

            sessionStatusLabel.setForeground(RED);
            sessionStatusLabel.setText(
                    "Sesiunile nu au putut fi încărcate."
            );
        }

        checkWaitingListNotifications();

        sessionListPanel.revalidate();
        sessionListPanel.repaint();
    }

    private int countActiveEnrollments(
            Connection connection,
            int sessionId
    ) throws SQLException {

        String sql = """
                SELECT COUNT(*) AS total
                FROM inscriere
                WHERE id_sesiune = ?
                    AND stare NOT IN ('Retrasa', 'Inlocuita')
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, sessionId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }
        }

        return 0;
    }

    private JPanel createSessionCard(
            int sessionId,
            LocalDate sessionDate,
            int duration,
            double price,
            int maxParticipants,
            int availableSeats,
            String status,
            String trainer,
            boolean isFull
    ) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(FIELD_BACKGROUND);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER,
                                1,
                                true
                        ),
                        BorderFactory.createEmptyBorder(
                                20,
                                22,
                                20,
                                22
                        )
                )
        );
        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 170)
        );
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(
                infoPanel,
                BoxLayout.Y_AXIS
        ));

        String dateText = sessionDate == null
                ? "Data nu este stabilită"
                : sessionDate.format(DATE_FORMAT);

        JLabel courseLabel = new JLabel(
                getCourseDisplayName(selectedCourse.name())
        );
        courseLabel.setFont(
                new Font("SansSerif", Font.BOLD, 17)
        );
        courseLabel.setForeground(INK);
        courseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel detailsLabel = new JLabel(
                dateText + "  •  " + duration + " min  •  "
                        + price + " RON"
        );
        detailsLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );
        detailsLabel.setForeground(MUTED);
        detailsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel trainerLabel = new JLabel(
                "Trainer: " + trainer
        );
        trainerLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );
        trainerLabel.setForeground(MUTED);
        trainerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel seatsLabel = new JLabel(
                availableSeats + " din " + maxParticipants
                        + " locuri disponibile  •  " + status
        );
        seatsLabel.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );
        seatsLabel.setForeground(
                isFull ? RED : GREEN
        );
        seatsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(courseLabel);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(detailsLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(trainerLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(seatsLabel);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0)
        );
        buttonPanel.setOpaque(false);

        if (isFull) {
            JButton waitingListButton =
                    new JButton("Intră pe lista de așteptare");

            styleSecondaryButton(waitingListButton);

            waitingListButton.addActionListener(e ->
                    joinWaitingList(sessionId)
            );

            buttonPanel.add(waitingListButton);
        } else {
            JButton enrollButton =
                    new JButton("Înscrie-te");

            stylePrimaryButton(enrollButton);

            enrollButton.addActionListener(e ->
                    openEnrollmentDialog(
                            sessionId,
                            sessionDate,
                            price
                    )
            );

            buttonPanel.add(enrollButton);
        }

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.EAST);

        return card;
    }

    private void openEnrollmentDialog(
            int sessionId,
            LocalDate sessionDate,
            double price
    ) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Înscriere la curs",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setLayout(new BorderLayout());
        dialog.setSize(540, 520);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE);
        panel.setBorder(
                BorderFactory.createEmptyBorder(30, 34, 30, 34)
        );

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("Înscriere la curs");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(INK);
        addDialogRow(panel, title, c, 0, 0, 8);

        String dateText = sessionDate == null
                ? "Data nu este stabilită"
                : sessionDate.format(DATE_FORMAT);

        JLabel details = new JLabel(
                getCourseDisplayName(selectedCourse.name())
                        + " — " + dateText
                        + " — " + price + " RON"
        );
        details.setForeground(MUTED);
        addDialogRow(panel, details, c, 1, 0, 26);

        JLabel documentLabel = new JLabel("Document de înscriere");
        documentLabel.setForeground(INK);
        addDialogRow(panel, documentLabel, c, 2, 0, 8);

        JButton chooseFileButton = new JButton("Încarcă document");

        chooseFileButton.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );
        chooseFileButton.setBackground(FIELD_BACKGROUND);
        chooseFileButton.setForeground(INK);
        chooseFileButton.setFocusPainted(false);
        chooseFileButton.setPreferredSize(
                new Dimension(220, 42)
        );
        chooseFileButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        chooseFileButton.setBorder(
                BorderFactory.createLineBorder(BORDER, 1, true)
        );

        JLabel selectedFileLabel = new JLabel(
                "Niciun document selectat."
        );
        selectedFileLabel.setForeground(MUTED);

        addDialogRow(panel, chooseFileButton, c, 3, 0, 8);
        addDialogRow(panel, selectedFileLabel, c, 4, 0, 26);

        JLabel paymentLabel = new JLabel("Modalitate de plată");
        paymentLabel.setForeground(INK);
        addDialogRow(panel, paymentLabel, c, 5, 0, 8);

        JRadioButton advancePayment =
                new JRadioButton("Plătesc avansul");

        JRadioButton fullPayment =
                new JRadioButton("Plătesc integral");

        advancePayment.setOpaque(false);
        fullPayment.setOpaque(false);
        advancePayment.setForeground(INK);
        fullPayment.setForeground(INK);

        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(advancePayment);
        paymentGroup.add(fullPayment);

        JPanel paymentPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );
        paymentPanel.setOpaque(false);
        paymentPanel.add(advancePayment);
        paymentPanel.add(Box.createHorizontalStrut(28));
        paymentPanel.add(fullPayment);

        addDialogRow(panel, paymentPanel, c, 6, 0, 30);

        JButton confirmButton = new JButton("OK");

        confirmButton.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        confirmButton.setBackground(INK);
        confirmButton.setForeground(WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.setPreferredSize(
                new Dimension(240, 46)
        );
        confirmButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        addDialogRow(panel, confirmButton, c, 7, 0, 12);

        JLabel dialogStatusLabel = new JLabel(" ");
        dialogStatusLabel.setForeground(MUTED);
        addDialogRow(panel, dialogStatusLabel, c, 8, 0, 0);

        final File[] selectedDocument = {null};

        chooseFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();

            fileChooser.setDialogTitle(
                    "Selectează documentul de înscriere"
            );

            fileChooser.setFileFilter(
                    new FileNameExtensionFilter(
                            "Imagini și PDF",
                            "jpg",
                            "jpeg",
                            "png",
                            "pdf"
                    )
            );

            int result =
                    fileChooser.showOpenDialog(dialog);

            if (result == JFileChooser.APPROVE_OPTION) {
                selectedDocument[0] =
                        fileChooser.getSelectedFile();

                selectedFileLabel.setText(
                        selectedDocument[0].getName()
                );
            }
        });

        confirmButton.addActionListener(e -> {
            if (!advancePayment.isSelected()
                    && !fullPayment.isSelected()) {

                dialogStatusLabel.setForeground(RED);
                dialogStatusLabel.setText(
                        "Selectează modalitatea de plată."
                );
                return;
            }

            String paymentChoice = advancePayment.isSelected()
                    ? "Avans"
                    : "Integral";

            Integer enrollmentId = performEnrollment(
                    sessionId,
                    sessionDate,
                    price,
                    selectedDocument[0]
            );

            if (enrollmentId == null) {
                dialogStatusLabel.setForeground(RED);
                dialogStatusLabel.setText(
                        "Înscrierea nu a putut fi creată."
                );
                return;
            }

            dialog.dispose();

            sessionStatusLabel.setForeground(GREEN);
            sessionStatusLabel.setText(
                    "Ești înscris la cursul "
                            + getCourseDisplayName(
                            selectedCourse.name()
                    )
                            + ". Modalitate de plată selectată: "
                            + paymentChoice + "."
            );

            loadSessions();
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private Integer performEnrollment(
            int sessionId,
            LocalDate sessionDate,
            double price,
            File documentFile
    ) {
        if (clientId == null) {
            return null;
        }

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                int enrollmentId;

                String insertEnrollment = """
                        INSERT INTO inscriere
                            (id_client, id_sesiune, data_cursului,
                             data_inscrierii, stare, pret_aplicat)
                        VALUES (?, ?, ?, CURDATE(), 'In_asteptare', ?)
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        insertEnrollment,
                                        Statement.RETURN_GENERATED_KEYS
                                )
                ) {
                    statement.setInt(1, clientId);
                    statement.setInt(2, sessionId);

                    if (sessionDate == null) {
                        statement.setNull(
                                3,
                                java.sql.Types.DATE
                        );
                    } else {
                        statement.setDate(
                                3,
                                java.sql.Date.valueOf(sessionDate)
                        );
                    }

                    statement.setBigDecimal(
                            4,
                            java.math.BigDecimal.valueOf(price)
                    );

                    statement.executeUpdate();

                    try (
                            ResultSet keys =
                                    statement.getGeneratedKeys()
                    ) {
                        if (keys.next()) {
                            enrollmentId = keys.getInt(1);
                        } else {
                            connection.rollback();
                            return null;
                        }
                    }
                }

                if (documentFile != null) {
                    Path documentsFolder =
                            Path.of("documents");

                    if (!Files.exists(documentsFolder)) {
                        Files.createDirectories(documentsFolder);
                    }

                    String fileName =
                            System.currentTimeMillis()
                                    + "_"
                                    + documentFile.getName();

                    Path destination =
                            documentsFolder.resolve(fileName);

                    Files.copy(
                            documentFile.toPath(),
                            destination,
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    String insertDocument = """
                            INSERT INTO document_inscriere
                                (id_inscriere, tip_document,
                                 data_depunerii, referinta_fisier)
                            VALUES (?, ?, CURDATE(), ?)
                            """;

                    try (
                            PreparedStatement documentStatement =
                                    connection.prepareStatement(
                                            insertDocument
                                    )
                    ) {
                        documentStatement.setInt(
                                1,
                                enrollmentId
                        );
                        documentStatement.setString(
                                2,
                                "Document înscriere"
                        );
                        documentStatement.setString(
                                3,
                                destination.toString()
                        );

                        documentStatement.executeUpdate();
                    }
                }

                connection.commit();

                return enrollmentId;

            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private void joinWaitingList(
            int sessionId
    ) {
        if (clientId == null) {
            showError(
                    "Contul tău de client nu a putut fi identificat."
            );
            return;
        }

        String sql = """
                INSERT INTO lista_asteptare
                    (id_client, id_sesiune, data_adaugarii)
                VALUES (?, ?, NOW())
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, clientId);
            statement.setInt(2, sessionId);

            statement.executeUpdate();

            sessionStatusLabel.setForeground(GREEN);
            sessionStatusLabel.setText(
                    "Te-ai înscris pe lista de așteptare. "
                            + "Vei fi anunțat când se eliberează un loc."
            );

            loadSessions();

        } catch (SQLException exception) {
            exception.printStackTrace();

            if (exception.getMessage() != null
                    && exception.getMessage()
                    .contains("Duplicate entry")) {

                showError(
                        "Ești deja pe lista de așteptare "
                                + "pentru această sesiune."
                );
            } else {
                showError(
                        "Nu te-ai putut înscrie pe lista de așteptare."
                );
            }
        }
    }

    private void checkWaitingListNotifications() {
        if (clientId == null) {
            return;
        }

        String sql = """
                SELECT
                    la.id_sesiune,
                    c.denumire,
                    s.data_sesiune,
                    s.nr_maxim_cursanti
                FROM lista_asteptare la
                JOIN sesiune_curs s
                    ON s.id_sesiune = la.id_sesiune
                JOIN curs c
                    ON c.id_curs = s.id_curs
                WHERE la.id_client = ?
                ORDER BY la.data_adaugarii
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, clientId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    int sessionId =
                            resultSet.getInt("id_sesiune");

                    String courseName =
                            resultSet.getString("denumire");

                    LocalDate sessionDate = null;

                    if (resultSet.getDate("data_sesiune")
                            != null) {

                        sessionDate = resultSet
                                .getDate("data_sesiune")
                                .toLocalDate();
                    }

                    int maxParticipants =
                            resultSet.getInt(
                                    "nr_maxim_cursanti"
                            );

                    int takenSeats =
                            countActiveEnrollments(
                                    connection,
                                    sessionId
                            );

                    int availableSeats =
                            maxParticipants - takenSeats;

                    if (availableSeats > 0) {
                        String dateText = sessionDate == null
                                ? ""
                                : " din "
                                + sessionDate.format(DATE_FORMAT);

                        sessionStatusLabel.setForeground(GREEN);
                        sessionStatusLabel.setText(
                                "S-a eliberat un loc la cursul "
                                        + getCourseDisplayName(courseName)
                                        + dateText
                                        + ". Poți să te înscrii acum."
                        );

                        return;
                    }
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private Integer findClientId() {
        String sql = """
                SELECT id_client
                FROM utilizator
                WHERE email = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, userEmail);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt("id_client");
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(INK);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(180, 42));
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setBackground(FIELD_BACKGROUND);
        button.setForeground(INK);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(250, 42));
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setBorder(
                BorderFactory.createLineBorder(BORDER, 1, true)
        );
    }

    private void addDialogRow(
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

    private void showError(String message) {
        sessionStatusLabel.setForeground(RED);
        sessionStatusLabel.setText(message);
    }

    private record CourseItem(
            int id,
            String name,
            String level
    ) {

        @Override
        public String toString() {
            return name;
        }
    }

    private static class RoundedCourseButton extends JButton {

        RoundedCourseButton(String text) {
            super(text);

            setFont(new Font("SansSerif", Font.PLAIN, 13));
            setForeground(INK);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            );
            setHorizontalAlignment(SwingConstants.LEFT);
            setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 42)
            );
            setPreferredSize(new Dimension(250, 42));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color background = new Color(231, 221, 205);

            if (getModel().isRollover()) {
                background = new Color(220, 208, 188);
            }

            g.setColor(background);
            g.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    40,
                    40
            );

            g.setColor(new Color(205, 192, 172));
            g.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    40,
                    40
            );

            g.dispose();

            super.paintComponent(graphics);
        }
    }
}