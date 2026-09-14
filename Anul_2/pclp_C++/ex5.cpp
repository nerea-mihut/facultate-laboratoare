#include <iostream>
#include <string>

using namespace std;

class Media {
protected:
    string titlu;
    int durata; // in secunde
public:
    Media(string t, int d) : titlu(t), durata(d) {}

    virtual void play() = 0; 
    virtual int getDurata() { return durata; }
    string getTitlu() { return titlu; }

    virtual ~Media() {}
};


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
    int minutRamas; 
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
    int minutRamas; 
public:
    Audiobook(string t, int d, string aut) : Media(t, d), autor(aut), minutRamas(0) {}

    void play() {
        cout << "[Audiobook] Se asculta: " << titlu << " scrisa de " << autor << endl;
        cout << "  Ultima pozitie: minutul " << minutRamas << endl;
    }

    void setMinutRamas(int m) { minutRamas = m; }
};

class Playlist {
    Media** elemente; 
    int nr;
    int capacitate;
public:
    Playlist(int cap) : capacitate(cap), nr(0) {
        elemente = new Media*[capacitate]; 
    }

    void operator+=(Media* m) {
        if (nr < capacitate) {
            elemente[nr++] = m;
        }
    }

    Media* operator[](int index) {
        if (index >= 0 && index < nr) {
            return elemente[index];
        }
        return NULL;
    }

    int getNrElemente() { return nr; }

    ~Playlist() {
        for (int i = 0; i < nr; i++) {
            delete elemente[i]; 
        }
        delete[] elemente; 
    }
};

int main() {
    Playlist list(10);

    list += new Melodie("Bohemian Rhapsody", 354, "Queen");

    Podcast* p = new Podcast("Interviu Tech", 3600, "Elon Musk");
    p->setMinutRamas(15);
    list += p;

    Audiobook* a = new Audiobook("Dune", 72000, "Frank Herbert");
    a->setMinutRamas(120);
    list += a;

    cout << " Redare Playlist Polimorfica" << endl;
    for (int i = 0; i < list.getNrElemente(); i++) {
        list[i]->play();
    }

    return 0;
}
