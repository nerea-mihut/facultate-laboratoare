#include <graphics.h>
#include <iostream>
#include <cmath>

using namespace std;

#define PI 3.14159265

struct NodPunct {
    double x, y;
    NodPunct* next;
};

struct NodPoligon {
    NodPunct* puncte;
    int nrPuncte;
    NodPoligon* next;
};

void adaugaPunct(NodPunct*& cap, double x, double y) {
    NodPunct* nou = new NodPunct;
    nou->x = x;
    nou->y = y;
    nou->next = cap;
    cap = nou;
}

void adaugaPoligon(NodPoligon*& scena, NodPunct* listaPuncte, int nr) {
    NodPoligon* nou = new NodPoligon;
    nou->puncte = listaPuncte;
    nou->nrPuncte = nr;
    nou->next = scena;
    scena = nou;
}

void deseneazaScena(NodPoligon* scena, int culoare) {
    setcolor(culoare);
    NodPoligon* p = scena;
    while (p != NULL) {
        int* coord = new int[2 * (p->nrPuncte + 1)];
        NodPunct* pt = p->puncte;
        int i = 0;
        while (pt != NULL) {
            coord[2 * i] = (int)pt->x;
            coord[2 * i + 1] = (int)pt->y;
            pt = pt->next;
            i++;
        }
        coord[2 * p->nrPuncte] = coord[0];
        coord[2 * p->nrPuncte + 1] = coord[1];
        drawpoly(p->nrPuncte + 1, coord);
        delete[] coord;
        p = p->next;
    }
}

void transformaScena(NodPoligon* scena, double unghi, double scalare, int px, int py) {
    double rad = unghi * PI / 180.0;
    NodPoligon* p = scena;
    while (p != NULL) {
        NodPunct* pt = p->puncte;
        while (pt != NULL) {
            // SCALARE
            pt->x = px + (pt->x - px) * scalare;
            pt->y = py + (pt->y - py) * scalare;
            // ROTIRE
            double x_rel = pt->x - px;
            double y_rel = pt->y - py;
            pt->x = px + (x_rel * cos(rad) - y_rel * sin(rad));
            pt->y = py + (x_rel * sin(rad) + y_rel * cos(rad));
            pt = pt->next;
        }
        p = p->next;
    }
}

int main() {
    initwindow(800, 600, "Scena Corectata");
    
    NodPoligon* scena = NULL;
    int cX = 400, cY = 300;
    
    NodPunct* p1 = NULL;
    adaugaPunct(p1, cX - 50, cY - 150);
    adaugaPunct(p1, cX + 50, cY - 150);
    adaugaPunct(p1, cX, cY - 220);
    adaugaPoligon(scena, p1, 3);
    
    NodPunct* p2 = NULL;
    adaugaPunct(p2, cX + 150, cY + 50);
    adaugaPunct(p2, cX + 220, cY + 50);
    adaugaPunct(p2, cX + 220, cY - 20);
    adaugaPunct(p2, cX + 150, cY - 20);
    adaugaPoligon(scena, p2, 4);
    
    NodPunct* p3 = NULL;
    adaugaPunct(p3, cX - 220, cY + 50);
    adaugaPunct(p3, cX - 120, cY + 50);
    adaugaPunct(p3, cX - 120, cY + 10);
    adaugaPunct(p3, cX - 220, cY + 10);
    adaugaPoligon(scena, p3, 4);
    
    deseneazaScena(scena, WHITE);
    transformaScena(scena, 45, 1.1, cX, cY);
    deseneazaScena(scena, GREEN);
    
    outtextxy(20, 20, (char*)"Alb: Pozitie initiala | Verde: Rotit 45 grade & Scalat 1.1x");
    
    getch();
    closegraph();
    return 0;
}