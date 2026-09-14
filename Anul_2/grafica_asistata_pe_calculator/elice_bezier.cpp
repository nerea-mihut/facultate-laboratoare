#include <winbgim.h>
#include <cmath>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

struct Punct {
    float x, y;
};

// cal pct pe curba Bezier cubica
Punct getBezier(Punct p0, Punct p1, Punct p2, Punct p3, float t) {
    float invT = 1.0f - t;
    Punct p;
    p.x = pow(invT, 3) * p0.x + 3 * pow(invT, 2) * t * p1.x + 3 * invT * pow(t, 2) * p2.x + pow(t, 3) * p3.x;
    p.y = pow(invT, 3) * p0.y + 3 * pow(invT, 2) * t * p1.y + 3 * invT * pow(t, 2) * p2.y + pow(t, 3) * p3.y;
    return p;
}

// rotirea in jurul centrului
Punct rotate(Punct p, float angle) {
    float rad = angle * M_PI / 180.0;
    return { p.x * cos(rad) - p.y * sin(rad), p.x * sin(rad) + p.y * cos(rad) };
}

void deseneazaPala(int cx, int cy, float unghiRotatie) {
    Punct p0 = {0, 0};    // baza
    Punct p1 = {40, 60};  // curb de iesire
    Punct p2 = {160, 90}; // lat max exterior
    Punct p3 = {220, 0};  // vf palei
    
    Punct p4 = {160, -90}; // curb de intoarcere
    Punct p5 = {40, -60};  // intrare inapoi in centru
    
    int pts[200];
    int n = 0;
    
    for (float t = 0; t <= 1.0; t += 0.05) {
        Punct p = rotate(getBezier(p0, p1, p2, p3, t), unghiRotatie);
        pts[n++] = cx + (int)p.x;
        pts[n++] = cy - (int)p.y;
    }
    
    for (float t = 0; t <= 1.0; t += 0.05) {
        Punct p = rotate(getBezier(p3, p4, p5, p0, t), unghiRotatie);
        pts[n++] = cx + (int)p.x;
        pts[n++] = cy - (int)p.y;
    }
    
    setcolor(BLACK);
    setfillstyle(SOLID_FILL, WHITE);
    drawpoly(n / 2, pts);
}

int main() {
    initwindow(800, 600, "Elice 3 Pale Bezier - Implicit");
    
    float unghiAnimatie = 0;
    int nrPale = 3;
    
    while (!kbhit()) {
        setvisualpage(1 - getactivepage());
        setbkcolor(WHITE);
        cleardevice();
        
        int centrulX = getmaxx() / 2;
        int centrulY = getmaxy() / 2;
        
        // palele la 120 grd
        for (int i = 0; i < nrPale; i++) {
            float unghiPala = unghiAnimatie + (i * 360.0 / nrPale);
            deseneazaPala(centrulX, centrulY, unghiPala);
        }
        
        // Punct central discret
        setcolor(BLACK);
        circle(centrulX, centrulY, 2);
        
        setactivepage(1 - getactivepage());
        ungahiAnimatie += 4; // viteza de rotatie (modifica valoarea pentru mai rapid/lent)
        delay(15);
    }
    
    closegraph();
    return 0;
}