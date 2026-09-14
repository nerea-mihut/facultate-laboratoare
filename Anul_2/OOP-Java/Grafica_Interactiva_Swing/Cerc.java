import java.awt.*;

public class Cerc extends Forma{
    public Cerc(int x, int y, Color culoare) {
        super(x, y, culoare);
    }
    @Override
    public void draw(Graphics g){
        if(esteSelectat) {
            g.setColor(Color.BLACK);
        }else {
            g.setColor(culoare);
        }
        g.drawOval(x - 51, y - 51, 102, 102);
    }
}
