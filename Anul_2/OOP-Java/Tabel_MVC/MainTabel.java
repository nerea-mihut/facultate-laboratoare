import javax.swing.SwingUtilities;

public class MainTabel {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TabelStudentiModel model = new TabelStudentiModel();
            // Îi pasăm view-ului modelul de tabel, ca să știe cum să se deseneze din start
            TabelStudentiView view = new TabelStudentiView(model.getTableModel());
            TabelStudentiController controller = new TabelStudentiController(model, view);

            view.setVisible(true);
        });
    }
}