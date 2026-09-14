import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;

public class TabelStudentiView extends JFrame {
    private JTable tabel;
    // Câmpurile din formularul de introducere
    private JTextField txtNume, txtPrenume, txtAn, txtNota;
    private JButton btnAdauga;

    public TabelStudentiView(DefaultTableModel tableModel) {
        setTitle("Formular Student - JTable MVC");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Creăm tabelul grafic și îi dăm modelul de date primit
        tabel = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tabel);

        // 2. Creăm elementele formularului
        txtNume = new JTextField(8);
        txtPrenume = new JTextField(8);
        txtAn = new JTextField(3);
        txtNota = new JTextField(3);
        btnAdauga = new JButton("Adaugă");

        // 3. Aranjăm formularul într-un panou (FlowLayout le pune în linie)
        JPanel panouFormular = new JPanel();
        panouFormular.add(new JLabel("Nume:"));
        panouFormular.add(txtNume);
        panouFormular.add(new JLabel("Prenume:"));
        panouFormular.add(txtPrenume);
        panouFormular.add(new JLabel("An:"));
        panouFormular.add(txtAn);
        panouFormular.add(new JLabel("Notă:"));
        panouFormular.add(txtNota);
        panouFormular.add(btnAdauga);

        // 4. Punem tabelul în centru și formularul jos
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(panouFormular, BorderLayout.SOUTH);
    }

    // Metode pentru a lua textul introdus de utilizator
    public String getNume() { return txtNume.getText().trim(); }
    public String getPrenume() { return txtPrenume.getText().trim(); }
    public String getAnRaw() { return txtAn.getText().trim(); }
    public String getNotaRaw() { return txtNota.getText().trim(); }

    // Curăță câmpurile după adăugare, ca să poți scrie următorul student
    public void curataFormular() {
        txtNume.setText("");
        txtPrenume.setText("");
        txtAn.setText("");
        txtNota.setText("");
    }

    // IMPORTANT: Permite Controller-ului să asculte când se dă click pe buton
    public void adaugaAscultatorButon(ActionListener ascultator) {
        btnAdauga.addActionListener(ascultator);
    }
}