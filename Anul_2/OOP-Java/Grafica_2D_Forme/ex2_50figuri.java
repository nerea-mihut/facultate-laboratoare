import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ex2_50figuri extends JFrame {
    private JPanel myPanel;
    private List<Mark> listaFiguri = new ArrayList<>();

    public ex2_50figuri() {
        for (int i = 0; i < 50; i++) {
            listaFiguri.add(new Mark());
        }

        setTitle("50 figuri");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        myPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (Mark m : listaFiguri) {
                    g.setColor(m.c);
                    g.drawOval(m.x, m.y, m.w, m.h);
                }
            }
        };

        add(myPanel);
        setVisible(true);
    }

    public static void main(String[] args) {
        new ex2_50figuri();
    }
}