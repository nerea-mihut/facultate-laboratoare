import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StudentView extends JFrame {
    private JList<String> jList1; // Componenta vizuală de tip listă [cite: 23]
    private DefaultListModel<String> listModel; // Modelul de date din spatele listei Swing [cite: 49]

    public StudentView() {
        setTitle("Lista Studenti - Laborator MVC");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inițializăm modelele vizuale conform specificațiilor Swing [cite: 49]
        listModel = new DefaultListModel<>();
        jList1 = new JList<>(listModel); // Conectăm lista de modelul ei [cite: 52]

        // Adăugăm un scrollbar în caz că avem mulți studenți în fișier
        JScrollPane scrollPane = new JScrollPane(jList1);

        // Configurăm un layout simplu și curat
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    // Metodă prin care Controller-ul poate încărca datele în interfață [cite: 6]
    public void afiseazaStudenti(List<String> studenti) {
        listModel.clear(); // Curățăm lista existentă [cite: 48]
        for (String student : studenti) {
            listModel.addElement(student); // Adăugăm elementele primite [cite: 51]
        }
    }
}