#include <iostream>
#include <string>

using namespace std;

class ContBancar {
protected:
    string titular;
    double sold;
    string istoric[5]; 
    int nrTranzactii;

    void adaugaInIstoric(string detalii) {
        if (nrTranzactii < 5) {
            istoric[nrTranzactii++] = detalii;
        } else {
            for (int i = 0; i < 4; i++) istoric[i] = istoric[i + 1];
            istoric[4] = detalii;
        }
    }

public:
    ContBancar(string t, double s) : titular(t), sold(s), nrTranzactii(0) {}

    virtual void depune(double suma) = 0;
    virtual void retrage(double suma) = 0;

    friend ostream& operator<<(ostream& out, const ContBancar& c) {
        out << "\nTitular: " << c.titular << " | Sold: " << c.sold << " RON";
        out << "\nIstoric tranzactii:";
        for (int i = 0; i < c.nrTranzactii; i++) {
            out << "\n  - " << c.istoric[i];
        }
        return out;
    }

    void operator--(int) {
        double comision = 5.0;
        this->sold -= comision;
        this->adaugaInIstoric("Comision bancar: 5 RON");
    }

    ContBancar& operator+(ContBancar& altul) {
        this->sold += altul.sold;
        this->adaugaInIstoric("Transfer unire conturi: +" + to_string(altul.sold));
        altul.sold = 0;
        altul.adaugaInIstoric("Cont inchis (transferat)");
        return *this;
    }

    double getSold() { return sold; }
    virtual ~ContBancar() {}
};


class ContCurent : public ContBancar {
    double limitaDescoperire;
public:
    ContCurent(string t, double s, double limita) : ContBancar(t, s), limitaDescoperire(limita) {}

    void depune(double suma) {
        sold += suma;
        adaugaInIstoric("Depunere: " + to_string(suma));
    }

    void retrage(double suma) {
        if (sold + limitaDescoperire >= suma) {
            sold -= suma;
            adaugaInIstoric("Retragere: " + to_string(suma));
        } else {
            cout << "\nFonduri insuficiente (limita descoperire depasita)!";
        }
    }
};

class ContEconomii : public ContBancar {
    double rataDobanda; 
    double soldMinim;
public:
    ContEconomii(string t, double s, double dobanda, double minim)
        : ContBancar(t, s), rataDobanda(dobanda), soldMinim(minim) {}

    void depune(double suma) {
        double bonus = suma * rataDobanda;
        sold += (suma + bonus);
        adaugaInIstoric("Depunere: " + to_string(suma) + " (Dobanda: " + to_string(bonus) + ")");
    }

    void retrage(double suma) {
        if (sold - suma >= soldMinim) {
            sold -= suma;
            adaugaInIstoric("Retragere: " + to_string(suma));
        } else {
            cout << "\nRetragere refuzata! Soldul trebuie sa ramana peste " << soldMinim;
        }
    }
};

int main() {
    ContBancar* c1 = new ContCurent("Ionescu Dan", 1000, 500);
    ContBancar* c2 = new ContEconomii("Popescu Ana", 2000, 0.05, 500);

    c1->depune(200);
    c1->retrage(1400);

    c2->depune(500);
    c2->retrage(2200);

    (*c1)--;

    cout << *c1;
    cout << *c2;

    cout << "\n\n--- Unire conturi (c1 + c2) ---";
    *c1 + *c2;

    cout << *c1;
    cout << *c2;

    delete c1;
    delete c2;

    return 0;
}
