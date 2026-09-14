import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Ne asigurăm că interfața arată bine pe Windows/Mac
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Program Desenare");
            Panou8 app = new Panou8();

            if (app.getMainPanel() == null) {
                JOptionPane.showMessageDialog(null,
                        "Eroare Critica: mainPanel este null!\n" +
                                "Verifica field name in Designer si Rebuild Project.");
            } else {
                frame.setContentPane(app.getMainPanel());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}