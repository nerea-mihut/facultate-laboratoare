package salon.ui.registry;

import salon.service.SterilizationService;
import salon.ui.components.RoundedCard;
import salon.ui.components.UiStyle;
import salon.ui.model.RegistryRecord;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

public class SterilizationRegistryPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final SterilizationService sterilizationService;
    private final int employeeId;

    private final JPanel recordsPanel = new JPanel();

    public SterilizationRegistryPanel(
            SterilizationService sterilizationService,
            int employeeId
    ) {
        this.sterilizationService = sterilizationService;
        this.employeeId = employeeId;

        setLayout(new BorderLayout(0, 16));
        setBackground(UiStyle.CREAM);
        setBorder(
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        );

        RoundedCard card = new RoundedCard();
        card.setLayout(new BorderLayout(0, 18));
        card.setBorder(
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        );

        JPanel heading = new JPanel(new BorderLayout(12, 0));
        heading.setOpaque(false);

        heading.add(createBackButton(), BorderLayout.WEST);

        JPanel titles = new JPanel(new GridLayout(0, 1, 0, 5));
        titles.setOpaque(false);
        titles.add(UiStyle.label(
                "Registru de sterilizare", 26, true, UiStyle.INK
        ));
        titles.add(UiStyle.label(
                "Înregistrările sunt asociate contului tău.",
                13,
                false,
                UiStyle.MUTED
        ));

        JButton addRecordButton =
                UiStyle.primaryButton("Adaugă înregistrare");
        addRecordButton.setPreferredSize(new Dimension(220, 42));

        addRecordButton.addActionListener(e ->
                openRegistryDialog()
        );

        heading.add(titles, BorderLayout.CENTER);
        heading.add(addRecordButton, BorderLayout.EAST);

        recordsPanel.setLayout(
                new BoxLayout(recordsPanel, BoxLayout.Y_AXIS)
        );
        recordsPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(recordsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getViewport().setBackground(UiStyle.WHITE);

        card.add(heading, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        loadRecords();
    }

    private JButton createBackButton() {
        JButton button = UiStyle.secondaryButton("< Înapoi");
        button.setPreferredSize(new Dimension(110, 38));

        // navigarea înapoi va fi gestionată de dashboard
        // printr-un callback; pentru moment butonul rămâne vizual
        return button;
    }

    private void loadRecords() {
        recordsPanel.removeAll();

        List<RegistryRecord> records =
                sterilizationService.loadRecords(employeeId);

        for (RegistryRecord record : records) {
            recordsPanel.add(
                    createRecordCard(record)
            );
            recordsPanel.add(Box.createVerticalStrut(12));
        }

        if (records.isEmpty()) {
            recordsPanel.add(
                    emptyLabel("Nu există înregistrări în registru.")
            );
        }

        recordsPanel.revalidate();
        recordsPanel.repaint();
    }

    private JPanel createRecordCard(RegistryRecord record) {
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
                new Dimension(Integer.MAX_VALUE, 160)
        );
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel title = UiStyle.label(
                record.date().format(DATE_FORMAT)
                        + " · "
                        + record.device(),
                16,
                true,
                UiStyle.INK
        );
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel details = UiStyle.label(
                record.material()
                        + " · "
                        + record.temperature()
                        + "°C · "
                        + record.startTime().format(TIME_FORMAT)
                        + "–"
                        + record.endTime().format(TIME_FORMAT),
                13,
                false,
                UiStyle.MUTED
        );
        details.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel test = UiStyle.label(
                record.testReference() == null
                        || record.testReference().isBlank()
                        ? "Fără poză martor atașată."
                        : "Martor: " + record.testReference(),
                12,
                false,
                UiStyle.MUTED
        );
        test.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(title);
        info.add(Box.createVerticalStrut(5));
        info.add(details);
        info.add(Box.createVerticalStrut(4));
        info.add(test);

        card.add(info, BorderLayout.CENTER);

        return card;
    }

    private void openRegistryDialog() {
        SterilizationRegistryDialog dialog =
                new SterilizationRegistryDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        sterilizationService,
                        employeeId
                );

        dialog.setVisible(true);

        loadRecords();
    }

    private JLabel emptyLabel(String text) {
        JLabel label = UiStyle.label(
                text, 13, false, UiStyle.MUTED
        );
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }
}