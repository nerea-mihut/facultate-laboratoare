import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import java.io.File;
import javax.imageio.ImageIO;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;

public class GamePanel extends JPanel implements ActionListener, MouseMotionListener, MouseListener {

    private Timer gameTimer;
    private ArrayList<Cat> pisici;
    private ArrayList<Sparkle> sclipiciList;

    private ArrayList<Point> mouseHistory;

    private int currentMouseX = -100;
    private int currentMouseY = -100;

    private BufferedImage imagineFundal;
    private Font fontRetro;
    private BufferedImage imagineSadCat;

    public static int score = 0;
    public static int currentLevel = 1;
    private boolean isGameOver = false;

    private double sadCatScale = 0;
    private double sadCatAngle = 0;
    private boolean showGameOverText = false;

    public GamePanel() {
        setBackground(Color.DARK_GRAY);

        try {
            imagineFundal = ImageIO.read(new File("pisici/background.png"));
        } catch (Exception e) {
            System.out.println("Eroare critică: Nu am găsit fișierul 'pisici/background.png'!");
        }

        try {
            imagineSadCat = ImageIO.read(new File("pisici/sadcat.png"));
        } catch (Exception e) {
            System.out.println("Nu am găsit fișierul 'pisici/sadcat.png'!");
        }

        try {
            Font fontBrut = Font.createFont(Font.TRUETYPE_FONT, new File("pisici/bitcount.ttf"));
            fontRetro = fontBrut.deriveFont(18f);
        } catch (Exception e) {
            System.out.println("Nu am găsit fontul! Folosesc un font de rezervă.");
            fontRetro = new Font("Monospaced", Font.BOLD, 18);
        }

        pisici = new ArrayList<>();
        pisici.add(new Cat());

        sclipiciList = new ArrayList<>();
        mouseHistory = new ArrayList<>();

        addMouseMotionListener(this);
        addMouseListener(this);

        ascundeCursorulOS();

        gameTimer = new Timer(16, this);
        gameTimer.start();
    }
//sistem audio
    private void playSound(String fileName) {
        try {
            File audioFile = new File("pisici/" + fileName);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            System.out.println("Eroare la redarea sunetului: " + fileName);
        }
    }

