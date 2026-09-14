public class ContBancar {
    double suma;

    public ContBancar(double sumaInitiala){
        this.suma = sumaInitiala;
    }

    public void adauga(double x){
        if (x>0){
            this.suma += x;
        }
    }

    public boolean extrage(double x){
        if (this.suma >= x){
            this.suma -= x;
            return true;
        } else{
            return false;
        }
    }

    public double getSuma(){
        return this.suma;
    }

    public void afisare() {
        System.out.println("Sold curent: " + this.suma);
    }
}

