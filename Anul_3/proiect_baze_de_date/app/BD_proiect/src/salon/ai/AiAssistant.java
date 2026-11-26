package salon.ai;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;

/**
 * Butonul-pastilă "Discută cu asistentul AI" și fereastra de chat.
 *
 * Utilizare, o singură linie în constructorul dashboard-ului:
 *   AiAssistant.installForClient(this, this::showAppointments);
 *   AiAssistant.installForEmployee(this, access.employeeId(), access.technician(), access.trainer());
 */
public final class AiAssistant {

    public enum Corner {
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }

    private static final Color INK = new Color(30, 30, 30);
    private static final Color MUTED = new Color(115, 110, 103);
    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color FIELD = new Color(250, 249, 247);
    private static final Color BORDER = new Color(225, 220, 213);
    private static final Color BEIGE = new Color(231, 221, 205);
    private static final Color HOVER = new Color(220, 208, 188);

    private static final int PILL_WIDTH = 250;
    private static final int PILL_HEIGHT = 46;
    private static final int MARGIN = 24;

    private final JFrame frame;
    private final AssistantEngine engine;
    private final Runnable openBooking;
    private final StringBuilder html = new StringBuilder();

    private JDialog dialog;
    private JEditorPane chat;
    private JTextField input;
    private JButton sendButton;
    private JPanel actionBar;
    private JLabel subtitle;
    private String defaultSubtitle;
    private boolean busy;

    private AiAssistant(JFrame frame, AssistantEngine engine, Runnable openBooking) {
        this.frame = frame;
        this.engine = engine;
        this.openBooking = openBooking;
    }

    public static void installForClient(JFrame frame, Runnable openBooking) {
        installForClient(frame, openBooking, Corner.BOTTOM_RIGHT);
    }

    public static void installForClient(JFrame frame, Runnable openBooking, Corner corner) {
        new AiAssistant(frame, AssistantEngine.forClient(), openBooking).install(corner);
    }

    public static void installForEmployee(
            JFrame frame,
            int employeeId,
            boolean technician,
            boolean trainer
    ) {
        installForEmployee(frame, employeeId, technician, trainer, Corner.BOTTOM_RIGHT);
    }

    public static void installForEmployee(
            JFrame frame,
            int employeeId,
            boolean technician,
            boolean trainer,
            Corner corner
    ) {
        new AiAssistant(
                frame,
                AssistantEngine.forEmployee(employeeId, technician, trainer),
                null
        ).install(corner);
    }

