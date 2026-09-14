import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Joc j = new Joc();
            j.setVisible(true);
        });
    }
}