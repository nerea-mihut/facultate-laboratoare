#include <winbgim.h>
#include <vector>
#include <math.h>

using namespace std;

#define PI 3.141592

struct Punct {
    double x, y;
};

Punct transforma(Punct p, double Sx, double Sy, double unghi, int cx, int cy, int dx, int dy) {
    double rad = unghi * PI / 180.0;
    double x_sc = (p.x + dx) * Sx;
    double y_sc = (p.y + dy) * Sy;
    Punct p_f;
    p_f.x = cx + (x_sc - cx) * cos(rad) - (y_sc - cy) * sin(rad);
    p_f.y = cy + (x_sc - cx) * sin(rad) + (y_sc - cy) * cos(rad);
    return p_f;
}

void deseneazaHarta(int dx, int dy, double Sx, double Sy, double unghi, int x_min, int y_min) {
    vector<vector<Punct>> obiecte = {
        {{50, 250}, {750, 250}, {750, 350}, {50, 350}},
        {{150, 100}, {300, 100}, {300, 200}, {150, 200}},
        {{500, 380}, {650, 380}, {700, 500}, {450, 500}}
    };
    int culori[] = {DARKGRAY, BLUE, BROWN};
    
    for (size_t i = 0; i < obiecte.size(); i++) {
        int n = obiecte[i].size();
        int* p_bgi = new int[2 * (n + 1)];
        for (int j = 0; j < n; j++) {
            Punct p_t = transforma(obiecte[i][j], Sx, Sy, unghi, 400, 300, dx, dy);
            p_bgi[2 * j] = (int)p_t.x - x_min;
            p_bgi[2 * j + 1] = (int)p_t.y - y_min;
        }
        p_bgi[2 * n] = p_bgi[0];
        p_bgi[2 * n + 1] = p_bgi[1];
        setcolor(culori[i]);
        setfillstyle(SOLID_FILL, culori[i]);
        fillpoly(n + 1, p_bgi);
        delete[] p_bgi;
    }
}

int main() {
    initwindow(800, 600, "GMaps: Navigare Fara Glitch");
    
    int x_min = 200, y_min = 150, x_max = 600, y_max = 450;
    int dx = 0, dy = 0;
    double Sx = 1.0, Sy = 1.0, unghi = 0;
    int pagina_vizibila = 0;
    
    while (1) {
        setactivepage(1 - pagina_vizibila);
        cleardevice();
        
        setviewport(0, 0, 800, 600, 0);
        setcolor(WHITE);
        rectangle(x_min - 1, y_min - 1, x_max + 1, y_max + 1);
        outtextxy(10, 10, (char*)"Navigare: W,A,S,D | R-Rotire | Q,E-Scalare | ESC: Iesire");
        
        setviewport(x_min, y_min, x_max, y_max, 1);
        deseneazaHarta(dx, dy, Sx, Sy, unghi, x_min, y_min);
        
        setvisualpage(1 - pagina_vizibila);
        pagina_vizibila = 1 - pagina_vizibila;
        
        if (kbhit()) {
            char t = getch();
            if (t == 27) break;
            if (t == 'w' || t == 'W') dy -= 10;
            if (t == 's' || t == 'S') dy += 10;
            if (t == 'a' || t == 'A') dx -= 10;
            if (t == 'd' || t == 'D') dx += 10;
            if (t == 'r' || t == 'R') unghi += 5;
            if (t == 'q' || t == 'Q') {
                Sx -= 0.1;
                Sy -= 0.1;
            }
            if (t == 'e' || t == 'E') {
                Sx += 0.1;
                Sy += 0.1;
            }
        }
        delay(10);
    }
    
    closegraph();
    return 0;
}