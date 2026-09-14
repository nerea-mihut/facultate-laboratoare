#include <iostream>
#include <string>
using namespace std;
//scrii o singura buc de cod care poate lucra cu oricare dintre acestea(nr intregi, nr reale etc).
template <class T1, class T2>
class Pair {
private:
    T1 primaValoare;
    T2 aDouaValoare;

public:Pair(T1 v1, T2 v2) : primaValoare(v1), aDouaValoare(v2) {}

    void setPrima(T1 v) { primaValoare = v; } //fct de setare
    void setADoua(T2 v) { aDouaValoare = v; }

    T1 getPrima() const { return primaValoare; }// fct de obtin
    T2 getADoua() const { return aDouaValoare; }

    friend ostream& operator<<(ostream& out, const Pair<T1, T2>& p) { //frienda ca sa accesez membrii privati
        out << "(" << p.primaValoare << ", " << p.aDouaValoare << ")";
        return out;
    }
};

int main() {
    Pair<int, double> p1(10, 3.14);    //tipuri intregi si nr R

    cout << "Pereche 1 (int, double): " << p1 << endl;

    Pair<string, double> p2("Pret", 99.99);    //siruri de caractere si nr R

    cout << "Pereche 2 (string, double): " << p2 << endl;

    p1.setPrima(50);    //test functii

    cout << "Pereche 1 modificata: " << p1.getPrima() << " si " << p1.getADoua() << endl;

    return 0;
}