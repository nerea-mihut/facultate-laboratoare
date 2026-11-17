package salon.ui.technician;

import salon.service.TechnicianService;
import salon.ui.components.RoundedCard;
import salon.ui.components.UiStyle;

import javax.swing.*;
import java.awt.*;

public class TechnicianProfilePanel extends JPanel {

    private final TechnicianService technicianService;

    public TechnicianProfilePanel(
            TechnicianService technicianService
    ) {
        this.technicianService = technicianService;

        setLayout(new GridBagLayout());
        setBackground(UiStyle.CREAM);
        setBorder(
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        );

        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(
                BorderFactory.createEmptyBorder(36, 48, 36, 48)
        );
        card.setPreferredSize(new Dimension(720, 520));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel heading =
                UiStyle.label("Profilul meu", 30, true, UiStyle.INK);
        addRow(card, heading, c, 0, 0, 26);

        addProfileLine(card, c, 1, "Prenume",
                technicianService.getFirstName());
        addProfileLine(card, c, 3, "Nume",
                technicianService.getLastName());
        addProfileLine(card, c, 5, "Adresă de e-mail",
                technicianService.getEmail());
        addProfileLine(card, c, 7, "Telefon",
                technicianService.getPhone());

        add(card);
    }

    private void addProfileLine(
            JPanel card,
            GridBagConstraints c,
            int row,
            String label,
            String value
    ) {
        JLabel labelText =
                UiStyle.label(label, 13, false, UiStyle.MUTED);
        addRow(card, labelText, c, row, 0, 6);

        JLabel valueText = new JLabel(
                value == null || value.isBlank() ? "—" : value
        );
        valueText.setFont(new Font("SansSerif", Font.BOLD, 16));
        valueText.setForeground(UiStyle.INK);
        addRow(card, valueText, c, row + 1, 0, 18);
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