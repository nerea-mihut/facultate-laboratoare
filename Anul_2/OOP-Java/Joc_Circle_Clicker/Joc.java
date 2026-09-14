import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Random;

public class Joc extends JFrame {
    private JPanel panel1;
    private JPanel mainPanel;
    private JLabel scorLabel;
    private JButton pauzaButton;
    private JButton salveazaJocButton;
    private JButton incarcaJocButton;

    private java.util.List<Cerc> listaCercuri = new java.util.concurrent.CopyOnWriteArrayList<>(); //această clasă face în fundal o copie completă a listei, face modificarea pe copie, iar apoi înlocuiește instant lista veche cu cea nouă
    private int scor = 0;
    private boolean esteInPauza = false; // flag, daca ii true blocheaza clicku si micsorarea
    private Random rand = new Random();

    public Joc() {
        setContentPane(panel1);
        setTitle("Circle Clicker");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        int numarCercuriInitiale = rand.nextInt(4) + 3; // genereaza un nr aleatoriu între 3 și 6
        for (int i = 0; i < numarCercuriInitiale; i++) {
            genereazaCercNou();
        }
        actualizeazaScorVizual();

        //logica click
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (esteInPauza) return;

                for (Cerc cercCurent : listaCercuri) {
                    if (cercCurent != null && cercCurent.continePunct(e.getX(), e.getY())) {// daca cursoru ii pe el si am dat click
                        scor += 1;
                        listaCercuri.remove(cercCurent);
                        genereazaCercNou();
                        break;
                    }
                }
                actualizeazaScorVizual();
                mainPanel.repaint(); //redeseneaza
            }
        });

        //butonul de pauza si start
        pauzaButton.addActionListener(e -> {
            esteInPauza = !esteInPauza;
            pauzaButton.setText(esteInPauza ? "Start" : "Pauza");
        });

        // serializare- salvarae buton
        salveazaJocButton.addActionListener(e -> {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("salvare_joc.dat"))) { //ObjectOutputStream ia datele din RAM si le scrie in fisiweu salvare_joc.dat
                oos.writeInt(scor);
                oos.writeObject(listaCercuri);
                JOptionPane.showMessageDialog(this, "Joc salvat cu succes!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Eroare la salvare: " + ex.getMessage());// stream-urile de fișiere se închid automat la final, prevenind pierderile de memorie (memory leaks), chiar dacă apare o eroare la scriere.
            }
        });

        // deserializare

        incarcaJocButton.addActionListener(e -> {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("salvare_joc.dat"))) {
                scor = ois.readInt(); //citește primul calup de bytes din fisier, îl transf înapoi în nr intreg si suprascrie scor
                listaCercuri = (java.util.List<Cerc>) ois.readObject(); //read object() returneaza un ob Object, noi ii zicem stiu ca i cerc in fisier si ti zic si tie (cerc)

                while (listaCercuri.size() < 3) {
                    genereazaCercNou();
                }

                actualizeazaScorVizual(); // schimba scoru
                mainPanel.repaint(); // redeseneaza ce o fost salvat in ram
                JOptionPane.showMessageDialog(this, "Joc încărcat!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Nu există salvare ");
            }// ia fisierul generat, descifreaza si reconstruieste obiectele in RAM exact in starea in care le am salvat
        });

        //pornesc Thread-ul
        new Thread(this::buclaJoc).start(); // REFERINTA LA METODA!!!    dau o referinta directa catre buclaJoc si creeaza compilatorul in fundal runnable anonim
    }

    private void buclaJoc() {
        while (true) {
            try {
                if (!esteInPauza) { // daca nu in pauza fa ce i aici daca ii pe pauza sari peste
                    for (Cerc cercCurent : listaCercuri) {
                        if (cercCurent != null) {
                            cercCurent.micsoreaza();

                            if (cercCurent.dimensiune <= 2) {
                                scor -= 1;
                                listaCercuri.remove(cercCurent);
                                genereazaCercNou();
                                actualizeazaScorVizual();
                            }
                        }
                    }
                    // repaintul il dau pe mainpanel nu pe toata fereastra

                    if (mainPanel != null) {
                        mainPanel.repaint();
                    }
                }
                Thread.sleep(80); // 80 ms pentru un ritm mai lent
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void genereazaCercNou() {
        // iau dim panelului daca e prea devreme si sunt 0, punem valori implicite fixe
        int lMax = (mainPanel != null && mainPanel.getWidth() > 0) ? mainPanel.getWidth() : 500; // ii mainPanel initializat si are o latime >0 daca da o fol pe aia daca nu pune val astea implicite
        int hMax = (mainPanel != null && mainPanel.getHeight() > 0) ? mainPanel.getHeight() : 400;

        int dim = rand.nextInt(136) + 15; // genereaza random cercuri de la 15p la 150p dimensiune
        int randomX = rand.nextInt(Math.max(10, lMax - dim)); // ca sa nu iasa cercul din ecran sau sa se faca pe colturi si sa nu l vewdem
        int randomY = rand.nextInt(Math.max(10, hMax - dim)); //Math.max ii ca sa nu gebereze numere negative
        int randomViteza;
        if (dim < 50) {
            randomViteza = 1;
        } else {
            randomViteza = rand.nextInt(2) + 1; // MODIFICAT AICI: viteza va fi doar 1 sau 2, eliminând viteza 3 care era prea rapidă
        }
        Color randomCuloare = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)); //generez culoarile

        listaCercuri.add(new Cerc(randomX, randomY, dim, randomViteza, randomCuloare)); // toate chestiile alese random is trimise catre constructor, cand vechiul cerc ii nimerit ii eliminat din cercCurent si noy cerc ii ia locu in memorie
    }

    private void actualizeazaScorVizual() {
        if (scorLabel != null) {
            scorLabel.setText("Scor: " + scor);
        }
    }

    private void createUIComponents() {
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // curata automat panelului

                // Dacă avem un cerc activ în memorie, îl pictăm pe panel
                for (Cerc cercCurent : listaCercuri) {
                    if (cercCurent != null) {
                        g.setColor(cercCurent.culoare);
                        g.fillOval(cercCurent.x, cercCurent.y, cercCurent.dimensiune, cercCurent.dimensiune);
                    }
                }
            }
        };
    }
}