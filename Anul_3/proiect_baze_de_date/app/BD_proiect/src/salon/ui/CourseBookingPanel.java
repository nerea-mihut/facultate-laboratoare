package salon.ui;

import salon.db.DatabaseConnection;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CourseBookingPanel extends JPanel {

    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color INK = new Color(30, 30, 30);
    private static final Color MUTED = new Color(115, 110, 103);
    private static final Color WHITE = Color.WHITE;
    private static final Color FIELD_BACKGROUND =
            new Color(250, 249, 247);
    private static final Color BORDER =
            new Color(225, 220, 213);
    private static final Color GREEN =
            new Color(88, 150, 105);
    private static final Color RED =
            new Color(160, 45, 45);

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

        card.add(createCourseListPanel(), BorderLayout.WEST);
        card.add(createSessionPanel(), BorderLayout.CENTER);

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
                        0, 0, getWidth(), getHeight(), 30, 30
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

        JLabel title = label("Cursuri", 20, true, INK);
        title.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 18, 0)
        );

        courseListPanel.setLayout(
                new BoxLayout(courseListPanel, BoxLayout.Y_AXIS)
        );
        courseListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(courseListPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(WHITE);

        leftPanel.add(title, BorderLayout.NORTH);
        leftPanel.add(scroll, BorderLayout.CENTER);

        return leftPanel;
    }

    private JPanel createSessionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);

        JLabel title = label(
                "Sesiuni disponibile", 26, true, INK
        );
        title.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 8, 0)
        );

        JLabel subtitle = label(
                "Selectează un curs din stânga pentru a vedea "
                        + "datele disponibile.",
                13, false, MUTED
        );
        subtitle.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 20, 0)
        );

        showAllCheckBox.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );
        showAllCheckBox.setForeground(INK);
        showAllCheckBox.setOpaque(false);
        showAllCheckBox.addActionListener(e -> loadSessions());

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(
                new BoxLayout(topPanel, BoxLayout.Y_AXIS)
        );

        topPanel.add(title);
        topPanel.add(subtitle);
        topPanel.add(showAllCheckBox);

        sessionListPanel.setLayout(
                new BoxLayout(sessionListPanel, BoxLayout.Y_AXIS)
        );
        sessionListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(sessionListPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(WHITE);

        sessionStatusLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
        sessionStatusLabel.setForeground(MUTED);
        sessionStatusLabel.setBorder(
                BorderFactory.createEmptyBorder(14, 0, 0, 0)
        );

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
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
                ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                courses.add(new CourseItem(
                        result.getInt("id_curs"),
                        result.getString("denumire"),
                        result.getString("nivel")
                ));
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            showError("Cursurile nu au putut fi încărcate.");
        }

        courses.sort(
                Comparator.comparingInt(
                        course -> getCourseOrder(course.name())
                )
        );

        for (CourseItem course : courses) {
            JButton button = new RoundedCourseButton(
                    getCourseDisplayName(course.name())
            );

            button.addActionListener(e -> {
                selectedCourse = course;
                loadSessions();
            });

            courseListPanel.add(button);
            courseListPanel.add(Box.createVerticalStrut(10));
        }

        courseListPanel.revalidate();
        courseListPanel.repaint();
    }

    private String normalizeCourseName(String name) {
        if (name == null) {
            return "";
        }

        return name.toLowerCase(Locale.ROOT)
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

        if (name.contains("baza1")) return 1;
        if (name.contains("baza2")) return 2;
        if (name.contains("baza3")) return 3;

        if (name.contains("mastertechnician")
                || name.contains("mastertech")) {
            return 4;
        }

        if (name.contains("extreme")) return 5;

        if (name.contains("trainerintraining")
                || name.contains("trainersintraining")) {
            return 7;
        }

        if (name.contains("trainer")) return 6;
        if (name.contains("intretineri")) return 8;
        if (name.contains("salonwork")) return 9;

        return 99;
    }

    private String getCourseDisplayName(String courseName) {
        if (courseName == null) {
            return "";
        }

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
                    "(?iu)\\b" + word + "\\b", ""
            );
        }

        name = name.replaceAll("\\s*-\\s*", " - ")
                .replaceAll("\\s{2,}", " ")
                .trim();

        if (name.endsWith("-")) {
            name = name.substring(0, name.length() - 1).trim();
        }

        if (name.startsWith("-")) {
            name = name.substring(1).trim();
        }

        return name.isBlank() ? courseName : name;
    }

    public void refreshData() {
        clientId = findClientId();

        if (selectedCourse != null) {
            loadSessions();
        } else {
            loadCourses();
        }
    }

    private void loadSessions() {
        sessionListPanel.removeAll();
        sessionStatusLabel.setText(" ");
        sessionStatusLabel.setForeground(MUTED);

        if (selectedCourse == null) {
            sessionListPanel.revalidate();
            sessionListPanel.repaint();
            return;
        }

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
                ORDER BY s.data_sesiune, s.id_sesiune
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, selectedCourse.id());

            try (ResultSet result = statement.executeQuery()) {
                boolean hasSessions = false;

                while (result.next()) {
                    int sessionId = result.getInt("id_sesiune");

                    java.sql.Date storedDate =
                            result.getDate("data_sesiune");

                    LocalDate sessionDate = storedDate == null
                            ? null
                            : storedDate.toLocalDate();

                    int maxParticipants =
                            result.getInt("nr_maxim_cursanti");

                    int takenSeats = countActiveEnrollments(
                            connection, sessionId
                    );

                    int availableSeats = Math.max(
                            0, maxParticipants - takenSeats
                    );

                    boolean full = availableSeats == 0;

                    if (!showAllCheckBox.isSelected() && full) {
                        continue;
                    }

                    hasSessions = true;

                    sessionListPanel.add(createSessionCard(
                            connection,
                            sessionId,
                            sessionDate,
                            result.getInt("durata"),
                            result.getBigDecimal("pret"),
                            maxParticipants,
                            availableSeats,
                            result.getString("stare"),
                            result.getString("trainer")
                    ));

                    sessionListPanel.add(
                            Box.createVerticalStrut(14)
                    );
                }

                if (!hasSessions) {
                    JLabel empty = label(
                            "Nu există sesiuni disponibile pentru acest curs.",
                            13, false, MUTED
                    );
                    empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                    sessionListPanel.add(empty);
                }
            }

            checkWaitingListNotifications();

        } catch (SQLException exception) {
            exception.printStackTrace();
            showError("Sesiunile nu au putut fi încărcate.");
        }

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

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt("total") : 0;
            }
        }
    }

    private String findEnrollmentStatus(
            Connection connection,
            int sessionId
    ) throws SQLException {

        if (clientId == null) {
            return null;
        }

        String sql = """
                SELECT stare
                FROM inscriere
                WHERE id_client = ?
                  AND id_sesiune = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, clientId);
            statement.setInt(2, sessionId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? result.getString("stare")
                        : null;
            }
        }
    }

    private boolean isOnWaitingList(
            Connection connection,
            int sessionId
    ) throws SQLException {

        if (clientId == null) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM lista_asteptare
                WHERE id_client = ?
                  AND id_sesiune = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, clientId);
            statement.setInt(2, sessionId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private JPanel createSessionCard(
            Connection connection,
            int sessionId,
            LocalDate sessionDate,
            int duration,
            BigDecimal price,
            int maxParticipants,
            int availableSeats,
            String status,
            String trainer
    ) throws SQLException {

        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(FIELD_BACKGROUND);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER, 1, true
                        ),
                        BorderFactory.createEmptyBorder(
                                20, 22, 20, 22
                        )
                )
        );

        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 170)
        );
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        String dateText = sessionDate == null
                ? "Data nu este stabilită"
                : sessionDate.format(DATE_FORMAT);

        JLabel courseLabel = label(
                getCourseDisplayName(selectedCourse.name()),
                17, true, INK
        );

        JLabel detailsLabel = label(
                dateText + "  •  " + duration + " min  •  "
                        + price.stripTrailingZeros().toPlainString()
                        + " RON",
                13, false, MUTED
        );

        JLabel trainerLabel = label(
                "Trainer: " + trainer,
                13, false, MUTED
        );

        JLabel seatsLabel = label(
                availableSeats + " din " + maxParticipants
                        + " locuri disponibile  •  " + status,
                13, true,
                availableSeats == 0 ? RED : GREEN
        );

        for (JLabel item : new JLabel[]{
                courseLabel, detailsLabel, trainerLabel, seatsLabel
        }) {
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        info.add(courseLabel);
        info.add(Box.createVerticalStrut(6));
        info.add(detailsLabel);
        info.add(Box.createVerticalStrut(4));
        info.add(trainerLabel);
        info.add(Box.createVerticalStrut(8));
        info.add(seatsLabel);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0)
        );
        buttons.setOpaque(false);

        String enrollmentStatus =
                findEnrollmentStatus(connection, sessionId);

        boolean waiting =
                isOnWaitingList(connection, sessionId);

        boolean bookable = "Planificata".equals(status)
                && (sessionDate == null
                || !sessionDate.isBefore(LocalDate.now()));

        JButton action;

        if (enrollmentStatus != null) {
            action = new JButton(
                    "Înscriere: " + enrollmentStatus
            );
            styleSecondaryButton(action);
            action.setEnabled(false);

        } else if (!bookable) {
            action = new JButton("Înscrieri indisponibile");
            styleSecondaryButton(action);
            action.setEnabled(false);

        } else if (availableSeats == 0) {
            action = new JButton(
                    waiting
                            ? "Ești pe lista de așteptare"
                            : "Intră pe lista de așteptare"
            );
            styleSecondaryButton(action);
            action.setEnabled(!waiting && clientId != null);
            action.addActionListener(
                    e -> joinWaitingList(sessionId)
            );

        } else {
            action = new JButton("Înscrie-te");
            stylePrimaryButton(action);
            action.setEnabled(clientId != null);
            action.addActionListener(e ->
                    openEnrollmentDialog(
                            sessionId, sessionDate, price
                    )
            );
        }

        buttons.add(action);

        card.add(info, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.EAST);

        return card;
    }

    private void openEnrollmentDialog(
            int sessionId,
            LocalDate sessionDate,
            BigDecimal price
    ) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Înscriere la curs",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE
        );
        dialog.setSize(540, 560);
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

        addDialogRow(
                panel,
                label("Înscriere la curs", 24, true, INK),
                c, 0, 0, 8
        );

        String dateText = sessionDate == null
                ? "Data nu este stabilită"
                : sessionDate.format(DATE_FORMAT);

        JLabel details = label(
                "<html>"
                        + escape(getCourseDisplayName(
                        selectedCourse.name()
                ))
                        + "<br>" + dateText
                        + " — "
                        + price.stripTrailingZeros().toPlainString()
                        + " RON</html>",
                13, false, MUTED
        );

        addDialogRow(panel, details, c, 1, 0, 26);

        addDialogRow(
                panel,
                label("Document de înscriere", 13, false, INK),
                c, 2, 0, 8
        );

        JButton chooseFileButton =
                new JButton("Încarcă document");
        styleSecondaryButton(chooseFileButton);

        JLabel selectedFileLabel = label(
                "Niciun document selectat.",
                12, false, MUTED
        );

        addDialogRow(
                panel, chooseFileButton, c, 3, 0, 8
        );
        addDialogRow(
                panel, selectedFileLabel, c, 4, 0, 26
        );

        addDialogRow(
                panel,
                label("Modalitate de plată", 13, false, INK),
                c, 5, 0, 8
        );

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

        addDialogRow(
                panel, paymentPanel, c, 6, 0, 30
        );

        JButton confirmButton = new JButton("OK");
        stylePrimaryButton(confirmButton);
        confirmButton.setPreferredSize(new Dimension(240, 46));

        addDialogRow(
                panel, confirmButton, c, 7, 0, 12
        );

        JLabel dialogStatusLabel = label(
                " ", 12, false, MUTED
        );

        addDialogRow(
                panel, dialogStatusLabel, c, 8, 0, 0
        );

        File[] selectedDocument = {null};

        chooseFileButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(
                    "Selectează documentul de înscriere"
            );
            chooser.setFileFilter(
                    new FileNameExtensionFilter(
                            "Imagini și PDF",
                            "jpg", "jpeg", "png", "pdf"
                    )
            );

            if (chooser.showOpenDialog(dialog)
                    == JFileChooser.APPROVE_OPTION) {

                selectedDocument[0] = chooser.getSelectedFile();
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

            File document = selectedDocument[0];

            confirmButton.setEnabled(false);
            chooseFileButton.setEnabled(false);
            advancePayment.setEnabled(false);
            fullPayment.setEnabled(false);

            dialog.setDefaultCloseOperation(
                    WindowConstants.DO_NOTHING_ON_CLOSE
            );

            dialogStatusLabel.setForeground(MUTED);
            dialogStatusLabel.setText("Se salvează înscrierea...");

            new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground()
                        throws Exception {

                    return performEnrollment(
                            sessionId,
                            document,
                            paymentChoice
                    );
                }

                @Override
                protected void done() {
                    confirmButton.setEnabled(true);
                    chooseFileButton.setEnabled(true);
                    advancePayment.setEnabled(true);
                    fullPayment.setEnabled(true);

                    dialog.setDefaultCloseOperation(
                            WindowConstants.DISPOSE_ON_CLOSE
                    );

                    try {
                        get();

                        dialog.dispose();
                        loadSessions();

                        sessionStatusLabel.setForeground(GREEN);
                        sessionStatusLabel.setText(
                                "Înscrierea a fost salvată. "
                                        + "Opțiune de plată: "
                                        + paymentChoice
                                        + ". Vezi Activitatea mea."
                        );

                    } catch (Exception exception) {
                        Throwable cause =
                                exception.getCause() != null
                                        ? exception.getCause()
                                        : exception;

                        String message;

                        if (cause instanceof EnrollmentException) {
                            message = cause.getMessage();

                        } else if (cause instanceof SQLException sql
                                && sql.getErrorCode() == 1062) {

                            message = "Există deja o înscriere "
                                    + "pentru această sesiune.";

                        } else {
                            cause.printStackTrace();
                            message = "Înscrierea nu a putut fi "
                                    + "salvată. Verifică eroarea "
                                    + "din consolă.";
                        }

                        dialogStatusLabel.setForeground(RED);
                        dialogStatusLabel.setText(
                                "<html>" + escape(message) + "</html>"
                        );
                    }
                }
            }.execute();
        });

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private Integer performEnrollment(
            int sessionId,
            File documentFile,
            String paymentChoice
    ) throws Exception {

        if (clientId == null || clientId <= 0) {
            throw new EnrollmentException(
                    "Contul de client nu a putut fi identificat."
            );
        }

        if (!"Avans".equals(paymentChoice)
                && !"Integral".equals(paymentChoice)) {
            throw new EnrollmentException(
                    "Selectează o opțiune de plată validă."
            );
        }

        Path copiedDocument = null;
        boolean committed = false;

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                int maxParticipants;
                String status;
                LocalDate actualDate;
                BigDecimal actualPrice;

                String lockSql = """
                        SELECT
                            nr_maxim_cursanti,
                            stare,
                            data_sesiune,
                            pret
                        FROM sesiune_curs
                        WHERE id_sesiune = ?
                        FOR UPDATE
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(lockSql)
                ) {
                    statement.setInt(1, sessionId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new EnrollmentException(
                                    "Sesiunea nu mai există."
                            );
                        }

                        maxParticipants =
                                result.getInt("nr_maxim_cursanti");
                        status = result.getString("stare");
                        actualPrice = result.getBigDecimal("pret");

                        java.sql.Date date =
                                result.getDate("data_sesiune");

                        actualDate = date == null
                                ? null
                                : date.toLocalDate();
                    }
                }

                if (!"Planificata".equals(status)) {
                    throw new EnrollmentException(
                            "Înscrierile sunt disponibile doar "
                                    + "pentru sesiuni planificate."
                    );
                }

                if (actualDate != null
                        && actualDate.isBefore(LocalDate.now())) {
                    throw new EnrollmentException(
                            "Nu te poți înscrie la o sesiune "
                                    + "cu data în trecut."
                    );
                }

                String existingSql = """
                        SELECT stare
                        FROM inscriere
                        WHERE id_client = ?
                          AND id_sesiune = ?
                        FOR UPDATE
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(existingSql)
                ) {
                    statement.setInt(1, clientId);
                    statement.setInt(2, sessionId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            throw new EnrollmentException(
                                    "Ai deja o înscriere cu starea "
                                            + result.getString("stare")
                                            + ". Verifică Activitatea mea."
                            );
                        }
                    }
                }

                int takenSeats = 0;

                String occupiedSql = """
                        SELECT id_inscriere
                        FROM inscriere
                        WHERE id_sesiune = ?
                          AND stare NOT IN ('Retrasa', 'Inlocuita')
                        FOR UPDATE
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(occupiedSql)
                ) {
                    statement.setInt(1, sessionId);

                    try (ResultSet result = statement.executeQuery()) {
                        while (result.next()) {
                            takenSeats++;
                        }
                    }
                }

                if (takenSeats >= maxParticipants) {
                    throw new EnrollmentException(
                            "Sesiunea este complet ocupată. "
                                    + "Închide formularul și bifează "
                                    + "«Arată tot» pentru a accesa "
                                    + "lista de așteptare."
                    );
                }

                int enrollmentId;

                String insertSql = """
                        INSERT INTO inscriere
                            (
                                id_client,
                                id_sesiune,
                                data_cursului,
                                data_inscrierii,
                                stare,
                                pret_aplicat,
                                metoda_plata
                            )
                        VALUES (
                            ?, ?, ?, CURDATE(),
                            'In_asteptare', ?, ?
                        )
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        insertSql,
                                        Statement.RETURN_GENERATED_KEYS
                                )
                ) {
                    statement.setInt(1, clientId);
                    statement.setInt(2, sessionId);

                    if (actualDate == null) {
                        statement.setNull(3, Types.DATE);
                    } else {
                        statement.setDate(
                                3,
                                java.sql.Date.valueOf(actualDate)
                        );
                    }

                    statement.setBigDecimal(4, actualPrice);
                    statement.setString(5, paymentChoice);
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Nu s-a obținut ID-ul înscrierii."
                            );
                        }
                        enrollmentId = keys.getInt(1);
                    }
                }

                if (documentFile != null) {
                    Path folder = Path.of("documents");
                    Files.createDirectories(folder);

                    copiedDocument = folder.resolve(
                            UUID.randomUUID()
                                    + "_"
                                    + documentFile.getName()
                    );

                    Files.copy(
                            documentFile.toPath(),
                            copiedDocument
                    );

                    String documentSql = """
                            INSERT INTO document_inscriere
                                (
                                    id_inscriere,
                                    tip_document,
                                    data_depunerii,
                                    referinta_fisier
                                )
                            VALUES (?, ?, CURDATE(), ?)
                            """;

                    try (
                            PreparedStatement statement =
                                    connection.prepareStatement(
                                            documentSql
                                    )
                    ) {
                        statement.setInt(1, enrollmentId);
                        statement.setString(2, "Document înscriere");
                        statement.setString(
                                3, copiedDocument.toString()
                        );
                        statement.executeUpdate();
                    }
                }

                String removeWaitingSql = """
                        DELETE FROM lista_asteptare
                        WHERE id_client = ?
                          AND id_sesiune = ?
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        removeWaitingSql
                                )
                ) {
                    statement.setInt(1, clientId);
                    statement.setInt(2, sessionId);
                    statement.executeUpdate();
                }

                connection.commit();
                committed = true;

                return enrollmentId;

            } catch (Exception exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }

        } finally {
            if (!committed && copiedDocument != null) {
                try {
                    Files.deleteIfExists(copiedDocument);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    private void joinWaitingList(int sessionId) {
        if (clientId == null) {
            showError(
                    "Contul tău de client nu a putut fi identificat."
            );
            return;
        }

        String sql = """
                INSERT INTO lista_asteptare
                    (id_client, id_sesiune, data_adaugarii)
                SELECT ?, s.id_sesiune, NOW()
                FROM sesiune_curs s
                WHERE s.id_sesiune = ?
                  AND s.stare = 'Planificata'
                  AND (
                      s.data_sesiune IS NULL
                      OR s.data_sesiune >= CURDATE()
                  )
                  AND NOT EXISTS (
                      SELECT 1
                      FROM inscriere i
                      WHERE i.id_client = ?
                        AND i.id_sesiune = s.id_sesiune
                  )
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, clientId);
            statement.setInt(2, sessionId);
            statement.setInt(3, clientId);

            int inserted = statement.executeUpdate();

            loadSessions();

            if (inserted == 0) {
                showError(
                        "Sesiunea nu mai permite această acțiune "
                                + "sau ai deja o înscriere."
                );
                return;
            }

            sessionStatusLabel.setForeground(GREEN);
            sessionStatusLabel.setText(
                    "Te-ai înscris pe lista de așteptare. "
                            + "Vezi sesiunea în Activitatea mea."
            );

        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1062) {
                sessionStatusLabel.setForeground(MUTED);
                sessionStatusLabel.setText(
                        "Ești deja pe lista de așteptare "
                                + "pentru această sesiune."
                );
            } else {
                exception.printStackTrace();
                showError(
                        "Înscrierea pe lista de așteptare "
                                + "nu a putut fi salvată."
                );
            }
        }
    }

    private void checkWaitingListNotifications()
            throws SQLException {

        if (clientId == null) {
            return;
        }

        String sql = """
                SELECT
                    s.id_sesiune,
                    c.denumire,
                    s.data_sesiune,
                    s.nr_maxim_cursanti
                FROM lista_asteptare la
                JOIN sesiune_curs s
                    ON s.id_sesiune = la.id_sesiune
                JOIN curs c
                    ON c.id_curs = s.id_curs
                WHERE la.id_client = ?
                  AND s.stare = 'Planificata'
                  AND (
                      s.data_sesiune IS NULL
                      OR s.data_sesiune >= CURDATE()
                  )
                  AND NOT EXISTS (
                      SELECT 1
                      FROM inscriere i
                      WHERE i.id_client = la.id_client
                        AND i.id_sesiune = la.id_sesiune
                  )
                ORDER BY la.data_adaugarii
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, clientId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int availableSeats = Math.max(
                            0,
                            result.getInt("nr_maxim_cursanti")
                                    - countActiveEnrollments(
                                    connection,
                                    result.getInt("id_sesiune")
                            )
                    );

                    if (availableSeats == 0) {
                        continue;
                    }

                    java.sql.Date date =
                            result.getDate("data_sesiune");

                    String dateText = date == null
                            ? ""
                            : " din " + date.toLocalDate()
                            .format(DATE_FORMAT);

                    sessionStatusLabel.setForeground(GREEN);
                    sessionStatusLabel.setText(
                            "<html>Există un loc disponibil la cursul "
                                    + escape(getCourseDisplayName(
                                    result.getString("denumire")
                            ))
                                    + dateText
                                    + ". Poți să te înscrii.</html>"
                    );
                    return;
                }
            }
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

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    int id = result.getInt("id_client");

                    if (!result.wasNull() && id > 0) {
                        return id;
                    }
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            showError(
                    "Contul de client nu a putut fi încărcat."
            );
        }

        return null;
    }

    private JLabel label(
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

    private void stylePrimaryButton(JButton button) {
        button.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );
        button.setBackground(INK);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(180, 42));
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.putClientProperty("JButton.arc", 18);
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );
        button.setBackground(FIELD_BACKGROUND);
        button.setForeground(INK);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(250, 42));
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setBorder(
                BorderFactory.createLineBorder(
                        BORDER, 1, true
                )
        );
        button.putClientProperty("JButton.arc", 18);
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
        sessionStatusLabel.setText(
                "<html>" + escape(message) + "</html>"
        );
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private record CourseItem(
            int id,
            String name,
            String level
    ) {
    }

    private static class EnrollmentException extends Exception {
        EnrollmentException(String message) {
            super(message);
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

            g.setColor(
                    getModel().isRollover()
                            ? new Color(220, 208, 188)
                            : new Color(231, 221, 205)
            );

            g.fillRoundRect(
                    0, 0, getWidth(), getHeight(), 40, 40
            );

            g.setColor(new Color(205, 192, 172));
            g.drawRoundRect(
                    0, 0,
                    getWidth() - 1,
                    getHeight() - 1,
                    40, 40
            );

            g.dispose();
            super.paintComponent(graphics);
        }
    }
}