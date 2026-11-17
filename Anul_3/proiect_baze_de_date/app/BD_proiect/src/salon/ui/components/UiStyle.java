package salon.ui.components;

import javax.swing.*;
import java.awt.*;

public final class UiStyle {

    public static final Color CREAM = new Color(244, 240, 233);
    public static final Color INK = new Color(30, 30, 30);
    public static final Color MUTED = new Color(115, 110, 103);
    public static final Color WHITE = Color.WHITE;
    public static final Color FIELD = new Color(250, 249, 247);
    public static final Color BORDER = new Color(225, 220, 213);
    public static final Color BEIGE = new Color(231, 221, 205);
    public static final Color GREEN = new Color(88, 150, 105);
    public static final Color RED = new Color(160, 45, 45);

    private UiStyle() {
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(INK);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.putClientProperty("JButton.arc", 18);
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setBackground(FIELD);
        button.setForeground(INK);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.putClientProperty("JButton.arc", 18);
        return button;
    }

    public static JLabel label(
            String text,
            int size,
            boolean bold,
            Color color
    ) {
        JLabel label = new JLabel(text);
        label.setFont(
                new Font(
                        "SansSerif",
                        bold ? Font.BOLD : Font.PLAIN,
                        size
                )
        );
        label.setForeground(color);
        return label;
    }

    public static void styleField(JTextField field) {
        field.setPreferredSize(new Dimension(340, 42));
        field.setBackground(FIELD);
        field.setForeground(INK);
        field.setCaretColor(INK);
        field.setMargin(new Insets(8, 12, 8, 12));
    }
}