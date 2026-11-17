package salon.ui.technician;

import salon.service.TechnicianService;
import salon.ui.components.RoundedCard;
import salon.ui.components.UiStyle;

import javax.swing.*;
import java.awt.*;

public class TechnicianHomePanel extends JPanel {

    private final TechnicianService technicianService;

    public TechnicianHomePanel(
            TechnicianService technicianService,
            Runnable onShowAppointments,
            Runnable onShowAvailability,
            Runnable onShowRegistry
    ) {
        this.technicianService = technicianService;

        setLayout(new GridBagLayout());
        setBackground(UiStyle.CREAM);
        setBorder(
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        );

        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(
                BorderFactory.createEmptyBorder(40, 46, 40, 46)
        );
        card.setPreferredSize(new Dimension(520, 380));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        String greeting = "Bine ai venit!";

        if (technicianService.getFirstName() != null
                && !technicianService.getFirstName().isBlank()) {

            greeting = "Bine ai venit, "
                    + technicianService.getFirstName() + "!";
        }

        JLabel heading =
                UiStyle.label(greeting, 30, true, UiStyle.INK);
        addRow(card, heading, c, 0, 0, 32);

        JButton appointmentsButton =
                createMenuButton("Programări curente");

        appointmentsButton.addActionListener(e ->
                onShowAppointments.run()
        );

        addRow(card, appointmentsButton, c, 1, 0, 16);

        JButton availabilityButton =
                createMenuButton("Calendar disponibilitate");

        availabilityButton.addActionListener(e ->
                onShowAvailability.run()
        );

        addRow(card, availabilityButton, c, 2, 0, 16);

        JButton registryButton =
                createMenuButton("Registru de sterilizare");

        registryButton.addActionListener(e ->
                onShowRegistry.run()
        );

        addRow(card, registryButton, c, 3, 0, 0);

        add(card);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setBackground(UiStyle.FIELD);
        button.setForeground(UiStyle.INK);
        button.setPreferredSize(new Dimension(420, 58));
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

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
}