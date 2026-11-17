package salon.ui.components;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class DatePickerDialog extends JDialog {

    private LocalDate result;

    private final CalendarGrid calendar =
            new CalendarGrid();

    public DatePickerDialog(
            Frame owner,
            String title,
            LocalDate initialDate
    ) {
        this(
                (Window) owner,
                title,
                initialDate
        );
    }

    public DatePickerDialog(
            Window owner,
            String title,
            LocalDate initialDate
    ) {
        super(
                owner,
                title,
                Dialog.ModalityType.APPLICATION_MODAL
        );

        setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE
        );

        JPanel background =
                new JPanel(new BorderLayout());

        background.setBackground(UiStyle.CREAM);
        background.setBorder(
                BorderFactory.createEmptyBorder(
                        16, 16, 16, 16
                )
        );

        RoundedCard card = new RoundedCard();

        card.setLayout(
                new BorderLayout(0, 16)
        );

        card.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 20, 20, 20
                )
        );

        LocalDate startingDate = initialDate == null
                ? LocalDate.now()
                : initialDate;

        calendar.setPreferredSize(
                new Dimension(390, 330)
        );

        calendar.setDayStateProvider(
                date -> CalendarGrid.DayState.NEUTRAL
        );

        calendar.setDateEnabledPredicate(
                date -> true
        );

        calendar.setSelectedDate(startingDate);

        calendar.setDateSelectionListener(date -> {
            result = date;
            dispose();
        });

        JPanel actions = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        8,
                        0
                )
        );

        actions.setOpaque(false);

        JButton todayButton =
                UiStyle.secondaryButton("Astăzi");

        todayButton.addActionListener(e ->
                calendar.setSelectedDate(
                        LocalDate.now()
                )
        );

        JButton cancelButton =
                UiStyle.secondaryButton("Anulează");

        cancelButton.addActionListener(e -> {
            result = null;
            dispose();
        });

        actions.add(todayButton);
        actions.add(cancelButton);

        card.add(
                calendar,
                BorderLayout.CENTER
        );

        card.add(
                actions,
                BorderLayout.SOUTH
        );

        background.add(
                card,
                BorderLayout.CENTER
        );

        setContentPane(background);

        getRootPane().registerKeyboardAction(
                e -> {
                    result = null;
                    dispose();
                },
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        pack();

        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }

    public LocalDate showDialog() {
        result = null;
        setVisible(true);
        return result;
    }
}