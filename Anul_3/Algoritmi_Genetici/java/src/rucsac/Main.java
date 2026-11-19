package rucsac;

public class Main {

    public static void main(String[] args) {
        AlgoritmGenetic algoritm = new AlgoritmGenetic();

        algoritm.genPopInitiala();

        System.out.println("Populatia initiala:");
        algoritm.afiseazaPopulatia();

        System.out.println(
                "Fitness mediu: " + algoritm.medFitnessPerPopulatie()
        );

        Individ[] parinti = algoritm.selectiaParintilor();

        System.out.println("Parintele 1:");
        parinti[0].printCromozom();

        System.out.println("Parintele 2:");
        parinti[1].printCromozom();
    }
}