package salon.ui;

import salon.db.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppointmentBookingPanel extends JPanel {

    private static final Color CREAM =
            new Color(244, 240, 233);

    private static final Color INK =
            new Color(30, 30, 30);

    private static final Color MUTED =
            new Color(115, 110, 103);

    private static final Color WHITE = Color.WHITE;

    private static final Color FIELD =
            new Color(250, 249, 247);

    private static final Color BORDER =
            new Color(225, 220, 213);

    private static final Color BEIGE =
            new Color(231, 221, 205);

    private static final Color HOVER =
            new Color(220, 208, 188);

    private static final Color GREEN =
            new Color(88, 150, 105);

    private static final Color RED =
            new Color(160, 45, 45);

    private static final Locale RO =
            Locale.forLanguageTag("ro-RO");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final String userEmail;

    private final JPanel technicianListPanel =
            new JPanel();

    private final JPanel rightPanel =
            new JPanel(new CardLayout());

    private final JPanel typePanel =
            new JPanel(new GridLayout(2, 2, 10, 10));

    private final JPanel variantPanel =
            new JPanel(new GridLayout(0, 3, 10, 10));

    private final JPanel designPanel =
            new JPanel(new GridLayout(0, 2, 10, 10));

    private final JPanel extraPanel =
            new JPanel(new GridLayout(0, 1, 0, 8));

    private final JPanel calendarGrid =
            new JPanel(new GridLayout(7, 7, 7, 7));

    private final JPanel hoursPanel =
            new JPanel(new GridLayout(0, 4, 10, 10));

    private JPanel extraSection;

    private final JLabel technicianTitle =
            new JLabel("Alege o tehniciană");

    private final JLabel variantTitle =
            new JLabel("Opțiuni");

    private final JLabel serviceDescription =
            new JLabel(" ");

    private final JLabel selectionLabel =
            new JLabel(" ");

    private final JLabel totalLabel =
            new JLabel("Total: —");

    private final JLabel statusLabel =
            new JLabel(" ");

    private final JLabel monthLabel =
            new JLabel("", SwingConstants.CENTER);

    private final JLabel calendarSummaryLabel =
            new JLabel(" ");

    private final JLabel hoursTitle =
            new JLabel(" ");

    private final JButton availabilityButton =
            new JButton("Vezi disponibilitatea");

    private final List<ServiceItem> offeredServices =
            new ArrayList<>();

    private final List<TechnicianChoice> technicianChoices =
            new ArrayList<>();

    private TechnicianItem selectedTechnician;

    private ServiceItem baseService;
    private ServiceItem designService;
    private ServiceItem extraService;

    private String selectedType;

    private YearMonth displayedMonth = YearMonth.now();

    private LocalDate selectedDate;

    private boolean bookingBusy;

    public AppointmentBookingPanel(String userEmail) {
        this.userEmail = userEmail;

        setLayout(new BorderLayout());
        setBackground(CREAM);

        setBorder(
                BorderFactory.createEmptyBorder(
                        20, 20, 20, 20
                )
        );

        JPanel card = new RoundedPanel(WHITE, 28);

        card.setLayout(new BorderLayout(24, 0));

        card.setBorder(
                BorderFactory.createEmptyBorder(
                        24, 24, 24, 24
                )
        );

        card.add(createLeftPanel(), BorderLayout.WEST);

        rightPanel.setOpaque(false);

        rightPanel.add(
                createServiceView(),
                "SERVICES"
        );

        rightPanel.add(
                createCalendarView(),
                "CALENDAR"
        );

        card.add(rightPanel, BorderLayout.CENTER);

        statusLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        statusLabel.setForeground(MUTED);

        statusLabel.setBorder(
                BorderFactory.createEmptyBorder(
                        12, 4, 0, 4
                )
        );

        add(card, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        loadTechnicians();
        rebuildChoices();
        showServices();
    }

    private JPanel createLeftPanel() {
        JPanel panel =
                new JPanel(new BorderLayout(0, 16));

        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(226, 0));

        panel.add(
                label("Tehnicieni", 18, true, INK),
                BorderLayout.NORTH
        );

        technicianListPanel.setLayout(
                new BoxLayout(
                        technicianListPanel,
                        BoxLayout.Y_AXIS
                )
        );

        technicianListPanel.setOpaque(false);

        panel.add(
                scroll(technicianListPanel),
                BorderLayout.CENTER
        );

        stylePrimary(availabilityButton);

        availabilityButton.setPreferredSize(
                new Dimension(226, 44)
        );

        availabilityButton.addActionListener(
                event -> showCalendar()
        );

        panel.add(
                availabilityButton,
                BorderLayout.SOUTH
        );

        return panel;
    }

    private JPanel createServiceView() {
        JPanel view =
                new JPanel(new BorderLayout(0, 16));

        view.setOpaque(false);

        JPanel heading =
                new JPanel(new GridLayout(0, 1, 0, 5));

        heading.setOpaque(false);

        technicianTitle.setFont(
                new Font("SansSerif", Font.BOLD, 25)
        );

        technicianTitle.setForeground(INK);

        heading.add(technicianTitle);

        heading.add(
                label(
                        "Alege serviciul și personalizează programarea.",
                        13,
                        false,
                        MUTED
                )
        );

        view.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel();

        form.setOpaque(false);

        form.setLayout(
                new BoxLayout(form, BoxLayout.Y_AXIS)
        );

        form.add(section("Serviciu", typePanel));
        form.add(Box.createVerticalStrut(17));

        JPanel variants = section("", variantPanel);

        variantTitle.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        variantTitle.setForeground(INK);

        variants.add(
                variantTitle,
                BorderLayout.NORTH
        );

        form.add(variants);
        form.add(Box.createVerticalStrut(10));

        serviceDescription.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        serviceDescription.setForeground(MUTED);

        JPanel descriptionPanel =
                new JPanel(new BorderLayout()) {

                    @Override
                    public Dimension getMaximumSize() {
                        return new Dimension(
                                Integer.MAX_VALUE,
                                getPreferredSize().height
                        );
                    }
                };

        descriptionPanel.setOpaque(false);

        descriptionPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        descriptionPanel.add(
                serviceDescription,
                BorderLayout.CENTER
        );

        form.add(descriptionPanel);
        form.add(Box.createVerticalStrut(17));

        form.add(section("Design", designPanel));
        form.add(Box.createVerticalStrut(17));

        extraSection = section(
                "Extraopțiuni pentru întreținere",
                extraPanel
        );

        form.add(extraSection);

        view.add(
                scroll(form),
                BorderLayout.CENTER
        );

        JPanel summary = new RoundedPanel(FIELD, 18);

        summary.setLayout(new BorderLayout(12, 0));

        summary.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 18, 15, 18
                )
        );

        selectionLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        selectionLabel.setForeground(MUTED);

        totalLabel.setFont(
                new Font("SansSerif", Font.BOLD, 19)
        );

        totalLabel.setForeground(INK);

        summary.add(
                selectionLabel,
                BorderLayout.CENTER
        );

        summary.add(
                totalLabel,
                BorderLayout.EAST
        );

        view.add(
                summary,
                BorderLayout.SOUTH
        );

        return view;
    }

    private JPanel createCalendarView() {
        JPanel view =
                new JPanel(new BorderLayout(0, 16));

        view.setOpaque(false);

        JPanel heading =
                new JPanel(new BorderLayout(12, 0));

        heading.setOpaque(false);

        JPanel titles =
                new JPanel(new GridLayout(0, 1, 0, 5));

        titles.setOpaque(false);

        titles.add(
                label(
                        "Alege ziua",
                        25,
                        true,
                        INK
                )
        );

        calendarSummaryLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        calendarSummaryLabel.setForeground(MUTED);

        titles.add(calendarSummaryLabel);

        JButton back =
                new JButton("Modifică serviciul");

        styleSecondary(back);

        back.addActionListener(event -> {
            if (!bookingBusy) {
                showServices();
            }
        });

        heading.add(titles, BorderLayout.CENTER);
        heading.add(back, BorderLayout.EAST);

        view.add(heading, BorderLayout.NORTH);

        JPanel body = new JPanel();

        body.setOpaque(false);

        body.setLayout(
                new BoxLayout(body, BoxLayout.Y_AXIS)
        );

        JPanel navigation =
                new JPanel(new BorderLayout(12, 0));

        navigation.setOpaque(false);

        JButton previous = new JButton("<");
        JButton next = new JButton(">");

        styleSecondary(previous);
        styleSecondary(next);

        previous.setPreferredSize(
                new Dimension(42, 36)
        );

        next.setPreferredSize(
                new Dimension(42, 36)
        );

        previous.addActionListener(event -> {
            if (!bookingBusy) {
                displayedMonth =
                        displayedMonth.minusMonths(1);

                refreshCalendar();
            }
        });

        next.addActionListener(event -> {
            if (!bookingBusy) {
                displayedMonth =
                        displayedMonth.plusMonths(1);

                refreshCalendar();
            }
        });

        monthLabel.setFont(
                new Font("SansSerif", Font.BOLD, 18)
        );

        monthLabel.setForeground(INK);

        navigation.add(
                previous,
                BorderLayout.WEST
        );

        navigation.add(
                monthLabel,
                BorderLayout.CENTER
        );

        navigation.add(
                next,
                BorderLayout.EAST
        );

        navigation.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38)
        );

        navigation.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        body.add(navigation);
        body.add(Box.createVerticalStrut(12));

        calendarGrid.setOpaque(false);

        calendarGrid.setPreferredSize(
                new Dimension(500, 300)
        );

        calendarGrid.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 320)
        );

        calendarGrid.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        body.add(calendarGrid);
        body.add(Box.createVerticalStrut(18));

        hoursTitle.setFont(
                new Font("SansSerif", Font.BOLD, 16)
        );

        hoursTitle.setForeground(INK);

        hoursTitle.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        body.add(hoursTitle);
        body.add(Box.createVerticalStrut(10));

        hoursPanel.setOpaque(false);

        JPanel hoursWrapper =
                new JPanel(new BorderLayout());

        hoursWrapper.setOpaque(false);

        hoursWrapper.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        hoursWrapper.add(
                hoursPanel,
                BorderLayout.NORTH
        );

        body.add(hoursWrapper);

        view.add(
                scroll(body),
                BorderLayout.CENTER
        );

        return view;
    }

    private JPanel section(
            String title,
            JPanel contents
    ) {
        JPanel panel =
                new JPanel(new BorderLayout(0, 9)) {

                    @Override
                    public Dimension getMaximumSize() {
                        return new Dimension(
                                Integer.MAX_VALUE,
                                getPreferredSize().height
                        );
                    }
                };

        panel.setOpaque(false);

        panel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        contents.setOpaque(false);

        panel.add(
                label(title, 13, true, INK),
                BorderLayout.NORTH
        );

        panel.add(
                contents,
                BorderLayout.CENTER
        );

        return panel;
    }

    private JScrollPane scroll(JPanel panel) {
        JScrollPane result =
                new JScrollPane(panel);

        result.setBorder(null);
        result.setOpaque(false);
        result.getViewport().setOpaque(false);

        result.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        result.getVerticalScrollBar()
                .setUnitIncrement(18);

        return result;
    }

    private JLabel label(
            String text,
            int size,
            boolean bold,
            Color color
    ) {
        JLabel result = new JLabel(text);

        result.setFont(
                new Font(
                        "SansSerif",
                        bold ? Font.BOLD : Font.PLAIN,
                        size
                )
        );

        result.setForeground(color);

        return result;
    }

    private void loadTechnicians() {
        technicianListPanel.removeAll();
        technicianChoices.clear();

        String sql = """
                SELECT
                    a.id_angajat,
                    CONCAT(a.prenume, ' ', a.nume) AS nume_complet
                FROM angajat a
                JOIN tehnician t
                    ON t.id_angajat = a.id_angajat
                ORDER BY a.prenume, a.nume
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result =
                        statement.executeQuery()
        ) {
            while (result.next()) {
                TechnicianItem technician =
                        new TechnicianItem(
                                result.getInt("id_angajat"),
                                result.getString("nume_complet")
                        );

                ChoiceButton button =
                        new ChoiceButton(
                                technician.name()
                        );

                button.setHorizontalAlignment(
                        SwingConstants.LEFT
                );

                button.setPreferredSize(
                        new Dimension(220, 40)
                );

                button.setMinimumSize(
                        new Dimension(160, 40)
                );

                button.setMaximumSize(
                        new Dimension(Integer.MAX_VALUE, 40)
                );

                button.setAlignmentX(
                        Component.LEFT_ALIGNMENT
                );

                button.addActionListener(event -> {
                    if (!bookingBusy) {
                        selectTechnician(technician);
                    }
                });

                technicianChoices.add(
                        new TechnicianChoice(
                                technician,
                                button
                        )
                );

                technicianListPanel.add(button);

                technicianListPanel.add(
                        Box.createVerticalStrut(8)
                );
            }

        } catch (SQLException exception) {
            exception.printStackTrace();

            showError(
                    "Tehnicienii nu au putut fi încărcați."
            );
        }

        technicianListPanel.revalidate();
        technicianListPanel.repaint();
    }

    private void selectTechnician(
            TechnicianItem technician
    ) {
        selectedTechnician = technician;

        selectedType = null;
        baseService = null;
        designService = null;
        extraService = null;
        selectedDate = null;

        offeredServices.clear();

        technicianTitle.setText(
                technician.name()
        );

        statusLabel.setForeground(MUTED);
        statusLabel.setText(" ");

        for (TechnicianChoice choice : technicianChoices) {
            choice.button().setSelected(
                    choice.technician().id()
                            == technician.id()
            );
        }

        String sql = """
                SELECT
                    s.id_serviciu,
                    s.denumire,
                    s.categorie,
                    ts.durata,
                    ts.pret
                FROM tehnician_serviciu ts
                JOIN serviciu s
                    ON s.id_serviciu = ts.id_serviciu
                WHERE ts.id_tehnician = ?
                  AND ts.stare_oferta = 'Activa'
                ORDER BY s.id_serviciu
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(
                    1,
                    technician.id()
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                while (result.next()) {
                    offeredServices.add(
                            new ServiceItem(
                                    result.getInt("id_serviciu"),
                                    result.getString("denumire"),
                                    result.getString("categorie"),
                                    result.getInt("durata"),
                                    result.getBigDecimal("pret"),
                                    null
                            )
                    );
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();

            showError(
                    "Serviciile nu au putut fi încărcate."
            );
        }

        rebuildChoices();
        showServices();
    }

    private void rebuildChoices() {
        typePanel.removeAll();
        variantPanel.removeAll();
        designPanel.removeAll();
        extraPanel.removeAll();

        addTypeChoice("Ojă semi", "SEMI");
        addTypeChoice("Întreținere", "MAINTENANCE");
        addTypeChoice("Slim", "SLIM");
        addTypeChoice("Construcție", "CONSTRUCTION");

        serviceDescription.setText(" ");

        if (selectedType == null) {
            variantTitle.setText("Opțiuni");

            variantPanel.add(
                    label(
                            "Selectează un serviciu.",
                            12,
                            false,
                            MUTED
                    )
            );

        } else {
            switch (selectedType) {
                case "SEMI" -> buildSemiChoices();

                case "SLIM" -> buildSlimChoices();

                case "MAINTENANCE" ->
                        buildLengthChoices("intretinere");

                case "CONSTRUCTION" ->
                        buildLengthChoices("constructie");
            }
        }

        buildDesignChoices();
        buildExtraChoices();
        updateSummary();

        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void addTypeChoice(
            String title,
            String type
    ) {
        ChoiceButton button =
                new ChoiceButton(title);

        button.setSelected(
                type.equals(selectedType)
        );

        button.setEnabled(
                hasType(type)
        );

        button.addActionListener(event -> {
            if (!type.equals(selectedType)) {
                baseService = null;
                extraService = null;
            }

            selectedType = type;
            rebuildChoices();
        });

        typePanel.add(button);
    }

    private boolean hasType(String type) {
        return offeredServices.stream()
                .anyMatch(service -> {
                    String name =
                            normalize(service.name());

                    String category =
                            normalize(service.category());

                    return switch (type) {
                        case "SEMI" ->
                                name.contains("oja")
                                        && name.contains("semi");

                        case "SLIM" ->
                                name.equals("slim");

                        case "MAINTENANCE" ->
                                category.equals("intretinere")
                                        && sizeOf(service) >= 2;

                        case "CONSTRUCTION" ->
                                category.equals("constructie")
                                        && sizeOf(service) >= 2;

                        default -> false;
                    };
                });
    }

    private void buildLengthChoices(
            String category
    ) {
        variantTitle.setText("Mărimea");

        List<ServiceItem> sizes =
                offeredServices.stream()
                        .filter(service ->
                                category.equals(
                                        normalize(
                                                service.category()
                                        )
                                )
                        )
                        .filter(service ->
                                sizeOf(service) >= 2
                        )
                        .sorted(
                                Comparator.comparingInt(
                                        this::sizeOf
                                )
                        )
                        .toList();

        for (ServiceItem stored : sizes) {
            int size = sizeOf(stored);
            int steps = size - 2;

            boolean construction =
                    "constructie".equals(category);

            int duration =
                    construction
                            ? 90 + steps * 25
                            : 60 + steps * 15;

            long price =
                    construction
                            ? 150L + steps * 15L
                            : 130L + steps * 10L;

            String displayName =
                    construction
                            ? "Construcție"
                            : "Întreținere";

            ServiceItem choice =
                    new ServiceItem(
                            stored.id(),
                            displayName,
                            stored.category(),
                            duration,
                            BigDecimal.valueOf(price),
                            "Mărimea " + size
                    );

            addVariantChoice(
                    choice,
                    "Mărimea " + size
            );
        }
    }

    private void buildSlimChoices() {
        variantTitle.setText("Mărimea");

        serviceDescription.setText(
                "<html>"
                        + "Potrivit pentru persoanele cu unghii "
                        + "care cresc în jos."
                        + "</html>"
        );

        ServiceItem stored =
                offeredServices.stream()
                        .filter(service ->
                                normalize(service.name())
                                        .equals("slim")
                        )
                        .findFirst()
                        .orElse(null);

        if (stored == null) {
            return;
        }

        for (int size = 2; size <= 4; size++) {
            long price =
                    150L + (size - 2) * 25L;

            ServiceItem choice =
                    new ServiceItem(
                            stored.id(),
                            "Slim",
                            stored.category(),
                            90,
                            BigDecimal.valueOf(price),
                            "Mărimea " + size
                    );

            addVariantChoice(
                    choice,
                    "Mărimea " + size
            );
        }
    }

    private void buildSemiChoices() {
        variantTitle.setText("Tehnica");

        ServiceItem stored =
                offeredServices.stream()
                        .filter(service -> {
                            String name =
                                    normalize(
                                            service.name()
                                    );

                            return name.contains("oja")
                                    && name.contains("semi");
                        })
                        .findFirst()
                        .orElse(null);

        if (stored == null) {
            return;
        }

        ServiceItem simple =
                new ServiceItem(
                        stored.id(),
                        "Ojă semi",
                        stored.category(),
                        60,
                        BigDecimal.valueOf(130),
                        "Tehnică simplă"
                );

        ServiceItem hybrid =
                new ServiceItem(
                        stored.id(),
                        "Ojă semi",
                        stored.category(),
                        60,
                        BigDecimal.valueOf(140),
                        "Tehnică Hybrid"
                );

        addVariantChoice(simple, "Simplă");
        addVariantChoice(hybrid, "Hybrid");

        if (
                baseService != null
                        && "Tehnică Hybrid".equals(
                        baseService.details()
                )
        ) {
            serviceDescription.setText(
                    "<html>"
                            + "Întărirea vârfului cu polygel pentru "
                            + "unghii mai moi sau corecții "
                            + "de arhitectură."
                            + "</html>"
            );

        } else {
            serviceDescription.setText(
                    "<html>"
                            + "Fără selector de lungime. "
                            + "Alege tehnica simplă sau Hybrid."
                            + "</html>"
            );
        }
    }

    private void addVariantChoice(
            ServiceItem choice,
            String title
    ) {
        ChoiceButton button =
                pricedChoice(
                        title,
                        money(choice.price()),
                        sameChoice(baseService, choice)
                );

        button.addActionListener(event -> {
            baseService = choice;
            rebuildChoices();
        });

        variantPanel.add(button);
    }

    private void buildDesignChoices() {
        ChoiceButton noDesign =
                pricedChoice(
                        "Fără design",
                        "0 lei",
                        designService == null
                );

        noDesign.setEnabled(
                selectedTechnician != null
        );

        noDesign.addActionListener(event -> {
            designService = null;
            rebuildChoices();
        });

        designPanel.add(noDesign);

        offeredServices.stream()
                .filter(service ->
                        "design".equals(
                                normalize(
                                        service.category()
                                )
                        )
                )
                .sorted(
                        Comparator.comparingInt(
                                ServiceItem::duration
                        )
                )
                .forEach(service -> {
                    ChoiceButton button =
                            pricedChoice(
                                    designName(service),
                                    "+" + money(service.price()),
                                    sameChoice(
                                            designService,
                                            service
                                    )
                            );

                    button.addActionListener(event -> {
                        designService = service;
                        rebuildChoices();
                    });

                    designPanel.add(button);
                });
    }

    private void buildExtraChoices() {
        boolean maintenance =
                "MAINTENANCE".equals(
                        selectedType
                );

        if (!maintenance) {
            extraService = null;
        }

        extraSection.setVisible(maintenance);

        if (!maintenance) {
            return;
        }

        ChoiceButton noExtra =
                pricedChoice(
                        "Fără extraopțiuni",
                        "0 lei",
                        extraService == null
                );

        noExtra.addActionListener(event -> {
            extraService = null;
            rebuildChoices();
        });

        extraPanel.add(noExtra);

        for (ServiceItem service : offeredServices) {
            if (
                    !"supliment".equals(
                            normalize(
                                    service.category()
                            )
                    )
            ) {
                continue;
            }

            ChoiceButton button =
                    pricedChoice(
                            extraName(service),
                            "+" + money(service.price()),
                            sameChoice(
                                    extraService,
                                    service
                            )
                    );

            button.addActionListener(event -> {
                extraService =
                        sameChoice(extraService, service)
                                ? null
                                : service;

                rebuildChoices();
            });

            extraPanel.add(button);
        }
    }

    private ChoiceButton pricedChoice(
            String title,
            String price,
            boolean selected
    ) {
        ChoiceButton button =
                new ChoiceButton(
                        "<html>"
                                + "<div style='text-align:center;'>"
                                + escape(title)
                                + "<br>"
                                + "<span style='font-size:10px;'>"
                                + escape(price)
                                + "</span>"
                                + "</div>"
                                + "</html>"
                );

        button.setPreferredSize(
                new Dimension(120, 54)
        );

        button.setSelected(selected);

        return button;
    }

    private int sizeOf(ServiceItem service) {
        Matcher matcher =
                Pattern.compile("marimea\\s*(\\d+)")
                        .matcher(
                                normalize(
                                        service.name()
                                )
                        );

        return matcher.find()
                ? Integer.parseInt(
                matcher.group(1)
        )
                : -1;
    }

    private String designName(
            ServiceItem service
    ) {
        String name =
                normalize(service.name());

        if (name.contains("simplu")) {
            return "Easy";
        }

        if (name.contains("mediu")) {
            return "Mediu";
        }

        if (name.contains("complex")) {
            return "Complex";
        }

        return service.name();
    }

    private String extraName(
            ServiceItem service
    ) {
        String name =
                normalize(service.name());

        if (name.contains("corect")) {
            return "Corectare arhitectură";
        }

        if (name.contains("schimbare")) {
            return "Schimbare arhitectură";
        }

        return service.name();
    }

    private String baseDisplayName() {
        if (baseService == null) {
            return "";
        }

        String details =
                baseService.details();

        return baseService.name()
                + (
                details == null || details.isBlank()
                        ? ""
                        : " · " + details
        );
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text.toLowerCase(Locale.ROOT)
                .replace("ă", "a")
                .replace("â", "a")
                .replace("î", "i")
                .replace("ș", "s")
                .replace("ş", "s")
                .replace("ț", "t")
                .replace("ţ", "t")
                .trim();
    }

    private boolean sameChoice(
            ServiceItem first,
            ServiceItem second
    ) {
        return first != null
                && second != null
                && first.id() == second.id()
                && Objects.equals(
                first.details(),
                second.details()
        );
    }

    private String money(BigDecimal price) {
        return price.stripTrailingZeros()
                .toPlainString()
                + " lei";
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private List<ServiceItem> selectedServices() {
        List<ServiceItem> result =
                new ArrayList<>();

        if (baseService != null) {
            result.add(baseService);
        }

        if (designService != null) {
            result.add(designService);
        }

        if (
                "MAINTENANCE".equals(selectedType)
                        && extraService != null
        ) {
            result.add(extraService);
        }

        return result;
    }

    private BigDecimal totalPrice() {
        return selectedServices().stream()
                .map(ServiceItem::price)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private int totalDuration() {
        return selectedServices().stream()
                .mapToInt(
                        ServiceItem::duration
                )
                .sum();
    }

    private void updateSummary() {
        availabilityButton.setEnabled(
                selectedTechnician != null
                        && baseService != null
                        && !bookingBusy
        );

        if (baseService == null) {
            selectionLabel.setText(
                    "Alege serviciul și varianta dorită."
            );

            totalLabel.setText("Total: —");
            return;
        }

        StringBuilder text =
                new StringBuilder(
                        "<html>"
                                + escape(
                                baseDisplayName()
                        )
                );

        if (designService != null) {
            text.append("<br>Design ")
                    .append(
                            escape(
                                    designName(
                                            designService
                                    )
                            )
                    );
        }

        if (
                "MAINTENANCE".equals(selectedType)
                        && extraService != null
        ) {
            text.append("<br>")
                    .append(
                            escape(
                                    extraName(
                                            extraService
                                    )
                            )
                    );
        }

        text.append("</html>");

        selectionLabel.setText(
                text.toString()
        );

        totalLabel.setText(
                "Total: " + money(totalPrice())
        );
    }

    private void showServices() {
        availabilityButton.setVisible(true);

        ((CardLayout) rightPanel.getLayout())
                .show(
                        rightPanel,
                        "SERVICES"
                );

        updateSummary();
    }

    private void showCalendar() {
        if (
                selectedTechnician == null
                        || baseService == null
        ) {
            showError(
                    "Selectează tehniciana, serviciul "
                            + "și varianta dorită."
            );

            return;
        }

        availabilityButton.setVisible(false);

        calendarSummaryLabel.setText(
                "<html>"
                        + escape(
                        selectedTechnician.name()
                )
                        + "<br>"
                        + escape(
                        baseDisplayName()
                )
                        + " · "
                        + escape(
                        money(totalPrice())
                )
                        + "</html>"
        );

        ((CardLayout) rightPanel.getLayout())
                .show(
                        rightPanel,
                        "CALENDAR"
                );

        statusLabel.setForeground(MUTED);

        statusLabel.setText(
                "Zilele cu dungă verde permit serviciile "
                        + "selectate și au ore disponibile."
        );

        refreshCalendar();
    }

    private void refreshCalendar() {
        calendarGrid.removeAll();
        hoursPanel.removeAll();

        selectedDate = null;
        hoursTitle.setText(" ");

        String month =
                displayedMonth.format(
                        DateTimeFormatter.ofPattern(
                                "MMMM yyyy",
                                RO
                        )
                );

        monthLabel.setText(
                month.substring(0, 1)
                        .toUpperCase(RO)
                        + month.substring(1)
        );

        for (
                String day
                : new String[]{
                "Lu", "Ma", "Mi", "Jo", "Vi", "Sâ", "Du"
        }
        ) {
            JLabel heading =
                    label(
                            day,
                            12,
                            true,
                            MUTED
                    );

            heading.setHorizontalAlignment(
                    SwingConstants.CENTER
            );

            calendarGrid.add(heading);
        }

        int offset =
                displayedMonth.atDay(1)
                        .getDayOfWeek()
                        .getValue() - 1;

        for (int index = 0; index < offset; index++) {
            calendarGrid.add(new JLabel());
        }

        String loadingError = null;

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            for (
                    int day = 1;
                    day <= displayedMonth.lengthOfMonth();
                    day++
            ) {
                LocalDate date =
                        displayedMonth.atDay(day);

                boolean available =
                        selectedTechnician != null
                                && baseService != null
                                && !date.isBefore(LocalDate.now())
                                && !availableSlots(
                                connection,
                                selectedTechnician.id(),
                                date,
                                totalDuration()
                        ).isEmpty();

                DayButton button =
                        new DayButton(
                                day,
                                available
                        );

                button.setToolTipText(
                        available
                                ? "Serviciile selectate sunt permise "
                                + "și există ore disponibile."
                                : "Serviciile selectate nu sunt disponibile "
                                + "sau nu există timp liber suficient."
                );

                button.addActionListener(event -> {
                    if (bookingBusy) {
                        return;
                    }

                    selectedDate = date;

                    for (
                            Component component
                            : calendarGrid.getComponents()
                    ) {
                        if (component instanceof DayButton item) {
                            item.setSelected(
                                    item == button
                            );
                        }
                    }

                    calendarGrid.repaint();
                    loadAvailableHours();
                });

                calendarGrid.add(button);
            }

        } catch (SQLException exception) {
            exception.printStackTrace();

            loadingError =
                    "Disponibilitatea nu a putut fi încărcată: "
                            + exceptionMessage(exception);

            calendarGrid.removeAll();

            calendarGrid.add(
                    label(
                            "Eroare",
                            12,
                            true,
                            RED
                    )
            );
        }

        while (
                calendarGrid.getComponentCount() < 49
        ) {
            calendarGrid.add(new JLabel());
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();

        hoursPanel.revalidate();
        hoursPanel.repaint();

        if (loadingError != null) {
            showError(loadingError);
        }
    }

    private void loadAvailableHours() {
        hoursPanel.removeAll();

        if (
                selectedDate == null
                        || selectedTechnician == null
                        || baseService == null
        ) {
            hoursPanel.revalidate();
            hoursPanel.repaint();
            return;
        }

        hoursTitle.setText(
                "Ore disponibile · "
                        + selectedDate.format(
                        DATE_FORMAT
                )
        );

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            List<LocalDateTime> slots =
                    availableSlots(
                            connection,
                            selectedTechnician.id(),
                            selectedDate,
                            totalDuration()
                    );

            for (LocalDateTime slot : slots) {
                JButton button =
                        new JButton(
                                slot.format(
                                        TIME_FORMAT
                                )
                        );

                styleSecondary(button);

                button.setPreferredSize(
                        new Dimension(100, 39)
                );

                button.addActionListener(
                        event -> confirmAppointment(slot)
                );

                hoursPanel.add(button);
            }

            if (slots.isEmpty()) {
                hoursPanel.add(
                        label(
                                "<html>"
                                        + "Nu există ore disponibile pentru "
                                        + "serviciile selectate în această zi."
                                        + "</html>",
                                12,
                                false,
                                MUTED
                        )
                );
            }

        } catch (SQLException exception) {
            exception.printStackTrace();

            showError(
                    "Orele disponibile nu au putut fi încărcate: "
                            + exceptionMessage(exception)
            );
        }

        hoursPanel.revalidate();
        hoursPanel.repaint();
    }

    private boolean selectedServicesAllowedOnDay(
            Connection connection,
            int technicianId,
            LocalDate date
    ) throws SQLException {

        if (baseService == null) {
            return false;
        }

        return servicesAllowedOnDay(
                connection,
                technicianId,
                date,
                selectedServices()
        );
    }

    private boolean servicesAllowedOnDay(
            Connection connection,
            int technicianId,
            LocalDate date,
            List<ServiceItem> requested
    ) throws SQLException {

        if (requested.isEmpty()) {
            return false;
        }

        boolean configured = false;

        String configurationSql = """
                SELECT tip_zi
                FROM configurare_zi_tehnician
                WHERE id_tehnician = ?
                  AND data = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                configurationSql
                        )
        ) {
            statement.setInt(
                    1,
                    technicianId
            );

            statement.setDate(
                    2,
                    java.sql.Date.valueOf(date)
            );

            try (
                    ResultSet rows =
                            statement.executeQuery()
            ) {
                if (rows.next()) {
                    configured = true;

                    if (
                            !"Lucru".equals(
                                    rows.getString("tip_zi")
                            )
                    ) {
                        return false;
                    }
                }
            }
        }

        String servicesSql;

        if (configured) {
            servicesSql = """
                    SELECT ts.id_serviciu
                    FROM tehnician_serviciu ts
                    JOIN serviciu_zi_tehnician sz
                        ON sz.id_tehnician = ts.id_tehnician
                       AND sz.id_serviciu = ts.id_serviciu
                    WHERE ts.id_tehnician = ?
                      AND ts.stare_oferta = 'Activa'
                      AND sz.data = ?
                    """;

        } else {
            servicesSql = """
                    SELECT ts.id_serviciu
                    FROM tehnician_serviciu ts
                    WHERE ts.id_tehnician = ?
                      AND ts.stare_oferta = 'Activa'
                    """;
        }

        Set<Integer> allowedIds =
                new HashSet<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                servicesSql
                        )
        ) {
            statement.setInt(
                    1,
                    technicianId
            );

            if (configured) {
                statement.setDate(
                        2,
                        java.sql.Date.valueOf(date)
                );
            }

            try (
                    ResultSet rows =
                            statement.executeQuery()
            ) {
                while (rows.next()) {
                    allowedIds.add(
                            rows.getInt(
                                    "id_serviciu"
                            )
                    );
                }
            }
        }

        for (ServiceItem service : requested) {
            if (!allowedIds.contains(service.id())) {
                return false;
            }
        }

        return true;
    }

    private List<LocalDateTime> availableSlots(
            Connection connection,
            int technicianId,
            LocalDate date,
            int duration
    ) throws SQLException {

        if (
                duration <= 0
                        || date.isBefore(LocalDate.now())
        ) {
            return List.of();
        }

        if (
                !selectedServicesAllowedOnDay(
                        connection,
                        technicianId,
                        date
                )
        ) {
            return List.of();
        }

        return calculateTimeSlots(
                connection,
                technicianId,
                date,
                duration
        );
    }

    private List<LocalDateTime> calculateTimeSlots(
            Connection connection,
            int technicianId,
            LocalDate date,
            int duration
    ) throws SQLException {

        if (
                duration <= 0
                        || date.isBefore(LocalDate.now())
        ) {
            return List.of();
        }

        List<TimeInterval> courseIntervals =
                loadCourseIntervals(
                        connection,
                        technicianId,
                        date
                );

        if (!courseIntervals.isEmpty()) {
            return List.of();
        }

        List<TimeInterval> work =
                mergeWorkIntervals(
                        loadAvailabilityIntervals(
                                connection,
                                technicianId,
                                date,
                                "Lucru"
                        )
                );

        List<TimeInterval> busy =
                loadAvailabilityIntervals(
                        connection,
                        technicianId,
                        date,
                        "Absenta"
                );

        busy.addAll(
                loadAppointmentIntervals(
                        connection,
                        technicianId,
                        date
                )
        );

        TreeSet<LocalDateTime> slots =
                new TreeSet<>();

        LocalDateTime dayStart =
                date.atStartOfDay();

        LocalDateTime dayEnd =
                date.plusDays(1).atStartOfDay();

        LocalDateTime now =
                LocalDateTime.now();

        for (TimeInterval interval : work) {
            LocalDateTime start =
                    interval.start().isAfter(dayStart)
                            ? interval.start()
                            : dayStart;

            LocalDateTime end =
                    interval.end().isBefore(dayEnd)
                            ? interval.end()
                            : dayEnd;

            for (
                    LocalDateTime candidate = start;
                    !candidate.plusMinutes(duration)
                            .isAfter(end);
                    candidate = candidate.plusMinutes(30)
            ) {
                if (!candidate.isAfter(now)) {
                    continue;
                }

                LocalDateTime candidateEnd =
                        candidate.plusMinutes(duration);

                boolean overlaps = false;

                for (TimeInterval occupied : busy) {
                    if (
                            candidate.isBefore(
                                    occupied.end()
                            )
                                    && candidateEnd.isAfter(
                                    occupied.start()
                            )
                    ) {
                        overlaps = true;
                        break;
                    }
                }

                if (!overlaps) {
                    slots.add(candidate);
                }
            }
        }

        return new ArrayList<>(slots);
    }

    private List<TimeInterval> mergeWorkIntervals(
            List<TimeInterval> intervals
    ) {
        List<TimeInterval> sorted =
                new ArrayList<>(intervals);

        sorted.sort(
                Comparator.comparing(
                        TimeInterval::start
                )
        );

        List<TimeInterval> merged =
                new ArrayList<>();

        for (TimeInterval current : sorted) {
            if (merged.isEmpty()) {
                merged.add(current);
                continue;
            }

            TimeInterval previous =
                    merged.get(
                            merged.size() - 1
                    );

            if (
                    !current.start().isAfter(
                            previous.end()
                    )
            ) {
                LocalDateTime end =
                        current.end().isAfter(previous.end())
                                ? current.end()
                                : previous.end();

                merged.set(
                        merged.size() - 1,
                        new TimeInterval(
                                previous.start(),
                                end
                        )
                );

            } else {
                merged.add(current);
            }
        }

        return merged;
    }

    private List<TimeInterval> loadAvailabilityIntervals(
            Connection connection,
            int technicianId,
            LocalDate date,
            String type
    ) throws SQLException {

        String sql = """
                SELECT inceput, sfarsit
                FROM interval_disponibilitate
                WHERE id_angajat = ?
                  AND tip_interval = ?
                  AND inceput < ?
                  AND sfarsit > ?
                ORDER BY inceput
                """;

        List<TimeInterval> result =
                new ArrayList<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(
                    1,
                    technicianId
            );

            statement.setString(
                    2,
                    type
            );

            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            date.plusDays(1)
                                    .atStartOfDay()
                    )
            );

            statement.setTimestamp(
                    4,
                    Timestamp.valueOf(
                            date.atStartOfDay()
                    )
            );

            try (
                    ResultSet rows =
                            statement.executeQuery()
            ) {
                while (rows.next()) {
                    result.add(
                            new TimeInterval(
                                    rows.getTimestamp("inceput")
                                            .toLocalDateTime(),
                                    rows.getTimestamp("sfarsit")
                                            .toLocalDateTime()
                            )
                    );
                }
            }
        }

        return result;
    }

    private List<TimeInterval> loadAppointmentIntervals(
            Connection connection,
            int technicianId,
            LocalDate date
    ) throws SQLException {

        String sql = """
                SELECT
                    p.id_programare,
                    p.data,
                    p.ora,
                    COALESCE(
                        SUM(ps.durata_aplicata), 0
                    ) AS durata_totala
                FROM programare p
                LEFT JOIN programare_serviciu ps
                    ON ps.id_programare = p.id_programare
                WHERE p.id_tehnician = ?
                  AND (
                      p.stare IS NULL
                      OR p.stare <> 'Anulata'
                  )
                  AND p.data <= ?
                GROUP BY
                    p.id_programare,
                    p.data,
                    p.ora
                ORDER BY p.data, p.ora
                """;

        List<TimeInterval> result =
                new ArrayList<>();

        LocalDateTime dayStart =
                date.atStartOfDay();

        LocalDateTime dayEnd =
                date.plusDays(1).atStartOfDay();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(
                    1,
                    technicianId
            );

            statement.setDate(
                    2,
                    java.sql.Date.valueOf(date)
            );

            try (
                    ResultSet rows =
                            statement.executeQuery()
            ) {
                while (rows.next()) {
                    LocalDateTime start =
                            LocalDateTime.of(
                                    rows.getDate("data")
                                            .toLocalDate(),
                                    rows.getTime("ora")
                                            .toLocalTime()
                            );

                    int duration =
                            rows.getInt(
                                    "durata_totala"
                            );

                    if (duration <= 0) {
                        if (
                                start.toLocalDate()
                                        .equals(date)
                        ) {
                            throw new SQLException(
                                    "Programarea "
                                            + rows.getInt(
                                            "id_programare"
                                    )
                                            + " nu are durată validă."
                            );
                        }

                        continue;
                    }

                    LocalDateTime end =
                            start.plusMinutes(duration);

                    if (
                            start.isBefore(dayEnd)
                                    && end.isAfter(dayStart)
                    ) {
                        result.add(
                                new TimeInterval(
                                        start,
                                        end
                                )
                        );
                    }
                }
            }
        }

        return result;
    }

    private List<TimeInterval> loadCourseIntervals(
            Connection connection,
            int technicianId,
            LocalDate date
    ) throws SQLException {

        String sql = """
                SELECT i.id_interval
                FROM interval_sesiune i
                JOIN sesiune_curs s
                    ON s.id_sesiune = i.id_sesiune
                WHERE s.id_trainer = ?
                  AND s.stare <> 'Anulata'
                  AND (
                      i.data_zi = ?
                      OR (
                          i.inceput < ?
                          AND i.sfarsit > ?
                      )
                  )
                LIMIT 1
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(
                    1,
                    technicianId
            );

            statement.setDate(
                    2,
                    java.sql.Date.valueOf(date)
            );

            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            date.plusDays(1)
                                    .atStartOfDay()
                    )
            );

            statement.setTimestamp(
                    4,
                    Timestamp.valueOf(
                            date.atStartOfDay()
                    )
            );

            try (
                    ResultSet rows =
                            statement.executeQuery()
            ) {
                if (rows.next()) {
                    return List.of(
                            new TimeInterval(
                                    date.atStartOfDay(),
                                    date.plusDays(1)
                                            .atStartOfDay()
                            )
                    );
                }
            }
        }

        return List.of();
    }

    private void confirmAppointment(
            LocalDateTime start
    ) {
        if (
                bookingBusy
                        || baseService == null
                        || selectedTechnician == null
        ) {
            return;
        }

        StringBuilder message =
                new StringBuilder();

        message.append(
                        selectedTechnician.name()
                )
                .append("\n")
                .append(
                        start.format(DATE_FORMAT)
                )
                .append(", ora ")
                .append(
                        start.format(TIME_FORMAT)
                )
                .append("\n\n")
                .append(
                        baseDisplayName()
                );

        if (designService != null) {
            message.append("\nDesign ")
                    .append(
                            designName(
                                    designService
                            )
                    );
        }

        if (
                "MAINTENANCE".equals(selectedType)
                        && extraService != null
        ) {
            message.append("\n")
                    .append(
                            extraName(
                                    extraService
                            )
                    );
        }

        message.append("\n\nTotal: ")
                .append(
                        money(totalPrice())
                )
                .append(
                        "\n\nConfirmi programarea?"
                );

        int answer =
                JOptionPane.showConfirmDialog(
                        this,
                        message.toString(),
                        "Confirmă programarea",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (answer == JOptionPane.YES_OPTION) {
            createAppointment(start);
        }
    }

    private void lockTechnicianForBooking(
            Connection connection,
            int technicianId
    ) throws SQLException {

        String employeeSql = """
                SELECT id_angajat
                FROM angajat
                WHERE id_angajat = ?
                FOR UPDATE
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                employeeSql
                        )
        ) {
            statement.setInt(
                    1,
                    technicianId
            );

            try (
                    ResultSet rows =
                            statement.executeQuery()
            ) {
                if (!rows.next()) {
                    throw new SQLException(
                            "Angajatul nu mai există."
                    );
                }
            }
        }

        String technicianSql = """
                SELECT id_angajat
                FROM tehnician
                WHERE id_angajat = ?
                FOR UPDATE
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                technicianSql
                        )
        ) {
            statement.setInt(
                    1,
                    technicianId
            );

            try (
                    ResultSet rows =
                            statement.executeQuery()
            ) {
                if (!rows.next()) {
                    throw new SQLException(
                            "Tehniciana nu mai există."
                    );
                }
            }
        }
    }

    private void createAppointment(
            LocalDateTime start
    ) {
        if (
                bookingBusy
                        || selectedTechnician == null
                        || baseService == null
        ) {
            return;
        }

        TechnicianItem technician =
                selectedTechnician;

        List<ServiceItem> services =
                List.copyOf(
                        selectedServices()
                );

        int duration =
                services.stream()
                        .mapToInt(
                                ServiceItem::duration
                        )
                        .sum();

        BigDecimal price =
                services.stream()
                        .map(ServiceItem::price)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        bookingBusy = true;
        updateSummary();

        boolean committed = false;
        String failure = null;

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                lockTechnicianForBooking(
                        connection,
                        technician.id()
                );

                if (
                        !servicesAllowedOnDay(
                                connection,
                                technician.id(),
                                start.toLocalDate(),
                                services
                        )
                ) {
                    throw new SQLException(
                            "Unul dintre serviciile selectate "
                                    + "nu este disponibil la această "
                                    + "tehniciană în data de "
                                    + start.format(DATE_FORMAT)
                                    + ". Modifică serviciul "
                                    + "sau alege altă zi."
                    );
                }

                Integer clientId =
                        findClientId(connection);

                if (clientId == null) {
                    throw new SQLException(
                            "Contul nu este asociat unui client."
                    );
                }

                List<LocalDateTime> currentSlots =
                        calculateTimeSlots(
                                connection,
                                technician.id(),
                                start.toLocalDate(),
                                duration
                        );

                if (!currentSlots.contains(start)) {
                    throw new SQLException(
                            "Ora nu mai este disponibilă. "
                                    + "Alege un alt interval."
                    );
                }

                int appointmentId;

                String insertAppointment = """
                        INSERT INTO programare
                            (
                                id_client,
                                id_tehnician,
                                data,
                                ora
                            )
                        VALUES (?, ?, ?, ?)
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        insertAppointment,
                                        Statement.RETURN_GENERATED_KEYS
                                )
                ) {
                    statement.setInt(
                            1,
                            clientId
                    );

                    statement.setInt(
                            2,
                            technician.id()
                    );

                    statement.setDate(
                            3,
                            java.sql.Date.valueOf(
                                    start.toLocalDate()
                            )
                    );

                    statement.setTime(
                            4,
                            java.sql.Time.valueOf(
                                    start.toLocalTime()
                            )
                    );

                    statement.executeUpdate();

                    try (
                            ResultSet keys =
                                    statement.getGeneratedKeys()
                    ) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Nu s-a obținut "
                                            + "ID-ul programării."
                            );
                        }

                        appointmentId =
                                keys.getInt(1);
                    }
                }

                String insertService = """
                        INSERT INTO programare_serviciu
                            (
                                id_programare,
                                id_serviciu,
                                pret_aplicat,
                                durata_aplicata,
                                detalii
                            )
                        VALUES (?, ?, ?, ?, ?)
                        """;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        insertService
                                )
                ) {
                    for (ServiceItem service : services) {
                        statement.setInt(
                                1,
                                appointmentId
                        );

                        statement.setInt(
                                2,
                                service.id()
                        );

                        statement.setBigDecimal(
                                3,
                                service.price()
                        );

                        statement.setInt(
                                4,
                                service.duration()
                        );

                        statement.setString(
                                5,
                                service.details()
                        );

                        statement.addBatch();
                    }

                    statement.executeBatch();
                }

                connection.commit();
                committed = true;

            } catch (Exception exception) {
                try {
                    connection.rollback();

                } catch (SQLException rollbackException) {
                    exception.addSuppressed(
                            rollbackException
                    );
                }

                throw exception;
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            failure = exceptionMessage(exception);

        } finally {
            bookingBusy = false;
            updateSummary();
        }

        refreshCalendar();

        if (committed) {
            statusLabel.setForeground(GREEN);

            statusLabel.setText(
                    "Programarea a fost creată pentru "
                            + start.format(DATE_FORMAT)
                            + ", ora "
                            + start.format(TIME_FORMAT)
                            + ". Total: "
                            + money(price)
                            + "."
            );

        } else {
            showError(
                    failure == null
                            ? "Programarea nu a putut fi creată."
                            : failure
            );
        }
    }

    private Integer findClientId(
            Connection connection
    ) throws SQLException {

        String sql = """
                SELECT id_client
                FROM utilizator
                WHERE email = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
                    userEmail
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                if (result.next()) {
                    int id =
                            result.getInt(
                                    "id_client"
                            );

                    if (!result.wasNull()) {
                        return id;
                    }
                }
            }
        }

        return null;
    }

    private String exceptionMessage(
            Throwable exception
    ) {
        String message =
                exception.getMessage();

        return message == null || message.isBlank()
                ? "Operația nu a putut fi efectuată."
                : message;
    }

    private void stylePrimary(
            JButton button
    ) {
        button.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );

        button.setBackground(INK);
        button.setForeground(WHITE);
        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.putClientProperty(
                "JButton.arc",
                18
        );
    }

    private void styleSecondary(
            JButton button
    ) {
        button.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        button.setBackground(FIELD);
        button.setForeground(INK);
        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.putClientProperty(
                "JButton.arc",
                18
        );
    }

    private void showError(
            String message
    ) {
        statusLabel.setForeground(RED);

        statusLabel.setText(
                "<html>"
                        + escape(message)
                        + "</html>"
        );
    }

    private record TechnicianItem(
            int id,
            String name
    ) {
    }

    private record TechnicianChoice(
            TechnicianItem technician,
            ChoiceButton button
    ) {
    }

    private record ServiceItem(
            int id,
            String name,
            String category,
            int duration,
            BigDecimal price,
            String details
    ) {
    }

    private record TimeInterval(
            LocalDateTime start,
            LocalDateTime end
    ) {
    }

    private static class RoundedPanel
            extends JPanel {

        private final Color fill;
        private final int arc;

        RoundedPanel(
                Color fill,
                int arc
        ) {
            this.fill = fill;
            this.arc = arc;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(
                Graphics graphics
        ) {
            super.paintComponent(graphics);

            Graphics2D g =
                    (Graphics2D) graphics.create();

            try {
                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g.setColor(fill);

                g.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        arc,
                        arc
                );

            } finally {
                g.dispose();
            }
        }
    }

    private static class ChoiceButton
            extends JButton {

        ChoiceButton(String text) {
            super(text);

            setFont(
                    new Font(
                            "SansSerif",
                            Font.PLAIN,
                            13
                    )
            );

            setForeground(INK);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);

            setBorder(
                    BorderFactory.createEmptyBorder(
                            8, 14, 8, 14
                    )
            );

            setPreferredSize(
                    new Dimension(150, 46)
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        @Override
        protected void paintComponent(
                Graphics graphics
        ) {
            Graphics2D g =
                    (Graphics2D) graphics.create();

            try {
                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color fill;

                if (!isEnabled()) {
                    fill = FIELD;

                } else if (isSelected()) {
                    fill = HOVER;

                } else if (getModel().isRollover()) {
                    fill = new Color(
                            239, 231, 218
                    );

                } else {
                    fill = BEIGE;
                }

                g.setColor(fill);

                g.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        25,
                        25
                );

                g.setColor(
                        isSelected()
                                ? GREEN
                                : BORDER
                );

                g.setStroke(
                        new BasicStroke(
                                isSelected()
                                        ? 2f
                                        : 1f
                        )
                );

                g.drawRoundRect(
                        1,
                        1,
                        getWidth() - 3,
                        getHeight() - 3,
                        25,
                        25
                );

            } finally {
                g.dispose();
            }

            super.paintComponent(graphics);
        }
    }

    private static class DayButton
            extends JButton {

        private final boolean available;

        DayButton(
                int day,
                boolean available
        ) {
            super(
                    String.valueOf(day)
            );

            this.available = available;

            setEnabled(available);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);

            setFont(
                    new Font(
                            "SansSerif",
                            Font.BOLD,
                            14
                    )
            );

            setForeground(
                    available
                            ? INK
                            : MUTED
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            available
                                    ? Cursor.HAND_CURSOR
                                    : Cursor.DEFAULT_CURSOR
                    )
            );
        }

        @Override
        protected void paintComponent(
                Graphics graphics
        ) {
            Graphics2D g =
                    (Graphics2D) graphics.create();

            try {
                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g.setColor(
                        isSelected()
                                ? BEIGE
                                : FIELD
                );

                g.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        16,
                        16
                );

                g.setColor(
                        isSelected()
                                ? GREEN
                                : BORDER
                );

                g.drawRoundRect(
                        0,
                        0,
                        getWidth() - 1,
                        getHeight() - 1,
                        16,
                        16
                );

                if (available) {
                    int inset =
                            Math.max(
                                    7,
                                    getWidth() / 5
                            );

                    g.setColor(GREEN);

                    g.fillRoundRect(
                            inset,
                            getHeight() - 8,
                            Math.max(
                                    4,
                                    getWidth() - inset * 2
                            ),
                            4,
                            4,
                            4
                    );
                }

            } finally {
                g.dispose();
            }

            super.paintComponent(graphics);
        }
    }
}