import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StudentModel {
    // Returnează o listă de String-uri citite din fișier
    public List<String> citesteStudenti() {
        List<String> studenti = new ArrayList<>();
        // Folosim o cale relativă standard; fișierul va sta în rădăcina proiectului
        File fisier = new File("persoane.txt");

        if (fisier.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fisier))) {
                String linie;
                while ((linie = br.readLine()) != null) {
                    if (!linie.trim().isEmpty()) {
                        studenti.add(linie.trim());
                    }
                }
            } catch (Exception e) {
                System.err.println("Eroare la citirea fisierului: " + e.getMessage());
            }
        } else {
            System.out.println("Fisierul persoane.txt nu a fost gasit la: " + fisier.getAbsolutePath());
        }
        return studenti;
    }
}