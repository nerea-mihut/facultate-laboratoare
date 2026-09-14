#include <iostream>
#include <string>

using namespace std;

// --- Clasa Abstracta: Media  ---
class Media {
protected:
    string titlu;
    int durata; // in secunde
public:
    Media(string t, int d) : titlu(t), durata(d) {}

    virtual void play() = 0; // Metoda virtuala pura [cite: 1092]
    virtual int getDurata() { return durata; }
    string getTitlu() { return titlu; }

    virtual ~Media() {} // Destructor virtual obligatoriu [cite: 1075]
};

// --- Clase Derivate ---

class Melodie : public Media {
    string artist;
public:
    Melodie(string t, int d, string art) : Media(t, d), artist(art) {}

    void play() {
        cout << "[Melodie] Se reda: " << titlu << " de " << artist << endl;
    }
};

class Podcast : public Media {
    string invitat;
    int minutRamas; // stateful [cite: 4]
public:
    Podcast(string t, int d, string inv) : Media(t, d), invitat(inv), minutRamas(0) {}

    void play() {
        cout << "[Podcast] Se reda: " << titlu << " cu invitatul " << invitat << endl;
        cout << "  Reluare de la minutul: " << minutRamas << endl;
    }

    void setMinutRamas(int m) { minutRamas = m; }
};

class Audiobook : public Media {
    string autor;
    int minutRamas; // stateful
public:
    Audiobook(string t, int d, string aut) : Media(t, d), autor(aut), minutRamas(0) {}

    void play() {
        cout << "[Audiobook] Se asculta: " << titlu << " scrisa de " << autor << endl;
        cout << "  Ultima pozitie: minutul " << minutRamas << endl;
    }

    void setMinutRamas(int m) { minutRamas = m; }
};

// --- Clasa Container: Playlist [cite: 220-232, 642] ---
class Playlist {
    Media** elemente; // Tablou de pointeri la clasa de baza [cite: 1057]
    int nr;
    int capacitate;
public:
    Playlist(int cap) : capacitate(cap), nr(0) {
        elemente = new Media*[capacitate]; // Alocare dinamica [cite: 782-783]
    }

    // Supradefinire operator += pentru adaugare elemente [cite: 157-159]
    void operator+=(Media* m) {
        if (nr < capacitate) {
            elemente[nr++] = m;
        }
    }

    // Supradefinire operator [] pentru acces la pointeri [cite: 218-219, 229]
    Media* operator[](int index) {
        if (index >= 0 && index < nr) {
            return elemente[index];
        }
        return NULL;
    }

    int getNrElemente() { return nr; }

    ~Playlist() {
        for (int i = 0; i < nr; i++) {
            delete elemente[i]; // Eliberare obiecte [cite: 50, 111]
        }
        delete[] elemente; // Eliberare tablou [cite: 803]
    }
};

int main() {
    Playlist list(10);

    // Adaugare elemente media (alocare dinamica) [cite: 48]
    list += new Melodie("Bohemian Rhapsody", 354, "Queen");

    Podcast* p = new Podcast("Interviu Tech", 3600, "Elon Musk");
    p->setMinutRamas(15);
    list += p;

    Audiobook* a = new Audiobook("Dune", 72000, "Frank Herbert");
    a->setMinutRamas(120);
    list += a;

    cout << "--- Redare Playlist Polimorfica ---" << endl;
    for (int i = 0; i < list.getNrElemente(); i++) {
        // Accesare prin operatorul [] si apel play() polimorfic [cite: 1067]
        list[i]->play();
        cout << "-----------------------------------" << endl;
    }

    return 0;
}