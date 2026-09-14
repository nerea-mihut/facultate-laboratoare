import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class Sparkle {
    public double x, y;
    public double speedX, speedY;
    public int life;
    public Color color;
    public int radius;
    public String shape;

    public Sparkle(double startX, double startY, Color c, String shape) {
        this.x = startX;
        this.y = startY;
        this.color = c;
        this.shape = shape;

        Random r = new Random();

        // Explozie în toate direcțiile
        this.speedX = r.nextDouble() * 14 - 7;
        this.speedY = r.nextDouble() * 14 - 7;

        // durata de viata intre 20-40 de cadre
        this.life = 20 + r.nextInt(20);

        // Raza formei: între 4 și 8 pixeli
        this.radius = 4 + r.nextInt(5);
    }

    public void update() {
        x += speedX;
        y += speedY;
        speedY += 0.2; // Micro-gravitație
        life--;
    }

    public void draw(Graphics g) {
        if (life <= 0) return;

        int drawX = (int) x;
        int drawY = (int) y;

        if (shape.equals("coin")) {
            // desenare banuti aurii
            g.setColor(new Color(255, 215, 0)); // Auriu curat
            g.fillOval(drawX - radius /* scad raza din centru coord in coltu stanga sus*/, drawY - radius, radius * 2, radius * 2);

            g.setColor(new Color(218, 165, 32)); // Margine auriu-închis
            g.drawOval(drawX - radius, drawY - radius, radius * 2, radius * 2);

            // O mică linie pe mijloc ca să pară o monedă detaliată
            g.drawLine(drawX, drawY - radius + 2, drawX, drawY + radius - 2);
        }
        else if (shape.equals("xmas")) {
            //fulgii de nea
            g.setColor(color);
            g.drawLine(drawX - radius, drawY, drawX + radius, drawY); // Orizontal
            g.drawLine(drawX, drawY - radius, drawX, drawY + radius); // Vertical
            g.drawLine(drawX - radius + 2, drawY - radius + 2, drawX + radius - 2, drawY + radius - 2); // Diagonală 1
            g.drawLine(drawX - radius + 2, drawY + radius - 2, drawX + radius - 2, drawY - radius + 2); // Diagonală 2
        }
        else {
            //5stelute
            g.setColor(color);
            int[] xPoints = new int[10]; //pct pe care le unim ca sa fac steaua
            int[] yPoints = new int[10];

            double angle = Math.PI / 2; // Începem de la vârful de sus
            double step = Math.PI / 5;  // Distanța dintre vârfuri

            for (int i = 0; i < 10; i++) {
                // Alternăm între un punct exterior și un punct interior pentru a crea colțurile
                double r = (i % 2 == 0) ? radius : radius / 2.0;
                xPoints[i] = drawX + (int) (Math.cos(angle) * r);
                yPoints[i] = drawY - (int) (Math.sin(angle) * r);
                angle -= step; // Rotim unghiul pentru următorul punct
            }
            g.fillPolygon(xPoints, yPoints, 10);
        }
    }
}