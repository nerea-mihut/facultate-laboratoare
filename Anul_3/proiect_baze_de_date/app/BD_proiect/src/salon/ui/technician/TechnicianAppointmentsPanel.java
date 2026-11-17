package salon.ui.technician;

import salon.service.AppointmentService;
import salon.service.TechnicianService;
import salon.ui.components.RoundedCard;
import salon.ui.components.UiStyle;
import salon.ui.model.AppointmentItem;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TechnicianAppointmentsPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentService appointmentService;
    private final TechnicianService technicianService;
    private final Runnable onBack;

    private final JPanel appointmentsPanel =
            new JPanel();
    private final JPanel detailsPanel =
            new JPanel(new BorderLayout());

    private AppointmentItem selectedAppointment;

    public TechnicianAppointmentsPanel(
            AppointmentService appointmentService,
            TechnicianService technicianService
    ) {
        this(appointmentService, technicianService, null);
    }

    public TechnicianAppointmentsPanel(
            AppointmentService appointmentService,
            TechnicianService technicianService,
            Runnable onBack
    ) {
        this.onBack = onBack;
        this.appointmentService = appointmentService;
        this.technicianService = technicianService;

        setLayout(new BorderLayout(24, 0));
        setBackground(UiStyle.CREAM);
        setBorder(
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        );

        JPanel heading = new JPanel(new BorderLayout(12, 0));
        heading.setOpaque(false);

        heading.add(createBackButton(), BorderLayout.WEST);

        JLabel title = UiStyle.label(
                "Programările mele",
                24,
                true,
                UiStyle.INK
        );

        heading.add(title, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 14));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(430, 0));
        leftPanel.add(heading, BorderLayout.NORTH);

        appointmentsPanel.setLayout(
                new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS)
        );
        appointmentsPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(appointmentsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getViewport().setBackground(UiStyle.CREAM);

        leftPanel.add(scroll, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 14));
        rightPanel.setOpaque(false);

        JLabel detailsTitle = UiStyle.label(
                "Detaliile programării",
                24,
                true,
                UiStyle.INK
        );

        detailsPanel.setOpaque(false);
        detailsPanel.add(
                emptyDetailsPanel(
                        "Selectează o programare pentru a vedea detaliile."
                ),
                BorderLayout.CENTER
        );

        JScrollPane detailsScroll = new JScrollPane(detailsPanel);
        detailsScroll.setBorder(null);
        detailsScroll.getVerticalScrollBar().setUnitIncrement(18);
        detailsScroll.getViewport().setBackground(UiStyle.CREAM);

        rightPanel.add(detailsTitle, BorderLayout.NORTH);
        rightPanel.add(detailsScroll, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        loadAppointments();
    }

    private JButton createBackButton() {
        JButton button = UiStyle.secondaryButton("< Înapoi");
        button.setPreferredSize(new Dimension(110, 38));

        button.addActionListener(e -> {
            if (onBack != null) {
                onBack.run();
                return;
            }
            Container parent = getParent();
            while (parent != null) {
                if (parent.getLayout() instanceof CardLayout layout) {
                    layout.show(parent, "HOME");
                    return;
                }
                parent = parent.getParent();
            }
        });
        return button;
    }

    private void loadAppointments() {
        appointmentsPanel.removeAll();
        selectedAppointment = null;

        detailsPanel.removeAll();
        detailsPanel.add(
                emptyDetailsPanel(
                        "Selectează o programare pentru a vedea detaliile."
                ),
                BorderLayout.CENTER
        );

        List<AppointmentItem> appointments =
                appointmentService.loadTechnicianAppointments(
                        technicianService.getTechnicianId()
                );

        for (AppointmentItem appointment : appointments) {
            appointmentsPanel.add(
                    createAppointmentCard(appointment)
            );
            appointmentsPanel.add(Box.createVerticalStrut(12));
        }

        if (appointments.isEmpty()) {
            appointmentsPanel.add(
                    emptyLabel("Nu există programări atribuite.")
            );
        }

        appointmentsPanel.revalidate();
        appointmentsPanel.repaint();
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private JPanel createAppointmentCard(AppointmentItem appointment) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(UiStyle.FIELD);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                UiStyle.BORDER, 1, true
                        ),
                        BorderFactory.createEmptyBorder(
                                16, 18, 16, 18
                        )
                )
        );
        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 130)
        );
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel clientLabel = UiStyle.label(
                appointment.clientFirstName() + " "
                        + appointment.clientLastName(),
                16,
                true,
                UiStyle.INK
        );
        clientLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dateLabel = UiStyle.label(
                appointment.date().format(DATE_FORMAT)
                        + ", ora "
                        + appointment.time().format(TIME_FORMAT),
                13,
                false,
                UiStyle.MUTED
        );
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel durationLabel = UiStyle.label(
                "Durată: " + appointment.totalDuration()
                        + " min · Total: "
                        + money(appointment.totalPrice()),
                13,
                false,
                UiStyle.MUTED
        );
        durationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusLabel = UiStyle.label(
                "Stare: " + appointment.status(),
                13,
                true,
                "Anulata".equals(appointment.status())
                        ? UiStyle.RED
                        : UiStyle.GREEN
        );
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(clientLabel);
        info.add(Box.createVerticalStrut(5));
        info.add(dateLabel);
        info.add(Box.createVerticalStrut(3));
        info.add(durationLabel);
        info.add(Box.createVerticalStrut(5));
        info.add(statusLabel);

        card.add(info, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(
                    java.awt.event.MouseEvent event
            ) {
                selectedAppointment = appointment;
                loadAppointmentDetails();
            }
        });

        return card;
    }

    private void loadAppointmentDetails() {
        detailsPanel.removeAll();

        if (selectedAppointment == null) {
            return;
        }

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel clientCard = createSectionCard("Client");
        addDetailLine(
                clientCard,
                "Nume",
                selectedAppointment.clientFirstName() + " "
                        + selectedAppointment.clientLastName()
        );
        addDetailLine(
                clientCard,
                "Telefon",
                selectedAppointment.clientPhone()
        );
        addDetailLine(
                clientCard,
                "E-mail",
                selectedAppointment.clientEmail()
        );
        addDetailLine(
                clientCard,
                "Alergii declarate",
                selectedAppointment.allergies()
        );

        if ("Da".equalsIgnoreCase(
                selectedAppointment.allergies()
        )) {
            addDetailLine(
                    clientCard,
                    "Detalii alergii",
                    selectedAppointment.allergyDetails()
            );
        }

        JPanel appointmentCard = createSectionCard("Programare");
        addDetailLine(
                appointmentCard,
                "Data",
                selectedAppointment.date().format(DATE_FORMAT)
        );
        addDetailLine(
                appointmentCard,
                "Ora",
                selectedAppointment.time().format(TIME_FORMAT)
        );
        addDetailLine(
                appointmentCard,
                "Durată totală",
                selectedAppointment.totalDuration() + " minute"
        );
        addDetailLine(
                appointmentCard,
                "Total",
                money(selectedAppointment.totalPrice())
        );
        addDetailLine(
                appointmentCard,
                "Stare",
                selectedAppointment.status()
        );

        if (selectedAppointment.cancellationReason() != null
                && !selectedAppointment.cancellationReason()
                .isBlank()) {

            addDetailLine(
                    appointmentCard,
                    "Motivul anulării",
                    selectedAppointment.cancellationReason()
            );
        }

        content.add(clientCard);
        content.add(Box.createVerticalStrut(16));
        content.add(appointmentCard);
        content.add(Box.createVerticalStrut(16));

        JPanel servicesCard = createSectionCard("Servicii programate");
        loadServices(servicesCard);

        content.add(servicesCard);
        content.add(Box.createVerticalStrut(16));

        JPanel actionsCard = createSectionCard("Actualizează starea");
        addStatusButtons(actionsCard);

        content.add(actionsCard);
        content.add(Box.createVerticalStrut(16));

        JPanel feedbackCard = createSectionCard("Feedback client");
        loadFeedback(feedbackCard);

        content.add(feedbackCard);

        detailsPanel.add(content, BorderLayout.NORTH);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private void loadServices(JPanel servicesCard) {
        // va fi mutat în AppointmentService mai târziu
        // pentru moment lăsăm mesaj simplu
        addDetailLine(
                servicesCard,
                "Serviciile vor fi încărcate din AppointmentService.",
                ""
        );
    }

    private void loadFeedback(JPanel feedbackCard) {
        // va fi mutat în AppointmentService mai târziu
        addDetailLine(
                feedbackCard,
                "Feedback-ul va fi încărcat din AppointmentService.",
                ""
        );
    }

    private void addStatusButtons(JPanel actionsCard) {
        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        buttons.setOpaque(false);

        for (String status : new String[]{
                "Confirmata", "Finalizata", "Anulata", "Neprezentare"
        }) {
            JButton button = UiStyle.secondaryButton(status);

            button.addActionListener(e ->
                    updateAppointmentStatus(status)
            );

            buttons.add(button);
        }

        actionsCard.add(buttons, BorderLayout.CENTER);
    }

    private void updateAppointmentStatus(String newStatus) {
        if (selectedAppointment == null) {
            return;
        }

        if ("Anulata".equals(newStatus)) {
            String reason = JOptionPane.showInputDialog(
                    this,
                    "Introdu motivul anulării:",
                    "Anulare programare",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (reason == null) {
                return;
            }

            reason = reason.trim();

            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Completează motivul anulării.",
                        "Eroare",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            updateStatus(newStatus, reason);
            return;
        }

        updateStatus(newStatus, null);
    }

    private void updateStatus(
            String newStatus,
            String cancellationReason
    ) {
        try {
            appointmentService.updateStatus(
                    selectedAppointment.id(),
                    technicianService.getTechnicianId(),
                    newStatus,
                    cancellationReason
            );

            loadAppointments();

        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Programarea nu a putut fi actualizată.",
                    "Eroare",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JPanel createSectionCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(UiStyle.WHITE);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                UiStyle.BORDER, 1, true
                        ),
                        BorderFactory.createEmptyBorder(
                                18, 22, 18, 22
                        )
                )
        );
        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)
        );
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = UiStyle.label(
                title, 17, true, UiStyle.INK
        );

        JPanel lines = new JPanel();
        lines.setOpaque(false);
        lines.setLayout(new BoxLayout(lines, BoxLayout.Y_AXIS));

        card.add(label, BorderLayout.NORTH);
        card.add(lines, BorderLayout.CENTER);

        card.putClientProperty("lines", lines);

        return card;
    }

    private void addDetailLine(
            JPanel card,
            String label,
            String value
    ) {
        JPanel lines =
                (JPanel) card.getClientProperty("lines");

        JPanel line = new JPanel(new BorderLayout(12, 0));
        line.setOpaque(false);
        line.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 34)
        );
        line.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelText = UiStyle.label(
                label, 13, false, UiStyle.MUTED
        );

        JLabel valueText = new JLabel(
                value == null || value.isBlank() ? "—" : value
        );
        valueText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        valueText.setForeground(UiStyle.INK);

        line.add(labelText, BorderLayout.WEST);
        line.add(valueText, BorderLayout.CENTER);

        lines.add(line);
        lines.add(Box.createVerticalStrut(6));
    }

    private JLabel emptyLabel(String text) {
        JLabel label = UiStyle.label(
                text, 13, false, UiStyle.MUTED
        );
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    private JPanel emptyDetailsPanel(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JLabel label = UiStyle.label(
                text, 14, false, UiStyle.MUTED
        );

        panel.add(label);

        return panel;
    }

    private String money(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString() + " lei";
    }
}
