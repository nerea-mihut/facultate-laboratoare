import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Acvariu Pesti Animat");

                Acvariu ecranAcvariu = new Acvariu();

                frame.setContentPane(ecranAcvariu.panel1);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(650, 480);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // Pornim peștii după ce totul e afișat corect
                ecranAcvariu.pornesteAcvariul();
            }
        });
    }
}