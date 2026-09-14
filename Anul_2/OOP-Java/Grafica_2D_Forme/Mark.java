import java.awt.*;
import java.util.Random;

public class Mark {
    Color c;
    int x, y;
    int w = 30, h = 20;
    Random generator = new Random();

    public Mark() {
        x = generator.nextInt(350);
        y = generator.nextInt(200);
        float r = generator.nextFloat();
        float g = generator.nextFloat();
        float b = generator.nextFloat();
        c = new Color(r, g, b);
    }

    public Mark(int a, int b) {
        x = a; y = b;
        c = new Color(255, 0, 0);
        w = 200; h = 400;
    }
}