package salon.ui.components;

import javax.swing.*;
import java.awt.*;

public class RoundedCard extends JPanel {

    public RoundedCard() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics.create();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setColor(UiStyle.WHITE);
        g.fillRoundRect(
                0,
                0,
                getWidth(),
                getHeight(),
                28,
                28
        );

        g.dispose();
    }
}