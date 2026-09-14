import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Panou8 {
    private JPanel mainPanel;
    private JPanel panel1;
    private JButton susButton;
    private JButton josButton;
    private JButton stangaButton;
    private JButton dreaptaButton;
    private JRadioButton cercRadioButton;
    private JRadioButton patratRadioButton;
    private JRadioButton rosuRadioButton;
    private JRadioButton verdeRadioButton;
    private JRadioButton albastruRadioButton;

    private ArrayList<Forma> listaForme = new ArrayList<>();
    private Forma formaSelectata = null;

    // Această metodă este cheia! Ea creează zona de desenat
    private void createUIComponents() {
        panel1 = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Desenăm fundalul alb explicit pentru zona de desen
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());

                for (Forma f : listaForme) {
                    f.draw(g);
                }
            }
        };
    }

    public Panou8() {

        susButton.addActionListener(e -> misca(0, -10));
        josButton.addActionListener(e -> misca(0, 10));
        stangaButton.addActionListener(e -> misca(-10, 0));
        dreaptaButton.addActionListener(e -> misca(10, 0));

        rosuRadioButton.addActionListener(e -> schimbaCuloare(Color.RED));
        verdeRadioButton.addActionListener(e -> schimbaCuloare(Color.GREEN));
        albastruRadioButton.addActionListener(e -> schimbaCuloare(Color.BLUE));

        ButtonGroup grupForme = new ButtonGroup();
        grupForme.add(cercRadioButton);
        grupForme.add(patratRadioButton);

        ButtonGroup grupCulori = new ButtonGroup();
        grupCulori.add(rosuRadioButton);
        grupCulori.add(verdeRadioButton);
        grupCulori.add(albastruRadioButton);

        cercRadioButton.setSelected(true);
        rosuRadioButton.setSelected(true);

        panel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Aflăm culoarea EXACTĂ care e bifată acum
                Color c = Color.RED; // default
                if (verdeRadioButton.isSelected()) c = Color.GREEN;
                else if (albastruRadioButton.isSelected()) c = Color.BLUE;

                //Deselectăm restul
                for (Forma f : listaForme) f.setSelectat(false);

                // 3. Creăm obiectul
                if (cercRadioButton.isSelected()) {
                    formaSelectata = new Cerc(e.getX(), e.getY(), c);
                } else {
                    formaSelectata = new Patrat(e.getX(), e.getY(), c);
                }

                formaSelectata.setSelectat(true);
                listaForme.add(formaSelectata);

                panel1.repaint();
            }
        });
    }

    private void misca(int dx, int dy) {
        if (formaSelectata != null) {
            formaSelectata.muta(dx, dy);
            panel1.repaint();
        }
    }

    private void schimbaCuloare(Color c) {
        if (formaSelectata != null) {
            formaSelectata.setCuloare(c);
            panel1.repaint();
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}


// am desenat in paintComponent Pentru că în Swing, desenarea este tranzitorie.
// Dacă minimizăm fereastra sau o redimensionăm, tot ce am desenat manual dispare.
// Folosind paintComponent și o listă de obiecte, ne asigurăm că Java știe să redeseneze
// totul corect de fiecare dată când este nevoie.