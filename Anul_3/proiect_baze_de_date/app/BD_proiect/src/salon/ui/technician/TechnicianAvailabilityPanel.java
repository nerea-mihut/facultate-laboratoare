package salon.ui.technician;

import salon.service.AvailabilityService;
import salon.service.AvailabilityService.StoredInterval;
import salon.service.TechnicianService;
import salon.ui.components.CalendarGrid;
import salon.ui.components.RoundedCard;
import salon.ui.components.UiStyle;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TechnicianAvailabilityPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final AvailabilityService availabilityService;
    private final Integer employeeId;

    private final CalendarGrid calendar =
            new CalendarGrid();

    private final JPanel intervalsPanel = new JPanel();

    private final JPanel hoursPanel =
            new JPanel(new GridLayout(0, 4, 10, 10));

    private final JLabel dateTitle =
            UiStyle.label(
                    "Selectează o zi",
                    18,
                    true,
                    UiStyle.INK
            );

    private final JLabel statusLabel =
            UiStyle.label(
                    " ",
                    12,
                    false,
                    UiStyle.MUTED
            );

    private final Map<LocalDate, CalendarGrid.DayState> states =
            new HashMap<>();

    private final JButton addWorkButton =
            UiStyle.primaryButton("Adaugă interval de lucru");

    private final JButton addAbsenceButton =
            UiStyle.secondaryButton("Adaugă pauză / absență");

    private final JButton dayOffButton =
            UiStyle.secondaryButton("Nu lucrez / Liber");

    private final JButton refreshButton =
            UiStyle.secondaryButton("Reîmprospătează");

    private LocalDate selectedDate;
    private boolean busy;
    private long refreshVersion;

    public TechnicianAvailabilityPanel(
            AvailabilityService availabilityService,
            TechnicianService technicianService
    ) {
        this.availabilityService = availabilityService;
        this.employeeId =
                technicianService.getTechnicianId();

        setLayout(new BorderLayout());
        setBackground(UiStyle.CREAM);
        setBorder(
                BorderFactory.createEmptyBorder(
                        24, 24, 24, 24
                )
        );

        RoundedCard card = new RoundedCard();
        card.setLayout(new BorderLayout(0, 18));
        card.setBorder(
                BorderFactory.createEmptyBorder(
                        24, 28, 24, 28
                )
        );

        JPanel heading =
                new JPanel(new BorderLayout(12, 0));
        heading.setOpaque(false);

        JButton backButton =
                UiStyle.secondaryButton("< Înapoi");

        backButton.setPreferredSize(
                new Dimension(110, 38)
        );
        backButton.addActionListener(e -> goHome());

        heading.add(backButton, BorderLayout.WEST);

        JPanel titles = new JPanel(new GridLayout(0, 1, 0, 5));
        titles.setOpaque(false);

        titles.add(UiStyle.label(
                "Calendar disponibilitate",
                26,
                true,
                UiStyle.INK
        ));

        titles.add(UiStyle.label(
                "Selectează orice zi pentru a administra programul.",
                13,
                false,
                UiStyle.MUTED
        ));

        heading.add(titles, BorderLayout.CENTER);
        heading.add(refreshButton, BorderLayout.EAST);

        calendar.setDayStateProvider(date ->
                states.getOrDefault(
                        date,
                        CalendarGrid.DayState.NEUTRAL
                )
        );

        calendar.setDateEnabledPredicate(date -> !busy);

        calendar.setDateSelectionListener(date -> {
            selectedDate = date;
            refreshData();
        });

        /*
         * CalendarGrid își păstrează providerul.
         * La schimbarea lunii încărcăm marcajele noii luni.
         */
        Timer monthWatcher = new Timer(300, e -> {
            if (!busy
                    && isShowing()
                    && !calendar.getDisplayedMonth()
                    .equals(loadedMonth)) {
                refreshData();
            }
        });

        addHierarchyListener(e -> {
            if (isShowing()) {
                monthWatcher.start();

                if (!busy) {
                    refreshData();
                }
            } else {
                monthWatcher.stop();
            }
        });

        intervalsPanel.setOpaque(false);
        intervalsPanel.setLayout(
                new BoxLayout(
                        intervalsPanel,
                        BoxLayout.Y_AXIS
                )
        );

        hoursPanel.setOpaque(false);

        JPanel actions =
                new JPanel(new FlowLayout(
                        FlowLayout.LEFT, 8, 0
                ));
        actions.setOpaque(false);

        actions.add(addWorkButton);
        actions.add(addAbsenceButton);
        actions.add(dayOffButton);

        addWorkButton.addActionListener(e ->
                openIntervalDialog(null, "Lucru")
        );

        addAbsenceButton.addActionListener(e ->
                openIntervalDialog(null, "Absenta")
        );

        dayOffButton.addActionListener(e -> markDayOff());

        refreshButton.addActionListener(e -> refreshData());

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(
                new BoxLayout(body, BoxLayout.Y_AXIS)
        );

        calendar.setAlignmentX(Component.LEFT_ALIGNMENT);
        calendar.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 360)
        );

        body.add(calendar);
        body.add(Box.createVerticalStrut(12));

        JLabel legend = UiStyle.label(
                "Verde: loc pentru 60 min · "
                        + "Roșu: indisponibil · "
                        + "Neutru: fără program configurat",
                12,
                false,
                UiStyle.MUTED
        );
        legend.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(legend);
        body.add(Box.createVerticalStrut(20));

        dateTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(dateTitle);
        body.add(Box.createVerticalStrut(12));

        JPanel intervalSection =
                new JPanel(new BorderLayout(0, 12));
        intervalSection.setOpaque(false);
        intervalSection.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        intervalSection.add(
                intervalsPanel,
                BorderLayout.CENTER
        );
        intervalSection.add(
                actions,
                BorderLayout.SOUTH
        );

        body.add(intervalSection);
        body.add(Box.createVerticalStrut(20));

        JLabel hoursTitle = UiStyle.label(
                "Exemplu de ore disponibile pentru 60 minute",
                15,
                true,
                UiStyle.INK
        );

        hoursTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(hoursTitle);
        body.add(Box.createVerticalStrut(10));

        JPanel hoursWrapper =
                new JPanel(new BorderLayout());
        hoursWrapper.setOpaque(false);
        hoursWrapper.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );
        hoursWrapper.add(hoursPanel, BorderLayout.NORTH);

        body.add(hoursWrapper);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        card.add(heading, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(statusLabel, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        updateButtons();

        if (employeeId == null) {
            showError(
                    "Contul nu este asociat unui tehnician încărcat."
            );
        }
    }

    private YearMonth loadedMonth;

    public void refreshData() {
        if (busy || employeeId == null) {
            return;
        }

        YearMonth month = calendar.getDisplayedMonth();
        LocalDate date = selectedDate;
        long version = ++refreshVersion;

        setBusy(true);
        statusLabel.setForeground(UiStyle.MUTED);
        statusLabel.setText("Se încarcă disponibilitatea...");

        new SwingWorker<Snapshot, Void>() {
            @Override
            protected Snapshot doInBackground() {
                Map<LocalDate, CalendarGrid.DayState> colors =
                        new HashMap<>();

                for (int day = 1;
                     day <= month.lengthOfMonth();
                     day++) {

                    LocalDate current = month.atDay(day);

                    List<StoredInterval> intervals =
                            availabilityService
                                    .loadStoredDayIntervals(
                                            employeeId, current
                                    );

                    CalendarGrid.DayState state;

                    if (intervals.isEmpty()) {
                        state = CalendarGrid.DayState.NEUTRAL;
                    } else if (
                            availabilityService.availableSlots(
                                    employeeId, current, 60
                            ).isEmpty()
                    ) {
                        state = CalendarGrid.DayState.UNAVAILABLE;
                    } else {
                        state = CalendarGrid.DayState.AVAILABLE;
                    }

                    colors.put(current, state);
                }

                List<StoredInterval> intervals =
                        date == null
                                ? List.of()
                                : availabilityService
                                .loadStoredDayIntervals(
                                        employeeId, date
                                );

                List<LocalDateTime> slots =
                        date == null
                                ? List.of()
                                : availabilityService.availableSlots(
                                employeeId, date, 60
                        );

                return new Snapshot(colors, intervals, slots);
            }

            @Override
            protected void done() {
                if (version != refreshVersion) {
                    return;
                }

                setBusy(false);

                try {
                    Snapshot snapshot = get();

                    states.clear();
                    states.putAll(snapshot.states());

                    loadedMonth = month;
                    calendar.refresh();

                    renderDay(
                            date,
                            snapshot.intervals(),
                            snapshot.slots()
                    );

                    statusLabel.setForeground(UiStyle.MUTED);
                    statusLabel.setText(
                            "Programul este încărcat. "
                                    + "Orele afișate sunt orientative "
                                    + "pentru servicii de 60 minute."
                    );

                } catch (Exception exception) {
                    loadedMonth = month;
                    showException(exception);
                }
            }
        }.execute();
    }

    private void renderDay(
            LocalDate date,
            List<StoredInterval> intervals,
            List<LocalDateTime> slots
    ) {
        intervalsPanel.removeAll();
        hoursPanel.removeAll();

        dateTitle.setText(
                date == null
                        ? "Selectează o zi"
                        : "Program · " + date.format(DATE_FORMAT)
        );

        if (date != null) {
            if (intervals.isEmpty()) {
                intervalsPanel.add(
                        UiStyle.label(
                                "Nu ai configurat programul acestei zile.",
                                13,
                                false,
                                UiStyle.MUTED
                        )
                );
            }

            for (StoredInterval interval : intervals) {
                intervalsPanel.add(
                        createIntervalCard(interval)
                );
                intervalsPanel.add(
                        Box.createVerticalStrut(8)
                );
            }

            if (slots.isEmpty()) {
                hoursPanel.add(
                        UiStyle.label(
                                "Nu există ore disponibile pentru 60 min.",
                                12,
                                false,
                                UiStyle.MUTED
                        )
                );
            } else {
                for (LocalDateTime slot : slots) {
                    JLabel hour = UiStyle.label(
                            slot.format(TIME_FORMAT),
                            13,
                            true,
                            UiStyle.INK
                    );
                    hour.setHorizontalAlignment(
                            SwingConstants.CENTER
                    );
                    hour.setBorder(
                            BorderFactory.createEmptyBorder(
                                    10, 8, 10, 8
                            )
                    );
                    hoursPanel.add(hour);
                }
            }
        }

        intervalsPanel.revalidate();
        intervalsPanel.repaint();
        hoursPanel.revalidate();
        hoursPanel.repaint();

        updateButtons();
    }

    private JPanel createIntervalCard(
            StoredInterval interval
    ) {
        JPanel card =
                new JPanel(new BorderLayout(12, 0));

        card.setBackground(UiStyle.FIELD);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 66)
        );
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                UiStyle.BORDER, 1, true
                        ),
                        BorderFactory.createEmptyBorder(
                                10, 14, 10, 14
                        )
                )
        );

        String endText =
                interval.end().equals(
                        interval.start()
                                .toLocalDate()
                                .plusDays(1)
                                .atStartOfDay()
                )
                        ? "24:00"
                        : interval.end().format(TIME_FORMAT);

        JLabel text = UiStyle.label(
                interval.start().format(TIME_FORMAT)
                        + " – " + endText
                        + " · "
                        + (
                        "Lucru".equals(interval.type())
                                ? "Lucru"
                                : "Pauză / absență"
                ),
                13,
                false,
                UiStyle.INK
        );

        JPanel actions =
                new JPanel(new FlowLayout(
                        FlowLayout.RIGHT, 8, 0
                ));
        actions.setOpaque(false);

        JButton edit =
                UiStyle.secondaryButton("Modifică");
        JButton delete =
                UiStyle.secondaryButton("Șterge");

        edit.setEnabled(!busy);
        delete.setEnabled(!busy);

        edit.addActionListener(e -> {
            if (!busy) {
                openIntervalDialog(
                        interval,
                        interval.type()
                );
            }
        });

        delete.addActionListener(e -> {
            if (busy) {
                return;
            }

            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Ștergi acest interval?\n"
                            + "Programările existente nu se șterg.",
                    "Șterge interval",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (answer == JOptionPane.YES_OPTION) {
                runWrite(() ->
                        availabilityService.deleteStoredInterval(
                                employeeId, interval.id()
                        )
                );
            }
        });

        actions.add(edit);
        actions.add(delete);

        card.add(text, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);

        return card;
    }

    private void openIntervalDialog(
            StoredInterval existing,
            String type
    ) {
        if (busy || selectedDate == null) {
            return;
        }

        LocalDate date = existing == null
                ? selectedDate
                : existing.start().toLocalDate();

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                existing == null
                        ? "Adaugă interval"
                        : "Modifică interval",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE
        );

        JPanel form =
                new JPanel(new GridBagLayout());

        form.setBackground(UiStyle.WHITE);
        form.setBorder(
                BorderFactory.createEmptyBorder(
                        24, 28, 24, 28
                )
        );

        GridBagConstraints c =
                new GridBagConstraints();

        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 12, 0);

        JTextField startField = new JTextField(
                existing == null
                        ? "09:00"
                        : existing.start().format(TIME_FORMAT)
        );

        String initialEnd = "12:00";

        if (existing != null) {
            initialEnd = existing.end().equals(
                    date.plusDays(1).atStartOfDay()
            )
                    ? "24:00"
                    : existing.end().format(TIME_FORMAT);
        }

        JTextField endField =
                new JTextField(initialEnd);

        UiStyle.styleField(startField);
        UiStyle.styleField(endField);

        JLabel message = UiStyle.label(
                "Poți folosi 24:00 pentru sfârșitul zilei.",
                12,
                false,
                UiStyle.MUTED
        );

        JButton save =
                UiStyle.primaryButton("Salvează intervalul");

        Component[] items = {
                UiStyle.label(
                        date.format(DATE_FORMAT)
                                + " · "
                                + (
                                "Lucru".equals(type)
                                        ? "Lucru"
                                        : "Pauză / absență"
                        ),
                        19,
                        true,
                        UiStyle.INK
                ),
                UiStyle.label(
                        "Ora de început",
                        13, false, UiStyle.INK
                ),
                startField,
                UiStyle.label(
                        "Ora de sfârșit",
                        13, false, UiStyle.INK
                ),
                endField,
                save,
                message
        };

        for (int i = 0; i < items.length; i++) {
            c.gridy = i;
            form.add(items[i], c);
        }

        save.addActionListener(e -> {
            LocalDateTime start;
            LocalDateTime end;

            try {
                start = date.atTime(
                        LocalTime.parse(
                                startField.getText().trim()
                        )
                );

                String endText =
                        endField.getText().trim();

                end = "24:00".equals(endText)
                        ? date.plusDays(1).atStartOfDay()
                        : date.atTime(
                        LocalTime.parse(endText)
                );

                if (!end.isAfter(start)) {
                    throw new IllegalArgumentException(
                            "Sfârșitul trebuie să fie după început."
                    );
                }

            } catch (RuntimeException exception) {
                message.setForeground(UiStyle.RED);
                message.setText(
                        "<html>Verifică orele: 09:00, 14:00 "
                                + "sau 24:00 la sfârșit.</html>"
                );
                return;
            }

            save.setEnabled(false);
            dialog.setDefaultCloseOperation(
                    WindowConstants.DO_NOTHING_ON_CLOSE
            );

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground()
                        throws Exception {

                    if (existing == null) {
                        availabilityService.saveInterval(
                                employeeId, start, end, type
                        );
                    } else {
                        availabilityService.updateInterval(
                                employeeId,
                                existing.id(),
                                start,
                                end,
                                type
                        );
                    }

                    return null;
                }

                @Override
                protected void done() {
                    save.setEnabled(true);
                    dialog.setDefaultCloseOperation(
                            WindowConstants.DISPOSE_ON_CLOSE
                    );

                    try {
                        get();
                        dialog.dispose();
                        refreshData();

                    } catch (Exception exception) {
                        Throwable cause = unwrap(exception);

                        message.setForeground(UiStyle.RED);
                        message.setText(
                                "<html>"
                                        + escape(cause.getMessage())
                                        + "</html>"
                        );
                    }
                }
            }.execute();
        });

        dialog.setContentPane(form);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(460, 400));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void markDayOff() {
        if (busy || selectedDate == null) {
            return;
        }

        LocalDate date = selectedDate;

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Marchezi " + date.format(DATE_FORMAT)
                        + " ca zi liberă?\n"
                        + "Intervalele acelei zile vor fi înlocuite "
                        + "cu o absență de zi întreagă.\n"
                        + "Dacă există programări sau cursuri, "
                        + "operația va fi refuzată.",
                "Zi liberă",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (answer == JOptionPane.YES_OPTION) {
            runWrite(() ->
                    availabilityService.markDayOff(
                            employeeId, date
                    )
            );
        }
    }

    private void runWrite(WriteOperation operation) {
        if (busy) {
            return;
        }

        setBusy(true);
        statusLabel.setForeground(UiStyle.MUTED);
        statusLabel.setText("Se salvează...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground()
                    throws Exception {

                operation.run();
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);

                try {
                    get();
                    refreshData();
                } catch (Exception exception) {
                    showException(exception);
                }
            }
        }.execute();
    }

    private void setBusy(boolean value) {
        busy = value;
        updateButtons();
        calendar.refresh();
    }

    private void updateButtons() {
        boolean canEdit = !busy
                && employeeId != null
                && selectedDate != null;

        addWorkButton.setEnabled(canEdit);
        addAbsenceButton.setEnabled(canEdit);
        dayOffButton.setEnabled(canEdit);

        refreshButton.setEnabled(
                !busy && employeeId != null
        );
    }

    private void goHome() {
        if (busy) {
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
    }

    private void showException(Exception exception) {
        Throwable cause = unwrap(exception);
        cause.printStackTrace();

        showError(
                cause.getMessage() == null
                        ? "Operația nu a putut fi efectuată."
                        : cause.getMessage()
        );
    }

    private Throwable unwrap(Throwable exception) {
        Throwable result = exception;

        while (
                (result instanceof ExecutionException
                        || result instanceof
                        AvailabilityService.AvailabilityException)
                        && result.getCause() != null
        ) {
            result = result.getCause();
        }

        return result;
    }

    private void showError(String message) {
        statusLabel.setForeground(UiStyle.RED);
        statusLabel.setText(
                "<html>" + escape(message) + "</html>"
        );
    }

    private String escape(String text) {
        if (text == null) {
            return "Operația nu a putut fi efectuată.";
        }

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private record Snapshot(
            Map<LocalDate, CalendarGrid.DayState> states,
            List<StoredInterval> intervals,
            List<LocalDateTime> slots
    ) {
    }

    @FunctionalInterface
    private interface WriteOperation {
        void run() throws Exception;
    }
}