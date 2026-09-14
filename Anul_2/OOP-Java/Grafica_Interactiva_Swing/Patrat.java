import java.awt.*;

public class Patrat extends Forma {
    public Patrat(int x, int y, Color culoare) {
        super(x, y, culoare);
    }

    @Override
    public void draw(Graphics g) {

        if(esteSelectat) {
            g.setColor(Color.BLACK);
        }else {
            g.setColor(culoare);
        }
        g.drawRect(x - 51, y - 51, 102, 102);
    }
}
