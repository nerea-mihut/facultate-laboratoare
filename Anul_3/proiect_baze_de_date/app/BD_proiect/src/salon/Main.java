package salon;

import com.formdev.flatlaf.FlatLightLaf;
import salon.ui.LoginFrame;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();

            UIManager.put("defaultFont",
                    new Font("SansSerif", Font.PLAIN, 14));

            UIManager.put("Button.arc", 16);
            UIManager.put("Component.arc", 16);
            UIManager.put("TextComponent.arc", 16);

            UIManager.put("Component.focusColor",
                    new Color(160, 145, 120));

            UIManager.put("Component.focusedBorderColor",
                    new Color(160, 145, 120));

            new LoginFrame();
        });
    }
}