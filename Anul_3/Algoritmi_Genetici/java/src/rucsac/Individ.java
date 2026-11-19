package rucsac;

import java.util.Arrays;
import java.util.Random;

import static rucsac.ProblemaRucsacului.OBIECTE;
import static rucsac.ProblemaRucsacului.Prob_mutatie;
import static rucsac.ProblemaRucsacului.VOLUM_RUCSAC;

public class Individ {
    private static final Random random = new Random();

    private int[] cromozom;
    private Integer fitness;

    Individ() {
        cromozom = new int[OBIECTE.length];
        fitness = null;
    };

    Individ(int[] cromozom) {
        this.cromozom = cromozom.clone();
        fitness = null;
    };

    void generateRandom() {
        for (int i = 0; i < cromozom.length; i++) {
                cromozom[i] = random.nextInt(2);
        }
        fitness = null;
    };
    int[] getCromozom() {
            return cromozom.clone();
        };

    void printCromozom() {
            System.out.println(Arrays.toString(cromozom));
        };

    void mutatie() {
        for (int j = 0; j < cromozom.length; j++) {
            boolean HasMutation = random.nextInt(100) < Prob_mutatie;
            if (HasMutation) {
                cromozom[j] = 1 - cromozom[j];
            };
        };
        fitness = null;
    }

    int getFitness() {
        if (fitness == null) {
            int valoareTotala = 0;
            int volumTotal = 0;

            for (int i = 0; i < OBIECTE.length; i++) {
                if (cromozom[i] == 1) {
                    valoareTotala += OBIECTE[i][1];
                    volumTotal += OBIECTE[i][0];
                }
                if (volumTotal > VOLUM_RUCSAC) {
                    fitness = 0;
                } else {
                    fitness = valoareTotala;
                }
            }
        }
        return fitness;

    }
}
