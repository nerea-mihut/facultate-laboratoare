package rucsac;

import java.util.ArrayList;
import java.util.Random;

import static rucsac.ProblemaRucsacului.*;

public class AlgoritmGenetic {
    private final Random random = new Random();
    private ArrayList<Individ> population = new ArrayList<>();

    public void genPopInitiala() {
        population.clear();

        for (int i = 0; i < Pop_Size; i++) {
            Individ ind = new Individ();
            ind.generateRandom();
            population.add(ind);
        }
    }

    public double medFitnessPerPopulatie() {
        if (population.isEmpty()) {
            return 0.0;
        }

        int total = 0;

        for (Individ ind : population) {
            total += ind.getFitness();
        }

        return (double) total / population.size();
    }

    public Individ[] selectiaParintilor() {
        Individ p1 = population.get(
                random.nextInt(population.size())
        );

        Individ p2 = population.get(
                random.nextInt(population.size())
        );

        return new Individ[] {p1, p2};
    }

    public void afiseazaPopulatia() {
        for (Individ ind : population) {
            ind.printCromozom();
            System.out.println("Fitness: " + ind.getFitness());
        }
    }

    
}
