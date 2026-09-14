import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

public class TabelStudentiController {
    private TabelStudentiModel model;
    private TabelStudentiView view;

    public TabelStudentiController(TabelStudentiModel model, TabelStudentiView view) {
        this.model = model;
        this.view = view;

        // Îi spunem View-ului că această clasă va gestiona click-ul pe buton
        this.view.adaugaAscultatorButon(new AscultatorButonAdauga());
    }

    // Clasă internă privată care implementează ActionListener (Ascultătorul de evenimente)
    private class AscultatorButonAdauga implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // 1. Preluăm datele din View
                String nume = view.getNume();
                String prenume = view.getPrenume();

                // Validare simplă: să nu fie goale text-box-urile
                if (nume.isEmpty() || prenume.isEmpty()) {
                    JOptionPane.showMessageDialog(view, "Numele și Prenumele nu pot fi goale!");
                    return;
                }

                // 2. Convertim datele și aplicăm logica/validarea (An 1-4, Notă 1-10)
                int an = Integer.parseInt(view.getAnRaw());
                double nota = Double.parseDouble(view.getNotaRaw());

                if (an < 1 || an > 4 || nota < 1.0 || nota > 10.0) {
                    JOptionPane.showMessageDialog(view, "Date invalide! Anul trebuie să fie 1-4, iar nota 1-10.");
                    return;
                }

                // 3. Trimitem datele validate către Model pentru a actualiza tabelul
                model.adaugaStudentInModel(nume, prenume, an, nota);

                // 4. Curățăm formularul vizual pentru următoarea introducere
                view.curataFormular();

            } catch (NumberFormatException ex) {
                // Prindem eroarea dacă utilizatorul scrie litere în loc de cifre la An sau Notă
                JOptionPane.showMessageDialog(view, "Introdu numere valide la An și Notă!");
            }
        }
    }
}