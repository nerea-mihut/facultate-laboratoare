#include <iostream>
#include <string>
using namespace std;
// daca nu fol virtual ignora implement specifice din clasele derivate si o sa am memory leaks

class Persoana {
private:
    string nume;
    int varsta;
public:
    Persoana(string n, int v) : nume(n), varsta(v) {}

    virtual void afiseazaIdentitate() { //cand aj aici la rulare, mergi la adresa obiectului uita-te în tab și vezi ce este
        cout << "Persoana: " << nume << ", Varsta: " << varsta << " ani" << endl;
    }
    virtual ~Persoana() {}
};

class Job { //asta ii abstracta pt ca fiecare pers are job dar nu pot zice de pe acum care, ii prea general
private:
    string titluJob;
    double salariu;
public:
    Job(string titlu, double sal) : titluJob(titlu), salariu(sal) {}

    virtual void descrieResponsabilitati() = 0; //responsabilitățile depind de jobul specific, fortez clasa angajat sa implementeze metoda

    virtual void afiseazaDetaliiFinanciare() {
        cout << "Pozitie: " << titluJob << ", Salariu: " << salariu << " RON" << endl;
    }

    virtual ~Job() {}
};

class Angajat : public Persoana, public Job { //most multip
    string companie;
public:
    Angajat(string n, int v, string titlu, double sal, string comp)
        : Persoana(n, v), Job(titlu, sal), companie(comp) {}


    void descrieResponsabilitati() { //daca nu scriu asta clasa ramane si eaabstracta si nu am fi putut crea obiecte in ea
        cout << "Responsab: Indeplineste sarcinile specifice de " << titluJob << " la " << companie << "." << endl;
    }

    void afiseazaComplet() {//af toate datele (le combin)
        afiseazaIdentitate();     // din pers
        afiseazaDetaliiFinanciare(); // din j*b
        descrieResponsabilitati();
    }
};

int main() {
    //creez ob prin aloc dinamica
    Angajat* ang = new Angajat("Popescu Andrei", 30, "Software Developer", 8500, "TechCorp");
//permite să control durata de viață a ob și pregătește  polimorfism (dacă am fi avut mai multe tipuri de angajați)
    cout << "Info Angajat" << endl; //ca sa ma asigur ca se elib memorie

    ang->afiseazaComplet();

    delete ang;

    return 0;
}