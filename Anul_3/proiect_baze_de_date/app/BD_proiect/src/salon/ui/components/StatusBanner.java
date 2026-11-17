package salon.ui.components;

import javax.swing.*;
import java.awt.*;

public class StatusBanner extends JLabel {

    public StatusBanner() {
        setFont(new Font("SansSerif", Font.PLAIN, 12));
        setForeground(UiStyle.MUTED);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(
                BorderFactory.createEmptyBorder(8, 0, 8, 0)
        );
    }

    public void showSuccess(String message) {
        setForeground(UiStyle.GREEN);
        setText(message);
    }

    public void showError(String message) {
        setForeground(UiStyle.RED);
        setText(message);
    }

    public void clear() {
        setText(" ");
    }
}