    private void install(Corner corner) {
        PillButton pill = new PillButton("Discută cu asistentul AI", PILL_WIDTH, PILL_HEIGHT, true);
        JLayeredPane layer = frame.getLayeredPane();

        layer.add(pill, JLayeredPane.PALETTE_LAYER);

        Runnable place = () -> {
            int x = corner == Corner.BOTTOM_LEFT
                    ? MARGIN
                    : Math.max(0, layer.getWidth() - PILL_WIDTH - MARGIN);

            int y = Math.max(0, layer.getHeight() - PILL_HEIGHT - MARGIN);

            pill.setBounds(x, y, PILL_WIDTH, PILL_HEIGHT);
        };

        layer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                place.run();
            }
        });

        pill.addActionListener(event -> toggleDialog());

        SwingUtilities.invokeLater(place);
    }

    private void toggleDialog() {
        if (dialog == null) {
            buildDialog();
        }

        if (dialog.isVisible()) {
            dialog.setVisible(false);
            return;
        }

        Point origin = frame.getLocationOnScreen();

        int x = origin.x + frame.getWidth() - dialog.getWidth() - MARGIN;
        int y = origin.y + frame.getHeight() - dialog.getHeight() - PILL_HEIGHT - MARGIN * 2;

        dialog.setLocation(Math.max(0, x), Math.max(0, y));
        dialog.setVisible(true);
        input.requestFocusInWindow();
    }

    private void buildDialog() {
        dialog = new JDialog(frame, "Asistent AI", false);
        dialog.setSize(430, 580);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(CREAM);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Asistent AI");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(INK);

        defaultSubtitle = AiConfig.enabled()
                ? "Conectat la serviciul AI."
                : "Mod de bază: răspunsurile vin din datele aplicației.";

        subtitle = new JLabel(defaultSubtitle);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        JPanel header = new JPanel(new BorderLayout(0, 2));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);

        chat = new JEditorPane();
        chat.setContentType("text/html");
        chat.setEditable(false);
        chat.setBackground(Color.WHITE);
        chat.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(chat);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));

        actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionBar.setOpaque(false);
        actionBar.setVisible(false);

        input = new JTextField();
        input.setPreferredSize(new Dimension(100, 38));
        input.putClientProperty("JTextField.placeholderText", "Scrie un mesaj...");
        input.addActionListener(event -> send());

        sendButton = new PillButton("Trimite", 90, 38, true);
        sendButton.addActionListener(event -> send());

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        inputRow.add(input, BorderLayout.CENTER);
        inputRow.add(sendButton, BorderLayout.EAST);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.add(actionBar, BorderLayout.NORTH);
        south.add(inputRow, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);

        dialog.setContentPane(root);

        appendBubble(false, engine.greeting());
    }

    private void send() {
        if (busy) {
            return;
        }

        String text = input.getText().trim();

        if (text.isEmpty()) {
            return;
        }

        input.setText("");
        appendBubble(true, text);

        actionBar.removeAll();
        actionBar.setVisible(false);

        setBusy(true);

        new SwingWorker<AssistantEngine.Reply, Void>() {

            @Override
            protected AssistantEngine.Reply doInBackground() throws Exception {
                return engine.handle(text);
            }

            @Override
            protected void done() {
                setBusy(false);

                try {
                    show(get());

                } catch (Exception exception) {
                    Throwable cause = exception.getCause() != null
                            ? exception.getCause()
                            : exception;

                    cause.printStackTrace();

                    appendBubble(false, "Nu am putut răspunde acum: " + friendly(cause));
                }
            }

        }.execute();
    }

    private void show(AssistantEngine.Reply reply) {
        appendBubble(false, reply.text());

        if (reply.offerBooking() && openBooking != null) {
            JButton go = new PillButton("Deschide programările", 210, 36, false);

            go.addActionListener(event -> {
                dialog.setVisible(false);
                openBooking.run();
            });

            actionBar.add(go);
            actionBar.setVisible(true);
            actionBar.revalidate();
            actionBar.repaint();
        }
    }

    private void setBusy(boolean value) {
        busy = value;

        input.setEnabled(!value);
        sendButton.setEnabled(!value);

        subtitle.setText(value ? "Se pregătește răspunsul..." : defaultSubtitle);

        if (!value) {
            input.requestFocusInWindow();
        }
    }

    private static String friendly(Throwable cause) {
        if (cause instanceof SQLException) {
            return "a apărut o eroare la citirea datelor din baza de date.";
        }

        String message = cause.getMessage();

        return message == null || message.isBlank()
                ? "a apărut o eroare neașteptată."
                : message;
    }

    private void appendBubble(boolean user, String text) {
        String background = user ? "#E7DDCD" : "#FAF9F7";
        String align = user ? "right" : "left";
        String label = user ? "Tu" : "Asistent";

        html.append("<table width='100%' cellpadding='0' cellspacing='0'>")
                .append("<tr><td align='").append(align).append("'>")
                .append("<font color='#736E67' size='2'>").append(label).append("</font>")
                .append("<table cellpadding='8' cellspacing='0' bgcolor='").append(background).append("'>")
                .append("<tr><td>").append(escape(text)).append("</td></tr>")
                .append("</table>")
                .append("</td></tr></table><br>");

        chat.setText("<html><body style='font-family:SansSerif;font-size:13px;color:#1E1E1E'>"
                + html + "</body></html>");

        chat.setCaretPosition(chat.getDocument().getLength());
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
    }

    /** Buton cu capete complet rotunde (formă de pastilă). */
    private static final class PillButton extends JButton {

        private final boolean dark;

        PillButton(String text, int width, int height, boolean dark) {
            super(text);

            this.dark = dark;

            setPreferredSize(new Dimension(width, height));
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setForeground(dark ? Color.WHITE : INK);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();

            try {
                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color fill;

                if (!isEnabled()) {
                    fill = new Color(200, 196, 190);
                } else if (dark) {
                    fill = getModel().isRollover() ? new Color(60, 60, 60) : INK;
                } else {
                    fill = getModel().isRollover() ? HOVER : BEIGE;
                }

                g.setColor(fill);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

                if (!dark) {
                    g.setColor(BORDER);
                    g.setStroke(new BasicStroke(1f));
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                }

            } finally {
                g.dispose();
            }

            super.paintComponent(graphics);
        }
    }
}
