import javax.swing.*;
import java.awt.*;

public class Grafic extends JFrame {
    private JPanel myPanel;

    public Grafic() {
        setTitle("Grafic");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setContentPane(myPanel);
        setVisible(true);
    }


    private void createUIComponents() {
        System.out.println("Creating UI Components");
        myPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                //cerc
                g.setColor(Color.RED);
                g.drawOval(20  , 20, 100, 100);
                //patrat
                g.setColor(Color.BLUE);
                g.drawRect(150, 50, 100, 100);
                //linie
                g.setColor(Color.GREEN);
                g.drawLine(170, 170, 350, 170);
                //triunghi
                g.setColor(Color.ORANGE);
                int[] xT = {50, 100, 20};
                int[] yT = {200, 300, 300};
                g.drawPolygon(xT, yT, 3);

                ImageIcon img = new ImageIcon("IMG_2451.jpg");
                g.drawImage(img.getImage(), 200, 200, 100, 100, this);
            }
        };
    }
}