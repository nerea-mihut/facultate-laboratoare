import javax.swing.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Lab6 {
    private JTextField numeTextField;
    private JPanel panel1;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JCheckBox bursierCheckBox;
    private JRadioButton caminRadioButton;
    private JRadioButton chirieRadioButton;
    private JRadioButton domiciliuRadioButton;
    private JRadioButton mRadioButton;
    private JRadioButton fRadioButton;
    private JButton adaugaButton;
    private JButton cautaButton;

    private ArrayList<Student> listaDate = new ArrayList<>();

    public Lab6() {
        JFrame frame = new JFrame("Laborator 6");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ButtonGroup grupLocuinta = new ButtonGroup();
        grupLocuinta.add(caminRadioButton);
        grupLocuinta.add(chirieRadioButton);
        grupLocuinta.add(domiciliuRadioButton);

        ButtonGroup grupGen = new ButtonGroup();
        grupGen.add(mRadioButton);
        grupGen.add(fRadioButton);

        adaugaButton.addActionListener(e -> {
            try {
                // Preluăm datele din interfață
                String numeStr = textField1.getText();
                String prenumeStr = textField2.getText();
                // Conversie text -> LocalDate (ex: "2002-10-25")
                LocalDate dataN = LocalDate.parse(textField3.getText());
                boolean esteBursier = bursierCheckBox.isSelected();

                // Identificăm locuința
                String loc = "";
                if (caminRadioButton.isSelected()) loc = "Camin";
                else if (chirieRadioButton.isSelected()) loc = "Chirie";
                else if (domiciliuRadioButton.isSelected()) loc = "Domiciliu";

                // Identificăm genul
                String gen = mRadioButton.isSelected() ? "Masculin" : "Feminin";

                // Creăm obiectul Student (folosind constructorul tău)
                Student nou = new Student(numeStr, prenumeStr, dataN, esteBursier, loc, gen);

                // Îl adăugăm în ArrayList
                listaDate.add(nou);

                JOptionPane.showMessageDialog(frame, "Studentul " + numeStr + " a fost salvat!");

                // Curățăm câmpurile pentru o nouă introducere
                textField1.setText("");
                textField2.setText("");
                textField3.setText("");
                grupLocuinta.clearSelection();
                grupGen.clearSelection();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Eroare! Introdu data în formatul: AAAA-LL-ZZ\nExemplu: 2004-05-20");
            }
        });

        // 4. Acțiunea pentru butonul CAUTA
        cautaButton.addActionListener(e -> {
            String numeCautat = textField1.getText();
            boolean gasit = false;

            // Parcurgem lista să vedem dacă există numele
            for (Student s : listaDate) {
                if (s.Nume.equalsIgnoreCase(numeCautat)) {
                    String info = "Elev găsit:\n" +
                            "Nume: " + s.Nume + " " + s.Prenume + "\n" +
                            "Data: " + s.DataNastere + "\n" +
                            "Locuiește la: " + s.Locuinta + "\n" +
                            "Bursier: " + (s.Bursier ? "Da" : "No");
                    JOptionPane.showMessageDialog(frame, info);
                    gasit = true;
                    break;
                }
            }
            if (!gasit) {
                JOptionPane.showMessageDialog(frame, "Nu s-a găsit niciun student cu numele: " + numeCautat);
            }
        });

        frame.pack();
        frame.setVisible(true);
    }

    // Metoda main care pornește totul
    public static void main(String[] args) {
        new Lab6();
    }
}