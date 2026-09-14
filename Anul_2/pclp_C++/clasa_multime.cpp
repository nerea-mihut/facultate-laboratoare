#include <iostream>
#include <string>

using namespace std;

// --- Clasa Abstracta: Produs ---
class Produs {
protected:
    string nume;
    double pretBaza;
public:
    Produs(string n, double p) : nume(n), pretBaza(p) {}

    // Metode virtuale pure
    virtual double getPretFinal() = 0;
    virtual string getDescriere() = 0;

    // Supradefinire operator + pentru suma preturilor finale
    double operator+(Produs& altul) {
        return this->getPretFinal() + altul.getPretFinal();
    }

    // Supradefinire operator == [cite: 151, 159]
    int operator==(Produs& altul) {
        return this->nume == altul.nume;
    }

    string getNume() { return nume; }

    // Destructor virtual pentru eliberarea corecta a memoriei
    virtual ~Produs() {}
};

// --- Clase Derivate ---

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
    bool esteWeekend; // Folosim int pentru 0/1 (stil C)
public:
    ProdusDigital(string n, double p, string link, int weekend)
        : Produs(n, p), linkDownload(link), esteWeekend(weekend) {}

    double getPretFinal() {
        if (esteWeekend) return pretBaza * 0.90; // Reducere 10%
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
        if (zilePanaLaExpirare <= 2) return pretBaza * 0.50; // Reducere 50%
        return pretBaza;
    }

    string getDescriere() {
        return "[Perisabil] " + nume + " (Zile ramase: " + to_string(zilePanaLaExpirare) + ")";
    }
};

// --- Clasa Cos: Gestionare Tablou Dinamic de Pointeri ---
class Cos {
    Produs** elemente; // Tablou de pointeri catre clasa de baza [cite: 1057]
    int nr;
    int capacitate;
public:
    Cos(int cap) {
        this->capacitate = cap;
        this->nr = 0;
        this->elemente = new Produs*[capacitate]; // Alocare dinamica [cite: 782, 783]
    }

    void adauga(Produs* p) {
        if (nr < capacitate) {
            elemente[nr] = p;
            nr++;
        }
    }

    // Calcul total folosind legarea dinamica (polimorfism) [cite: 1067, 1088]
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

    // Destructor pentru eliberarea memoriei alocate cu new [cite: 49, 50]
    ~Cos() {
        for (int i = 0; i < nr; i++) {
            delete elemente[i];
        }
        delete[] elemente;
    }
};

int main() {
    // Simulare conform cerintelor
    Cos cosulMeu(5);

    // Adaugare produse prin alocare dinamica
    cosulMeu.adauga(new ProdusFizic("Tastatura", 150, 0.8, 10));
    cosulMeu.adauga(new ProdusDigital("Software", 200, "site.com/soft", 1)); // Este weekend
    cosulMeu.adauga(new ProdusPerisabil("Iaurt", 12, 1)); // Expira curand

    cout << "Produse in cos:" << endl;
    cosulMeu.afiseazaProduse();

    cout << "\nTotal de plata: " << cosulMeu.calculeazaTotal() << " RON" << endl;

    return 0;
}