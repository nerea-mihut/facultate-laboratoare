public class ContBancarExtins extends ContBancar{
    double rataDobandaAnuala;

    public ContBancarExtins(double sumaInitiala, double rataDobandaAnuala){
        super(sumaInitiala);
        this.rataDobandaAnuala = rataDobandaAnuala;
    }

    @Override
    public void afisare(){
        super.afisare();
        System.out.println("Rata de dobanda anuala: " + (this.rataDobandaAnuala *100) + "%");
    }

    public void adaugaDobandaLunara(){
        double dobanda = getSuma() * (this.rataDobandaAnuala / 12);

        adauga(dobanda);
        System.out.println("Dobanda adaugata: " + dobanda);
    }
}
