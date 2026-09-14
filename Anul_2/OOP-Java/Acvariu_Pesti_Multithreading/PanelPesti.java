import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// 1. Clasa care se ocupă STRICT de desenarea peștilor și mișcarea lor (Un JPanel normal)
class PanelPesti extends JPanel {
    private List<PesteMecanica> pesti = new ArrayList<>();

    public PanelPesti() {
        setBackground(new Color(20, 130, 200)); // Fundal albastru
    }

    public void pornesteAnimatia() {
        pesti.clear();
        // Creăm cei 5 pești pe înălțimi diferite
        for (int i = 0; i < 5; i++) {
            PesteMecanica p = new PesteMecanica(50 + (i * 70), this);
            pesti.add(p);

            // Pornim Thread-ul dedicat fiecărui pește
            Thread t = new Thread(p);
            t.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Desenează fundalul albastru

        // Desenăm câteva bule decorative
        g.setColor(new Color(255, 255, 255, 60));
        g.fillOval(100, 150, 15, 15);
        g.fillOval(400, 250, 20, 20);

        // Desenăm peștii
        for (PesteMecanica p : pesti) {
            p.deseneaza(g);
        }
    }
}