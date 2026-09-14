#include <graphics.h>
#include <fstream>
#include <vector>
#include <iostream>
#include <cmath>

using namespace std;

#define PI 3.14159265

struct Punct {
    int x, y;
};

//functie pt poli
void deseneazaPoligon(vector<Punct> p, int culoare) {
    int n = p.size();
    if(n == 0) return;

    int* puncte_bgi = new int[2 * (n + 1)];
    for (int i = 0; i < n; i++) {
        puncte_bgi[2 * i] = p[i].x;
        puncte_bgi[2 * i + 1] = p[i].y;
    }
    puncte_bgi[2 * n] = p[0].x;
    puncte_bgi[2 * n + 1] = p[0].y;

    setcolor(culoare);
    drawpoly(n + 1, puncte_bgi);
    delete[] puncte_bgi;
}

int main() {
    initwindow(800, 600, "Tema Poligoane - Corectat");
    
    ifstream fin("poligon.txt");
    vector<Punct> poliOriginal;
    Punct temp;
    
    while (fin >> temp.x >> temp.y) {
        poliOriginal.push_back(temp);
    }
    fin.close();
    
    if (poliOriginal.empty()) {
        outtextxy(20, 20, (char*)"Eroare: Fisierul poligon.txt e gol sau lipseste!");
        getch();
        return 1;
    }
    
    deseneazaPoligon(poliOriginal, WHITE);
    
    //transl prim nod
    int nouX1 = 450, nouY1 = 50;
    int dx = nouX1 - poliOriginal[0].x;
    int dy = nouY1 - poliOriginal[0].y;
    
    vector<Punct> poliTranslatat1 = poliOriginal;
    for (size_t i = 0; i < poliTranslatat1.size(); i++) {
        poliTranslatat1[i].x += dx;
        poliTranslatat1[i].y += dy;
    }
    deseneazaPoligon(poliTranslatat1, RED);

    //transl dupa c greutate
    double cgX = 0, cgY = 0;
    for (size_t i = 0; i < poliOriginal.size(); i++) {
        cgX += poliOriginal[i].x;
        cgY += poliOriginal[i].y;
    }
    cgX /= poliOriginal.size();
    cgY /= poliOriginal.size();
    
    int destCGX = 150, destCGY = 450;
    int dCGX = destCGX - (int)cgX;
    int dCGY = destCGY - (int)cgY;
    
    vector<Punct> poliTranslatatCG = poliOriginal;
    for (size_t i = 0; i < poliTranslatatCG.size(); i++) {
        poliTranslatatCG[i].x += dCGX;
        poliTranslatatCG[i].y += dCGY;
    }
    deseneazaPoligon(poliTranslatatCG, GREEN);
    
    //poli regulat
    setcolor(YELLOW);
    int laturi = 6, R = 60;
    int X_ecran = 400, Y_ecran = 300;
    int p_reg[14];
    
    for(int i = 0; i < laturi; i++) {
        double alfa = i * 2 * PI / laturi;
        p_reg[2*i] = X_ecran + (int)(R * cos(alfa));
        p_reg[2*i+1] = Y_ecran + (int)(R * sin(alfa));
    }
    p_reg[2*laturi] = p_reg[0];
    p_reg[2*laturi+1] = p_reg[1];
    
    drawpoly(laturi + 1, p_reg);
    
    outtextxy(20, 570, (char*)"Alb: Original | Rosu: Primul Nod | Verde: Centru Greutate | Galben: Regulat");
    
    getch();
    closegraph();
    return 0;
}