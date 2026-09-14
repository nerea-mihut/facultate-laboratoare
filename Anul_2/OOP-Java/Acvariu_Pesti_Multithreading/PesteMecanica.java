import javax.swing.*;
import java.awt.*;
import java.util.Random;
// 2. Clasa pentru logica de mișcare a fiecărui pește (Thread)

class PesteMecanica implements Runnable {
    private int x, y;
    private int viteza;
    private int directie = 1;
    private Color culoare;
    private final int DIMENSIUNE = 30;
    private JPanel panou;

    public PesteMecanica(int startY, JPanel panou) {
        Random rand = new Random();
        this.x = rand.nextInt(200);
        this.y = startY;
        this.viteza = rand.nextInt(4) + 2;
        this.culoare = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
        this.panou = panou;
    }

    public void deseneaza(Graphics g) {
        g.setColor(culoare);
        g.fillOval(x, y, DIMENSIUNE, DIMENSIUNE - 10);

        int[] coadaX = new int[3];
        int[] coadaY = {y + (DIMENSIUNE-10)/2, y, y + DIMENSIUNE - 10};

        if (directie == 1) {
            coadaX[0] = x; coadaX[1] = x - 15; coadaX[2] = x - 15;
        } else {
            coadaX[0] = x + DIMENSIUNE; coadaX[1] = x + DIMENSIUNE + 15; coadaX[2] = x + DIMENSIUNE + 15;
        }
        g.fillPolygon(coadaX, coadaY, 3);
    }

    @Override
    public void run() {
        while (true) {
            x += viteza * directie;

            if (panou.getWidth() > 0) {
                if (x + DIMENSIUNE + 15 >= panou.getWidth()) {
                    directie = -1;
                    x = panou.getWidth() - DIMENSIUNE - 15;
                } else if (x <= 15) {
                    directie = 1;
                    x = 15;
                }
                panou.repaint();
            }

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
