import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Monitorizare Parcare Supermarket");

                PanelParcare interfataParcare = new PanelParcare();

                // Încărcăm panel1 (root-ul din interfața ta)
                frame.setContentPane(interfataParcare.panel1);

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(750, 420);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // Pornim thread-ul de monitorizare din fundal
                interfataParcare.pornesteMonitorizarea();
            }
        });
    }
}