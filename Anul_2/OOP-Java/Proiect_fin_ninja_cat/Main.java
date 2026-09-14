import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Pornim aplicația pe firul de execuție grafic (Swing)
        SwingUtilities.invokeLater(() -> {
            CatNinja joc = new CatNinja();
            joc.setVisible(true);
        });
    }
}