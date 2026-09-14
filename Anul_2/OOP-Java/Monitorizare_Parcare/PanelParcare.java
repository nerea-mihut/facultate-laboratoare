import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PanelParcare {
    // Componentele din .form - IntelliJ le va recunoaște instant acum
    public JPanel panel1;
    public JPanel mainPanel;

    private DesenParcare panouDesen;

    public PanelParcare() {
        // Creăm zona în care se vor picta mașinile
        panouDesen = new DesenParcare();

        // Configurăm mainPanel să ocupe tot spațiul și adăugăm desenul în el
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(panouDesen, BorderLayout.CENTER);
    }

    public void pornesteMonitorizarea() {
        // Pentru că folderul de proiect e corect, punem doar numele fișierului.
        // Java îl va căuta exact în rădăcina folderului poo_java!
        String caleFisier = "parcare.txt";

        // Pornim Thread-ul cerut de problemă care monitorizează fișierul text
        MonitorFisier monitor = new MonitorFisier(caleFisier, panouDesen);
        Thread firMonitorizare = new Thread(monitor);
        firMonitorizare.start();
    }
}

// Componenta grafică ce desenează cele 10 loturi de parcare
class DesenParcare extends JPanel {
    private Color[] locuri = new Color[10];

    public DesenParcare() {
        setBackground(new Color(230, 230, 235)); // Culoare gri asfalt
    }

    // Metodă sincronizată (Thread-safe) pentru a vopsi locul ocupat de o mașină
    public synchronized void vopsesteLoc(int loc, Color culoareJava) {
        if (loc >= 0 && loc < 10) {
            locuri[loc] = culoareJava;
            repaint(); // Îi cerem interfeței Swing să se redeseneze
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int latimeLoc = getWidth() / 5;   // 5 locuri pe rând
        int inaltimeLoc = getHeight() / 2; // 2 rânduri (total 10)

        for (int i = 0; i < 10; i++) {
            int rand = i / 5;
            int coloana = i % 5;
            int x = coloana * latimeLoc;
            int y = rand * inaltimeLoc;

            // Desenăm liniile albe ale lotului
            g.setColor(Color.WHITE);
            g.drawRect(x + 5, y + 5, latimeLoc - 10, inaltimeLoc - 10);

            // Scriem numărul lotului
            g.setColor(Color.DARK_GRAY);
            g.drawString("Loc " + i, x + 15, y + 25);

            // Dacă este o mașină parcată (culoarea nu e null), o desenăm
            if (locuri[i] != null) {
                g.setColor(locuri[i]);
                g.fillRect(x + 20, y + 45, latimeLoc - 40, inaltimeLoc - 70);

                // Faruri galbene mici
                g.setColor(Color.YELLOW);
                g.fillRect(x + latimeLoc - 30, y + 55, 6, 6);
                g.fillRect(x + latimeLoc - 30, y + inaltimeLoc - 40, 6, 6);
            }
        }
    }
}

// Clasa Runnable care rulează pe un Fir de Execuție separat pentru a pândi fișierul text
class MonitorFisier implements Runnable {
    private File fisier;
    private DesenParcare panouDesen;
    private long ultimaModificare = 0;

    public MonitorFisier(String caleFisier, DesenParcare panouDesen) {
        this.fisier = new File(caleFisier);
        this.panouDesen = panouDesen;

        // Dacă fișierul nu există în proiect, îl creăm automat
        if (!fisier.exists()) {
            try {
                fisier.createNewFile();
            } catch (IOException e) {
                System.out.println("Nu s-a putut crea fișierul parcare.txt în proiect.");
            }
        }
        this.ultimaModificare = fisier.lastModified();
    }

    @Override
    public void run() {
        while (true) {
            // Logica din spate a monitorizării: dacă data ultimei modificări s-a schimbat, citim din nou!
            if (fisier.lastModified() > ultimaModificare) {
                ultimaModificare = fisier.lastModified();
                citesteFisier();
            }
            try {
                Thread.sleep(1000); // Firul face o pauză de o secundă, apoi verifică iar
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void citesteFisier() {
        try (BufferedReader br = new BufferedReader(new FileReader(fisier))) {
            String linie;
            while ((linie = br.readLine()) != null) {
                // Împărțim linia după virgulă: culoare, lot (Ex: rosu, 4)
                String[] bucati = linie.split(",");
                if (bucati.length == 2) {
                    String numeCuloare = bucati[0].trim();
                    try {
                        int loc = Integer.parseInt(bucati[1].trim());
                        Color culoareJava = transformCuloare(numeCuloare);

                        // Trimitem comanda către parcare prin metoda sincronizată
                        panouDesen.vopsesteLoc(loc, culoareJava);
                    } catch (NumberFormatException e) {
                        // Ignorăm erorile de scriere din fișier
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Eroare la citirea fișierului text.");
        }
    }

    private Color transformCuloare(String numeCuloare) {
        switch (numeCuloare.toLowerCase()) {
            case "rosu": return Color.RED;
            case "albastru": return Color.BLUE;
            case "verde": return Color.GREEN;
            case "galben": return Color.YELLOW;
            default: return Color.ORANGE;
        }
    }
}