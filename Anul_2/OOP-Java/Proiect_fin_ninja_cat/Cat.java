import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

//definesc pisicile si ce fac

public class Cat {
    public double x, y; //pozitia pe ecran
    public double speedX, speedY; //viteza

    public int radius = 55; //marimea pisicii si zona unde cand dam cu mouseul taiem

    public boolean isSliced = false;
    public int type;

    public int pauza = 0; //stai ascunsa sub ecran pentru X cadre înainte sa sari

    private Random random = new Random();
    private final double GRAVITY = 0.3; // la fiecare 16 milisec gravity se adauga la speedy si trage pisica inapoi jos

    private static final String[] FILE_NAMES = {
            "black.png", "calico.png", "clasic.png", "demonic.png",
            "dumitru.png", "egypt.png", "mitru.png", "siamese.png",
            "tabby.png", "white.png", "xmas.png"
    };

    private static BufferedImage[] spriteSheets = null;
    private static final int TOTAL_FRAMES = 13; //is fix 13 imagini cu pisica pe sprite sheet
    private static int frameWidth = 0;
    private static int frameHeight = 0;

    private int currentFrame = 0;
    private int animationTick = 0;
    private final int ANIMATION_DELAY = 4;

    //constructor in care initializez desenarea pisicilor

    public Cat() {
        if (spriteSheets == null) {
            incarcaToateImaginile(); // daca ii prima pisica din joc intra in incarcatoate... si duce fisierele de pe hard disk în memoria RAM .
        }
        reset(); //odata ce s-a asigurat ca toate imaginile is pregatite, da reset
    }

    //citirea fisierelor de pe hard disk si mutarea lor in ram

    private void incarcaToateImaginile() {
        spriteSheets = new BufferedImage[FILE_NAMES.length]; //cate nume de fisiere am in file names, construieste fix 12 sloturi sa memoreze imaginile respective
        for (int i = 0; i < FILE_NAMES.length; i++) {
            try {
                String caleCatreFisier = "pisici/" + FILE_NAMES[i];
                spriteSheets[i] = ImageIO.read(new File(caleCatreFisier));
            } catch (IOException e) {
                System.err.println("Eroare critică! Nu am găsit fișierul: pisici/" + FILE_NAMES[i]);
            }
        }
        if (spriteSheets[0] != null) { //am resuit sa incarc prima poza? daca da, ia ca model de referinta pt restul
            frameHeight = spriteSheets[0].getHeight();
            frameWidth = spriteSheets[0].getWidth() / TOTAL_FRAMES; //ia latimea totala a sprite sheetului (x pixeli)) și o împarte la TOTAL_FRAMES aka 13
            // => ca un singur patrat de animație (o singură pisică din acea bandă) are lățimea de y pixeli
            // astfel, foarfeca știe exact din câți în câți pixeli să taie pentru a reda alergarea!
        }
    }

    //functie de respawn la pisici=> in loc sa sterg si sa generez o nou pisica pur si simplu u redesenez dar cu different type
    public void reset() {
        this.x = 200 + random.nextInt(400); //am pus 200 pt ca nu vreau sa fie lipita de margine, ii nevoie de spatiu ca sa fie usor de taiat
        this.y = 650; // partea de jos a ecranului

        this.speedY = -(15 + random.nextDouble() * 4); // axa y merge in jos deci imi trb forts negativa ca sa urce in sus, spre 0
        this.speedX = random.nextDouble() * 8 - 4; // generez un unghi unic pt pisici pt ca r=next double ia intre 0.1 si 0.99 random si inmulteste ca sa mearga spre stanga si dreapta

        this.isSliced = false;
        this.currentFrame = 0;

        // Alegem o rasă aleatorie inițială
        this.type = random.nextInt(FILE_NAMES.length);

        //  Probabilitatea ca pisica aleasă să fie "demonic.png" crește cu nivelul
        // La nivelul 1, șansa e 5%, la nivelul 5 e 25%, etc...
        int sansaDemonic = GamePanel.currentLevel * 5;
        if (random.nextInt(100) < sansaDemonic) {
            for (int i = 0; i < FILE_NAMES.length; i++) {
                if (FILE_NAMES[i].equals("demonic.png")) {
                    this.type = i;
                    break;
                }
            }
        } else {
            //aici implementez un sistem de recompensă descrescătoare în funcție de nivel.
            // chiar dacă generatorul aleatoriu alege o pisică bonus, acest filtru îi recalculează probabilitatea de apariție.
            // la lvl 1, jucătorul primește mai multe bonusuri pentru a-i menține interesul, dar la niveluri superioare probabilitatea scade,
            // iar pisicile speciale sunt suprascrise cu pisici clasice pentru a crește gradul de dificultate

            String catName = FILE_NAMES[type];
            if (catName.equals("egypt.png") || catName.equals("xmas.png")) {

                int sansaSaApara = 70 - (GamePanel.currentLevel * 10);
                if (sansaSaApara < 10) sansaSaApara = 10; //indiferent la ce nivel ești mai mare , vei avea mereu măcar o șansă de 10% să prinzi un bonus pt ca altfel r da cu -

                if (random.nextInt(100) > sansaSaApara) {
                    this.type = 2; // "clasic.png"
                }
            }
        }

        this.pauza = random.nextInt(80);  //previne overlapping, desincronizare a spawnului, am atribuit un delay

    }


