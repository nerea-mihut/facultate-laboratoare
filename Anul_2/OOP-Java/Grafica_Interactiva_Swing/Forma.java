import java.awt.*;

public abstract class Forma implements Deseneaza {
    protected int x, y;
    protected Color culoare;
    protected boolean esteSelectat = false;

    public Forma(int x, int y, Color culoare){
        this.x = x;
        this.y = y;
        this.culoare = culoare;
    }

    public void muta(int dx, int dy) {
       this.x += dx;
       this.y += dy;
    }
    public void setCuloare(Color c){
        this.culoare = c;
    }

    public void setSelectat(boolean s) { this.esteSelectat = s; }
    public boolean isSelectat() { return esteSelectat; }

    public abstract void draw(Graphics g);
    public int getX(){return x;}
    public int getY(){return y;}
}