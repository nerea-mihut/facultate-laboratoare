import javax.swing.table.DefaultTableModel;

public class TabelStudentiModel {
    private DefaultTableModel tableModel; //stochează în memorie o matrice de obiecte (liniile) și un vector de string-uri (header-ul coloanelor)

    public TabelStudentiModel() {
        // Definim coloanele tabelului conform cerințelor din laborator
        String[] coloane = {"Nume", "Prenume", "An", "Nota"};
        // Inițializăm modelul de tabel cu 0 rânduri la pornire
        tableModel = new DefaultTableModel(coloane, 0);
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    // Metodă prin care adăugăm un rând nou în structura de date
    public void adaugaStudentInModel(String nume, String prenume, int an, double nota) {
        // În Swing, adăugăm rândul ca un tablou de obiecte
        tableModel.addRow(new Object[]{nume, prenume, an, nota});
    }
}