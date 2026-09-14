#include <graphics.h>
#include <cmath>
#include <vector>

using namespace std;

#define PI 3.14159265

struct Punct {
    double x, y;
};

typedef vector<Punct> Poligon;

// construieste figura
Poligon genereazaFiguraStandard(int Xc, int Yc, int L) {
    Poligon puncte;
    int rezolutie = 25;
    float curbura = L / 12.0;
    
    Punct colturile[4] = {
        {Xc - L/2.0, Yc - L/2.0},
        {Xc + L/2.0, Yc - L/2.0},
        {Xc + L/2.0, Yc + L/2.0},
        {Xc - L/2.0, Yc + L/2.0}
    };
    
    for(int i = 0; i < 4; i++) {
        Punct p_start = colturile[i];
        Punct p_end = colturile[(i + 1) % 4];
        
        for(int j = 0; j <= rezolutie; j++) {
            double t = (double)j / rezolutie;
            double px = p_start.x + (p_end.x - p_start.x) * t;
            double py = p_start.y + (p_end.y - p_start.y) * t;
            
            if(i == 0) py += curbura * sin(t * PI);
            else if(i == 1) px -= curbura * sin(t * PI);
            else if(i == 2) py -= curbura * sin(t * PI);
            else if(i == 3) px += curbura * sin(t * PI);
            
            puncte.push_back({px, py});
        }
    }
    
    return puncte;
}

// fct pentru translatarea
void translateazaFigura(Poligon& p, int dx, int dy) {
    for (auto& pct : p) {
        pct.x += dx;
        pct.y += dy;
    }
}

// fctie rotirea intregii scene
void rotesteScena(vector<Poligon>& scena, double unghi_grade, int px, int py) {
    double rad = unghi_grade * PI / 180.0;
    for (auto& poli : scena) {
        for (auto& pct : poli) {
            double x_rel = pct.x - px;
            double y_rel = pct.y - py;
            pct.x = px + (x_rel * cos(rad) - y_rel * sin(rad));
            pct.y = py + (x_rel * sin(rad) + y_rel * cos(rad));
        }
    }
}

void deseneazaPoligon(const Poligon& p, int culoare) {
    int n = p.size();
    int* coord = new int[2 * (n + 1)];
    
    for(int i = 0; i < n; i++) {
        coord[2*i] = (int)p[i].x;
        coord[2*i+1] = (int)p[i].y;
    }
    coord[2*n] = coord[0];
    coord[2*n+1] = coord[1];
    
    setcolor(culoare);
    drawpoly(n + 1, coord);
    delete[] coord;
}

int main() {
    initwindow(800, 600, "Multiplicare si Rotire Scena");
    
    vector<Poligon> scena;
    int pivotX = 400, pivotY = 300;
    
    Poligon figura1 = genereazaFiguraStandard(300, 300, 150);
    scena.push_back(figura1);
    
    Poligon figura2 = figura1;
    //translatie mutata sa nu fie suprapusa
    translateazaFigura(figura2, 200, 0);
    scena.push_back(figura2);
    
    for(const auto& f : scena) {
        deseneazaPoligon(f, WHITE);
    }
    
    //rotire
    rotesteScena(scena, 20, pivotX, pivotY);
    
    for(const auto& f : scena) {
        deseneazaPoligon(f, GREEN);
    }
    
    outtextxy(20, 20, (char*)"Alb: Originale (una multiplicata din alta) | Verde: Scena rotita 20 deg");
    
    getch();
    closegraph();
    return 0;
}