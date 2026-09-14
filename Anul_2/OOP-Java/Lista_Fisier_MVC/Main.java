import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Instanțiem cele 3 componente ale modelului arhitectural
            StudentModel model = new StudentModel();
            StudentView view = new StudentView();
            StudentController controller = new StudentController(model, view);

            // Încărcăm datele prin intermediul controller-ului [cite: 22]
            controller.pornesteAplicatie();

            // Facem fereastra vizibilă
            view.setVisible(true);
        });
    }
}

