package salon.ui.client;

import salon.service.ClientService;
import salon.service.ClientService.ActivityTable;
import salon.ui.components.RoundedCard;
import salon.ui.components.UiStyle;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientActivityPanel extends JPanel {

    private final ClientService clientService;
    private final String userEmail;

    private final JTable appointmentsTable = createTable();
    private final JTable enrollmentsTable = createTable();
    private final JTable waitingListTable = createTable();

    private final JLabel statusLabel =
            UiStyle.label(" ", 12, false, UiStyle.MUTED);

    private final JButton refreshButton =
            UiStyle.secondaryButton("Reîmprospătează");

    private boolean loading;
    private long requestNumber;

    public ClientActivityPanel(
            ClientService clientService,
            String userEmail,
            Runnable onBack
    ) {
        this.clientService = clientService;
        this.userEmail = userEmail;

        setLayout(new BorderLayout());
        setBackground(UiStyle.CREAM);
        setBorder(
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        );

        RoundedCard card = new RoundedCard();
        card.setLayout(new BorderLayout(0, 18));
        card.setBorder(
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        );

        JPanel heading = new JPanel(new BorderLayout(14, 0));
        heading.setOpaque(false);

        JButton backButton =
                UiStyle.secondaryButton("< Înapoi");

        backButton.setPreferredSize(new Dimension(110, 38));
        backButton.addActionListener(e -> onBack.run());

        JLabel title = UiStyle.label(
                "Activitatea mea",
                26,
                true,
                UiStyle.INK
        );

        refreshButton.addActionListener(e -> refreshData());

        heading.add(backButton, BorderLayout.WEST);
        heading.add(title, BorderLayout.CENTER);
        heading.add(refreshButton, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabs.setBackground(UiStyle.WHITE);
        tabs.setForeground(UiStyle.INK);

        tabs.addTab(
                "Programările mele",
                createTablePage(
                        appointmentsTable,
                        "Programările tale la salon, inclusiv cele anulate."
                )
        );

        tabs.addTab(
                "Cursurile mele",
                createTablePage(
                        enrollmentsTable,
                        "Înscrierile tale la cursuri și starea lor."
                )
        );

        tabs.addTab(
                "Lista de așteptare",
                createTablePage(
                        waitingListTable,
                        "Sesiunile pentru care aștepți un loc. "
                                + "Un loc neocupat nu înseamnă înscriere automată."
                )
        );

        card.add(heading, BorderLayout.NORTH);
        card.add(tabs, BorderLayout.CENTER);
        card.add(statusLabel, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    public void refreshData() {
        if (loading) {
            return;
        }

        loading = true;
        long currentRequest = ++requestNumber;

        refreshButton.setEnabled(false);
        statusLabel.setForeground(UiStyle.MUTED);
        statusLabel.setText("Se încarcă activitatea...");

        new SwingWorker<List<ActivityTable>, Void>() {

            @Override
            protected List<ActivityTable> doInBackground()
                    throws Exception {

                return List.of(
                        clientService.loadAppointments(userEmail),
                        clientService.loadEnrollments(userEmail),
                        clientService.loadWaitingList(userEmail)
                );
            }

            @Override
            protected void done() {
                if (currentRequest != requestNumber) {
                    return;
                }

                loading = false;
                refreshButton.setEnabled(true);

                try {
                    List<ActivityTable> data = get();

                    applyData(appointmentsTable, data.get(0));
                    applyData(enrollmentsTable, data.get(1));
                    applyData(waitingListTable, data.get(2));

                    statusLabel.setForeground(UiStyle.MUTED);
                    statusLabel.setText(
                            data.get(0).rows().size()
                                    + " programări · "
                                    + data.get(1).rows().size()
                                    + " înscrieri la cursuri · "
                                    + data.get(2).rows().size()
                                    + " sesiuni pe lista de așteptare"
                    );

                } catch (Exception exception) {
                    exception.printStackTrace();

                    clearTable(appointmentsTable);
                    clearTable(enrollmentsTable);
                    clearTable(waitingListTable);

                    statusLabel.setForeground(UiStyle.RED);
                    statusLabel.setText(
                            "Activitatea nu a putut fi încărcată. "
                                    + "Apasă Reîmprospătează pentru a reîncerca."
                    );
                }
            }
        }.execute();
    }

    private JTable createTable() {
        JTable table = new JTable();

        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setBackground(UiStyle.WHITE);
        table.setForeground(UiStyle.INK);
        table.setSelectionBackground(UiStyle.BEIGE);
        table.setSelectionForeground(UiStyle.INK);
        table.setGridColor(UiStyle.BORDER);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );
        table.getTableHeader().setBackground(UiStyle.FIELD);
        table.getTableHeader().setForeground(UiStyle.INK);
        table.getTableHeader().setReorderingAllowed(false);

        table.setDefaultRenderer(
                Object.class,
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable currentTable,
                            Object value,
                            boolean selected,
                            boolean focused,
                            int row,
                            int column
                    ) {
                        super.getTableCellRendererComponent(
                                currentTable,
                                formatValue(value),
                                selected,
                                focused,
                                row,
                                column
                        );

                        setBorder(
                                BorderFactory.createEmptyBorder(
                                        0, 10, 0, 10
                                )
                        );

                        setToolTipText(formatValue(value));
                        return this;
                    }
                }
        );

        return table;
    }

    private JPanel createTablePage(
            JTable table,
            String description
    ) {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setBackground(UiStyle.WHITE);
        page.setBorder(
                BorderFactory.createEmptyBorder(16, 4, 4, 4)
        );

        JLabel descriptionLabel = UiStyle.label(
                "<html>" + description + "</html>",
                13,
                false,
                UiStyle.MUTED
        );

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(
                BorderFactory.createLineBorder(UiStyle.BORDER)
        );
        scroll.getViewport().setBackground(UiStyle.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        page.add(descriptionLabel, BorderLayout.NORTH);
        page.add(scroll, BorderLayout.CENTER);

        return page;
    }

    private void applyData(
            JTable table,
            ActivityTable data
    ) {
        DefaultTableModel model = new DefaultTableModel(
                data.columns(),
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Object[] row : data.rows()) {
            model.addRow(row);
        }

        table.setModel(model);

        for (int index = 0;
             index < table.getColumnCount();
             index++) {

            String name = table.getColumnName(index);

            int width = switch (name) {
                case "Servicii", "Motiv anulare" -> 300;
                case "Curs", "Trainer", "Tehnician" -> 200;
                case "Adăugat la" -> 175;
                default -> 145;
            };

            table.getColumnModel()
                    .getColumn(index)
                    .setPreferredWidth(width);
        }
    }

    private void clearTable(JTable table) {
        if (table.getModel() instanceof DefaultTableModel model) {
            model.setRowCount(0);
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "—";
        }

        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().format(
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            );
        }

        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().format(
                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
            );
        }

        if (value instanceof java.sql.Time time) {
            return time.toLocalTime().format(
                    DateTimeFormatter.ofPattern("HH:mm")
            );
        }

        return value.toString();
    }
}