import javax.swing.JFrame;

//aici am creeat fereastra unde se va desena totul

public class CatNinja extends JFrame {

    public CatNinja() {
        setTitle("Cat Ninja - Pixel Art Edition");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        GamePanel panel = new GamePanel();
        add(panel);
    }
}