    public void update() { //cronometru de asteptare pt pisici, stau inghetate sub ecran pana cand le trece pauza
        if (pauza > 0) {
            pauza--;
            return;
        }

        x += speedX;
        y += speedY;
        speedY += GRAVITY; //ofer viteza cand urca si o reduce cu cat ajunge sus si cand cadrul ii unu spre ex 50 si viteza ii 0, sta pe loc o fract de sec si dupa coboara si viteza creste iar
// deci cand urca viteza -15 se aduna cu 0.3 treptat pana ajunge la 0 si dupa invers se aduna ca sa coboare in jos

//wall bouncing, sa nu iasa din cadru pisicile cand zboara
        int limitaStanga = 10 + radius;
        int limitaDreapta = 800 - 10 - radius;

        if (x < limitaStanga) {
            x = limitaStanga;
            speedX = Math.abs(speedX); //daca ii la -4 o fortam sa se mute la 4 aka marginea pisicii sa loveasca marginea ecranului nu centrul ei
        } else if (x > limitaDreapta) {
            x = limitaDreapta;
            speedX = -Math.abs(speedX);
        }

        // Când pisica cade sub ecran
        if (y > 700) {
            if (!isSliced && !getCatName().equals("demonic")) { //daca nu i demonica scazi 2 din scor aka daca ii normala
                GamePanel.score -= 2;
                if (GamePanel.score < 0) {
                    GamePanel.score = 0;
                }
                System.out.println("Ai ratat o pisică! -2 pct. Scor: " + GamePanel.score);
            }
            reset();
        }

        // actualizez animația (trecem la următorul cadru din imagine)
        animationTick++; //contor
        if (animationTick > ANIMATION_DELAY) { //nu schimba poza pisicii pana nu am avut 4 batai ale timerului
            animationTick = 0;
            currentFrame++;//trece la urm imagine din sprie sheet
            if (currentFrame >= TOTAL_FRAMES) { //creez o bucla infinita, sa para ca sare in continuu
                currentFrame = 0;
            }
        }
    }

    public void draw(Graphics g) {
        if (isSliced || pauza > 0) return; //daca pisica nu trebuie sa se vada nu te chinui s ao desenezi

        BufferedImage imagineaCurenta = spriteSheets[type];
        if (imagineaCurenta == null) return;

        int srcX = currentFrame * frameWidth;  //Source X cal unde începe cadrul curent in poza
        // Dacă suntem la cadrul 3 și fiecare cadru are 100px, tăietura începe la pixelul 300.
        int srcY = 0;

        int destX = (int)x - radius;
        int destY = (int)y - radius;

        // marimea desenului ii prop cu taietura
        int size = (int)(radius * 2.5);

        g.drawImage(imagineaCurenta,
                destX, destY, destX + size, destY + size,
                srcX, srcY, srcX + frameWidth, srcY + frameHeight,
                null); // ia o buc din im curenta si intinde o pe ecran intre destx si destx+size
    }

    public String getCatName() {
        return FILE_NAMES[type].replace(".png", "");
    }
}