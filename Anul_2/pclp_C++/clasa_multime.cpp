#include <iostream>
#include <string>

using namespace std;

class Produs {
protected:
    string nume;
    double pretBaza;
public:
    Produs(string n, double p) : nume(n), pretBaza(p) {}

    // Metode virtuale pure
    virtual double getPretFinal() = 0;
    virtual string getDescriere() = 0;

    double operator+(Produs& altul) {
        return this->getPretFinal() + altul.getPretFinal();
    }

    int operator==(Produs& altul) {
        return this->nume == altul.nume;
    }

    string getNume() { return nume; }

    virtual ~Produs() {}
};


class ProdusFizic : public Produs {
    double greutate;
    double costLivrareKg;
public:
    ProdusFizic(string n, double p, double g, double cl)
        : Produs(n, p), greutate(g), costLivrareKg(cl) {}

    double getPretFinal() {
        return pretBaza + (greutate * costLivrareKg);
    }

    string getDescriere() {
        return "[Fizic] " + nume;
    }
};

class ProdusDigital : public Produs {
    string linkDownload;
    bool esteWeekend;
public:
    ProdusDigital(string n, double p, string link, int weekend)
        : Produs(n, p), linkDownload(link), esteWeekend(weekend) {}

    double getPretFinal() {
        if (esteWeekend) return pretBaza * 0.90;
        return pretBaza;
    }

    string getDescriere() {
        return "[Digital] " + nume + " (Link: " + linkDownload + ")";
    }
};

class ProdusPerisabil : public Produs {
    int zilePanaLaExpirare;
public:
    ProdusPerisabil(string n, double p, int zile)
        : Produs(n, p), zilePanaLaExpirare(zile) {}

    double getPretFinal() {
        if (zilePanaLaExpirare <= 2) return pretBaza * 0.50;
        return pretBaza;
    }

    string getDescriere() {
        return "[Perisabil] " + nume + " (Zile ramase: " + to_string(zilePanaLaExpirare) + ")";
    }
};

class Cos {
    Produs** elemente;
    int nr;
    int capacitate;
public:
    Cos(int cap) {
        this->capacitate = cap;
        this->nr = 0;
        this->elemente = new Produs*[capacitate];
    }

    void adauga(Produs* p) {
        if (nr < capacitate) {
            elemente[nr] = p;
            nr++;
        }
    }

    double calculeazaTotal() {
        double total = 0;
        for (int i = 0; i < nr; i++) {
            total += elemente[i]->getPretFinal();
        }
        return total;
    }

    void afiseazaProduse() {
        for (int i = 0; i < nr; i++) {
            cout << elemente[i]->getDescriere() << " | Pret final: "
                 << elemente[i]->getPretFinal() << " RON" << endl;
        }
    }

    ~Cos() {
        for (int i = 0; i < nr; i++) {
            delete elemente[i];
        }
        delete[] elemente;
    }
};

int main() {
    Cos cosulMeu(5);

    // Adaugare produse prin alocare dinamica
    cosulMeu.adauga(new ProdusFizic("Tastatura", 150, 0.8, 10));
    cosulMeu.adauga(new ProdusDigital("Software", 200, "site.com/soft", 1));
    cosulMeu.adauga(new ProdusPerisabil("Iaurt", 12, 1));

    cout << "Produse in cos:" << endl;
    cosulMeu.afiseazaProduse();

    cout << "\nTotal de plata: " << cosulMeu.calculeazaTotal() << " RON" << endl;

    return 0;
}