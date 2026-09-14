#include <iostream>
#include <string>
using namespace std;
// daca nu fol virtual ignora implement specifice din clasele derivate si o sa am memory leaks
class Vehicle {
protected:
    string marca;
    double pretCombustibil; // pret l
public:
    Vehicle(string m, double p) : marca(m), pretCombustibil(p) {}

    virtual double calculeazaCostPeKm() = 0;// m virt pura obliga ' sa implementeze propriul cal

    virtual string getTip() = 0;

    string getMarca() {
        return marca;
    }

    virtual ~Vehicle() {}
};


class Car : public Vehicle { // '
    double consumLaSuta;
public:
    Car(string m, double p, double c) : Vehicle(m, p), consumLaSuta(c) {}

    double calculeazaCostPeKm() {
        return (consumLaSuta / 100.0) * pretCombustibil;
    }

    string getTip() {
        return "Autoturism";
    }
};

class Bus : public Vehicle {
    double consumLaSuta;
    int nrPasageri;
public:
    Bus(string m, double p, double c, int n) : Vehicle(m, p), consumLaSuta(c), nrPasageri(n) {}

    double calculeazaCostPeKm() {
        return ((consumLaSuta / 100.0) * pretCombustibil) + (nrPasageri * 0.05); //bilet bus
    }

    string getTip() {
        return "Autobuz";
    }
};

class Truck : public Vehicle {
    double consumGolit; // fara marfa
    double greutateTone;
public:
    Truck(string m, double p, double c, double g) : Vehicle(m, p), consumGolit(c), greutateTone(g) {}


    double calculeazaCostPeKm() {
        double consumReal = (consumGolit + (greutateTone * 2)) / 100.0;
        return consumReal * pretCombustibil; //prt creste cu incarcatura
    }

    string getTip() { return "Camion"; }
};

int main() {
    int n = 3;

    Vehicle** transp = new Vehicle*[n];// pointeri catre clasa Vehicle*


    transp[0] = new Car("Dacia", 7.5, 6.5); //aloc dinamica
    transp[1] = new Bus("Mercedes", 7.5, 25.0, 40);
    transp[2] = new Truck("Volvo", 7.5, 30.0, 15.5);

    cout << " Calcul costuri transport pe km\n";

    for (int i = 0; i < n; i++) { //apeleaza metoda specifica pt fiecare vehicul (aloc dinamic
        cout << transp[i]->getTip() << " " << transp[i]->getMarca() << ": ";
        cout << transp[i]->calculeazaCostPeKm() << " RON/km" << endl;
    }

    for (int i = 0; i < n; i++) {
        delete transp[i]; //el memorie
    }
    delete[] transp;

    return 0;
}