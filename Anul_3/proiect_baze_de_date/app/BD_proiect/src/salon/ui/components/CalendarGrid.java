package salon.ui.components;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CalendarGrid extends JPanel {

    public enum DayState {
        NEUTRAL,
        AVAILABLE,
        UNAVAILABLE
    }

    private static final Locale RO =
            Locale.forLanguageTag("ro-RO");

    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MMMM yyyy", RO);

    private final JPanel grid =
            new JPanel(new GridLayout(7, 7, 7, 7));

    private final JLabel monthLabel =
            new JLabel("", SwingConstants.CENTER);

    private YearMonth displayedMonth = YearMonth.now();
    private LocalDate selectedDate;

    private Consumer<LocalDate> dateSelectionListener;

    private Function<LocalDate, DayState> dayStateProvider =
            date -> DayState.NEUTRAL;

    private Predicate<LocalDate> dateEnabledPredicate =
            date -> true;

    public CalendarGrid() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);

        JPanel navigation =
                new JPanel(new BorderLayout(12, 0));

        navigation.setOpaque(false);

        JButton previous =
                UiStyle.secondaryButton("<");

        JButton next =
                UiStyle.secondaryButton(">");

        previous.setPreferredSize(new Dimension(42, 36));
        next.setPreferredSize(new Dimension(42, 36));

        previous.addActionListener(e ->
                setDisplayedMonth(
                        displayedMonth.minusMonths(1)
                )
        );

        next.addActionListener(e ->
                setDisplayedMonth(
                        displayedMonth.plusMonths(1)
                )
        );

        monthLabel.setFont(
                new Font("SansSerif", Font.BOLD, 18)
        );
        monthLabel.setForeground(UiStyle.INK);

        navigation.add(previous, BorderLayout.WEST);
        navigation.add(monthLabel, BorderLayout.CENTER);
        navigation.add(next, BorderLayout.EAST);

        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(520, 300));

        setMinimumSize(new Dimension(300, 280));

        add(navigation, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);

        refresh();
    }

    public void setDateSelectionListener(
            Consumer<LocalDate> listener
    ) {
        dateSelectionListener = listener;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public YearMonth getDisplayedMonth() {
        return displayedMonth;
    }

    public void setDisplayedMonth(YearMonth month) {
        displayedMonth = Objects.requireNonNull(month);
        refresh();
    }

    public void setSelectedDate(LocalDate date) {
        selectedDate = date;

        if (date != null) {
            displayedMonth = YearMonth.from(date);
        }

        refresh();
    }

    public void setDayStateProvider(
            Function<LocalDate, DayState> provider
    ) {
        dayStateProvider = provider == null
                ? date -> DayState.NEUTRAL
                : provider;

        refresh();
    }

    public void setDateEnabledPredicate(
            Predicate<LocalDate> predicate
    ) {
        dateEnabledPredicate = predicate == null
                ? date -> true
                : predicate;

        refresh();
    }

    /*
     * Compatibilitate cu panoul existent.
     * Disponibilitatea controlează culoarea, nu posibilitatea
     * de a apăsa pe o zi.
     */
    public void refresh(
            Function<LocalDate, Boolean> availabilityChecker
    ) {
        dayStateProvider = availabilityChecker == null
                ? date -> DayState.NEUTRAL
                : date -> Boolean.TRUE.equals(
                availabilityChecker.apply(date)
        )
                ? DayState.AVAILABLE
                : DayState.UNAVAILABLE;

        refresh();
    }

    public void refresh() {
        grid.removeAll();

        String month = displayedMonth.format(MONTH_FORMAT);

        monthLabel.setText(
                month.substring(0, 1).toUpperCase(RO)
                        + month.substring(1)
        );

        for (String day : new String[]{
                "Lu", "Ma", "Mi", "Jo", "Vi", "Sâ", "Du"
        }) {
            JLabel heading = UiStyle.label(
                    day,
                    12,
                    true,
                    UiStyle.MUTED
            );

            heading.setHorizontalAlignment(
                    SwingConstants.CENTER
            );

            grid.add(heading);
        }

        int offset = displayedMonth.atDay(1)
                .getDayOfWeek()
                .getValue() - 1;

        for (int i = 0; i < offset; i++) {
            grid.add(new JLabel());
        }

        for (int day = 1;
             day <= displayedMonth.lengthOfMonth();
             day++) {

            LocalDate date =
                    displayedMonth.atDay(day);

            DayState state =
                    dayStateProvider.apply(date);

            if (state == null) {
                state = DayState.NEUTRAL;
            }

            DayButton button =
                    new DayButton(date, state);

            button.setEnabled(
                    dateEnabledPredicate.test(date)
            );

            button.setSelected(
                    date.equals(selectedDate)
            );

            button.addActionListener(e -> {
                selectedDate = date;

                for (Component component :
                        grid.getComponents()) {

                    if (component instanceof DayButton item) {
                        item.setSelected(item == button);
                        item.repaint();
                    }
                }

                if (dateSelectionListener != null) {
                    dateSelectionListener.accept(date);
                }
            });

            grid.add(button);
        }

        while (grid.getComponentCount() < 49) {
            grid.add(new JLabel());
        }

        grid.revalidate();
        grid.repaint();
    }

    private static class DayButton extends JButton {

        private final LocalDate date;
        private final DayState state;

        DayButton(
                LocalDate date,
                DayState state
        ) {
            this.date = date;
            this.state = state;

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);

            setMargin(new Insets(0, 0, 0, 0));

            setFont(
                    new Font("SansSerif", Font.BOLD, 14)
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            String description = switch (state) {
                case AVAILABLE -> "Disponibilă";
                case UNAVAILABLE -> "Indisponibilă";
                case NEUTRAL -> "Fără marcaj";
            };

            setToolTipText(
                    date.format(
                            DateTimeFormatter.ofPattern(
                                    "dd.MM.yyyy"
                            )
                    ) + " — " + description
            );

            getAccessibleContext().setAccessibleName(
                    getToolTipText()
            );
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g =
                    (Graphics2D) graphics.create();

            try {
                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g.setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                );

                Color marker = switch (state) {
                    case AVAILABLE -> UiStyle.GREEN;
                    case UNAVAILABLE -> UiStyle.RED;
                    case NEUTRAL -> UiStyle.BORDER;
                };

                Color background = isSelected()
                        ? UiStyle.BEIGE
                        : UiStyle.FIELD;

                if (getModel().isRollover()
                        && isEnabled()
                        && !isSelected()) {

                    background =
                            new Color(239, 231, 218);
                }

                g.setColor(background);

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
                                ? UiStyle.INK
                                : UiStyle.BORDER
                );

                g.drawRoundRect(
                        0,
                        0,
                        getWidth() - 1,
                        getHeight() - 1,
                        16,
                        16
                );

                if (state != DayState.NEUTRAL) {
                    int inset = Math.max(
                            6,
                            getWidth() / 5
                    );

                    g.setColor(marker);

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

                String text =
                        String.valueOf(date.getDayOfMonth());

                g.setFont(getFont());

                g.setColor(
                        isEnabled()
                                ? UiStyle.INK
                                : UiStyle.MUTED
                );

                FontMetrics metrics =
                        g.getFontMetrics();

                int x = (
                        getWidth() - metrics.stringWidth(text)
                ) / 2;

                int y = (
                        getHeight() - metrics.getHeight()
                ) / 2 + metrics.getAscent() - 1;

                g.drawString(text, x, y);

            } finally {
                g.dispose();
            }
        }
    }
}