    private void ascundeCursorulOS() {
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "cursor invizibil"
        );
        setCursor(blankCursor);
    }

    private void verificaDificultate() {
        if (score >= 20 && currentLevel == 1) {
            currentLevel = 2;
            pisici.add(new Cat());
            playSound("levelup.wav");

        } else if (score >= 60 && currentLevel == 2) {
            currentLevel = 3;
            pisici.add(new Cat());
            playSound("levelup.wav");

        } else if (score >= 120 && currentLevel == 3) {
            currentLevel = 4;
            pisici.add(new Cat());
            playSound("levelup.wav");

        } else if (score >= 250 && currentLevel == 4) {
            currentLevel = 5;
            pisici.add(new Cat());
            playSound("levelup.wav");
        }
    }

    private void creeazaExplozieSclipici(double x, double y, String catName) {
        Color[] culoriPisica;
        String shape = "star";

        if (catName.equals("xmas")) {
            culoriPisica = new Color[]{Color.RED, Color.GREEN, Color.WHITE};
            shape = "coin";
        } else if (catName.equals("egypt")) {
            culoriPisica = new Color[]{Color.YELLOW, Color.ORANGE, new Color(255, 215, 0)};
            shape = "coin";
        } else {
            culoriPisica = new Color[]{Color.PINK, Color.CYAN, Color.WHITE, Color.YELLOW};
            shape = "star";
        }

        for (int i = 0; i < 30; i++) {
            Color culoareAleatorie = culoriPisica[(int)(Math.random() * culoriPisica.length)];
            sclipiciList.add(new Sparkle(x, y, culoareAleatorie, shape));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //ofera o netezire a formelor

        if (imagineFundal != null) {
            g2d.drawImage(imagineFundal, 0, 0, 800, 600, null);
        }

        if (!isGameOver) {
            for (Cat p : pisici) {
                p.draw(g);
            }
        }

        for (Sparkle s : sclipiciList) {
            s.draw(g);
        }//ordinea straturilor

        if (mouseHistory.size() > 1 && !isGameOver) {
            g2d.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (int i = 0; i < mouseHistory.size() - 1; i++) {
                Point p1 = mouseHistory.get(i);
                Point p2 = mouseHistory.get(i + 1);

                float alpha = (float) i / mouseHistory.size();
                g2d.setColor(new Color(255, 0, 0, (int)(alpha * 200)));
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y); //coada taoeturii se estompeaza si ofera efectul ala de miscare
            }
            g2d.setStroke(new BasicStroke(1));
        }

        if (!isGameOver) { //fac culoarea mouseului
            g2d.setColor(new Color(255, 0, 0, 80));
            g2d.fillOval(currentMouseX - 6, currentMouseY - 6, 12, 12);
            g2d.setColor(Color.RED);
            g2d.fillOval(currentMouseX - 2, currentMouseY - 2, 4, 4);
        }

        g2d.setFont(fontRetro);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Score: " + score, 22, 32);
        g2d.drawString("Level: " + currentLevel, 22, 57);//umbra scrisului

        Color rozRetro = new Color(220, 110, 210);
        g2d.setColor(rozRetro);
        g2d.drawString("Score: " + score, 20, 30);
        g2d.drawString("Level: " + currentLevel, 20, 55); //scrisu in sine

        if (isGameOver) {
            g2d.setColor(new Color(150, 0, 0, 180));
            g2d.fillRect(0, 0, 800, 600);

            if (imagineSadCat != null) {
                AffineTransform transformareVeche = g2d.getTransform(); //blochez imaginea pe fundal
                g2d.translate(400, 230);//centru atentiei in mij ferestrei
                g2d.rotate(sadCatAngle);
                int currentSize = (int) sadCatScale;
                g2d.drawImage(imagineSadCat, -currentSize / 2, -currentSize / 2, currentSize, currentSize, null);
                g2d.setTransform(transformareVeche); //fac sa se invarta imaginea in jurul propriei axe
            }

            if (showGameOverText) {
                g2d.setFont(fontRetro.deriveFont(50f));
                g2d.setColor(Color.BLACK);
                g2d.drawString("GAME OVER", 233, 443);
                g2d.setColor(Color.WHITE);
                g2d.drawString("GAME OVER", 230, 440);

                g2d.setFont(fontRetro.deriveFont(22f));
                g2d.setColor(Color.BLACK);
                g2d.drawString("You lasered the demonic cat", 182, 502);
                g2d.setColor(rozRetro);
                g2d.drawString("You lasered the demonic cat", 180, 500);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) { //coord tot ce se misca, schimba moare etc
        if (isGameOver) {
            if (sadCatScale < 300) {
                sadCatScale += 8; //creste imaginea cu pisica sad
                sadCatAngle += 0.4; //se roteste pe masura ce creste
            } else {
                sadCatScale = 300;
                sadCatAngle = 0;
                showGameOverText = true;
            }
            repaint();
            return;
        }

        verificaDificultate();
        for (Cat p : pisici) {
            p.update();//Fiecare pisică își calculează singură noua poziție, gravitația și ricoșeul în pereț
        }

        sclipiciList.removeIf(s -> s.life <= 0);
        for (Sparkle s : sclipiciList) {// daca sclipiciu are viata 0 sau mai putin, sterge o
            s.update();
        }

        while (mouseHistory.size() > 8) {
            mouseHistory.remove(0);//limitează lungimea cozii laserului la 8 puncte
        }
        if (mouseHistory.size() > 0) {
            mouseHistory.remove(0);
        }

        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        currentMouseX = e.getX();
        currentMouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isGameOver) return;

        currentMouseX = e.getX();
        currentMouseY = e.getY();

        mouseHistory.add(new Point(currentMouseX, currentMouseY));

        for (Cat p : pisici) {
            if (!p.isSliced && p.pauza == 0) {
                double distance = Math.sqrt(Math.pow(currentMouseX - p.x, 2) + Math.pow(currentMouseY - p.y, 2));// distanta dintre centu mouseului si centru picisii

                if (distance <= p.radius) {
                    p.isSliced = true;
                    String numePisica = p.getCatName();
//inghet pisica  taiata, dispare desenu si incepe animatia cu sclipici
                    creeazaExplozieSclipici(p.x, p.y, numePisica);

                    if (numePisica.equals("demonic")) {
                        isGameOver = true;
                        playSound("sad.wav");

                    } else if (numePisica.equals("egypt")) {
                        score += 10 * currentLevel;
                        playSound("bonus.wav");

                    } else if (numePisica.equals("xmas")) {
                        score += 15 * currentLevel;
                        playSound("bonus.wav");

                    } else {
                        score += 1;
                        // NOU: Sunetul pentru pisicile standard
                        playSound("glitter.wav");
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!isGameOver) {
            currentMouseX = e.getX(); //in momentu in care dau blick salvez coord pix de pe ecran, devine pct de origine pt verificare
            currentMouseY = e.getY();
            mouseHistory.add(new Point(currentMouseX, currentMouseY));//jocul se va uita la această listă și va desena linii între punctele consecutive
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
}