import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


// 3. Clasa ta legată direct de .form (Fără metode magice sau erori)
public class Acvariu {
    public JPanel panel1;
    public JPanel mainPanel;

    private PanelPesti acvariuDesenat;

    public Acvariu() {
        // Creăm panoul nostru curat în care înoată peștii
        acvariuDesenat = new PanelPesti();

        // Îi spunem lui mainPanel (cel din interfața ta vizuală) să se organizeze pe tot ecranul
        mainPanel.setLayout(new BorderLayout());

        // Introducem panoul cu pești direct în interiorul lui mainPanel!
        mainPanel.add(acvariuDesenat, BorderLayout.CENTER);
    }

    public void pornesteAcvariul() {
        acvariuDesenat.pornesteAnimatia();
    }
}