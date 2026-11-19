import java.util.Arrays;
import java.util.Random;

public class Main {
    static final Random random = new Random();

    static final int VOLUM_RUCSAC = 35;

    static final int[][] OBIECTE = {
            {15, 5},
            {1, 6},
            {2, 2},
            {5, 10},
            {2, 15},
            {20, 20},
            {12, 6}
    };

    static final int Pop_Size = 10;
    static final int Nr_gen = 20;
    static final int Prob_mutatie = 5;

    static class Individ {
        private int[] cromozom;
        private Integer fitness;

        Individ() {
            cromozom = new int[OBIECTE.length];
            fitness = null;
        }

        ;

        Individ(int[] cromozom) {
            this.cromozom = cromozom.clone();
            fitness = null;
        }

        ;

        void generateRandom() {
            for (int i = 0; i < cromozom.length; i++) {
                cromozom[i] = random.nextInt(2);
            }
            fitness = null;
        }

        ;

        int[] getCromozom() {
            return cromozom.clone();
        }

        ;

        void printCromozom() {
            System.out.println(Arrays.toString(cromozom));
        }

        ;

        void mutatie() {
            for (int j = 0; j < cromozom.length; j++) {
                boolean HasMutation = random.nextInt(100) < Prob_mutatie;

                if (HasMutation) {
                    cromozom[j] = 1 - cromozom[j];
                }
                ;
            }
            ;
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

    public static void main(String[] args) {
        Individ ind = new Individ(new int[] {0, 1, 0, 1, 1, 0, 0} );
        ind.printCromozom();
        System.out.println("Fitness: " + ind.getFitness());
    };

}