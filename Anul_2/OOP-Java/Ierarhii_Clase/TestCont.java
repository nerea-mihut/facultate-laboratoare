public class TestCont {
    public static void main(String[] args) {
        System.out.println("Testare Cont Bancar");
        ContBancarExtins cont = new ContBancarExtins(1000, 0.05); // 1000 lei, 5% dobanda

        cont.afisare();

        cont.adauga(500);
        System.out.println("Am adaugat 500. Sold nou: " + cont.getSuma());

        cont.extrage(200);
        System.out.println("Am retras 200. Sold nou: " + cont.getSuma());

        cont.adaugaDobandaLunara();

        System.out.println("\nSituatie finala:");
        cont.afisare();
    }
}