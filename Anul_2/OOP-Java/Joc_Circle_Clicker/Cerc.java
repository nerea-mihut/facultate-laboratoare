import java.awt.Color;
import java.io.Serializable;

public class Cerc implements Serializable {
    private static final long serialVersionUID = 1L;

    public int x, y, dimensiune, viteza;
    public Color culoare;

    public Cerc(int x, int y, int dimensiune, int viteza, Color culoare) {
        this.x = x;
        this.y = y;
        this.dimensiune = dimensiune;
        this.viteza = viteza;
        this.culoare = culoare;
    }

    public void micsoreaza() {
        this.dimensiune -= this.viteza;
    }

    //verific daca clickul ii in mij cercului
    public boolean continePunct(int mx, int my) {
        int raza = dimensiune / 2;
        int centruX = x + raza;
        int centruY = y + raza;
        // Distanța dintre click și centrul cercului la pătrat
        double distantaPatrat = Math.pow(mx - centruX, 2) + Math.pow(my - centruY, 2);
        return distantaPatrat <= Math.pow(raza, 2);
    }
}