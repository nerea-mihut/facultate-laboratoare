#include <winbgim.h>
#include <stdio.h>
#include <math.h>

#define NRN 10
#define PI 3.141592

typedef struct {
    int x, y;
} NOD;

// stea
NOD stea[NRN] = {
    {0, -30}, {7, -10}, {30, -10}, {12, 5},
    {20, 30}, {0, 15}, {-20, 30}, {-12, 5}, {-30, -10}, {-7, -10}
};

void Dsn(int xc, int yc, double rot) {
    int i;
    double co = cos(rot), si = sin(rot);
    NOD tmp[NRN + 1];
    
    for (i = 0; i < NRN; i++) {
        int rx = stea[i].x * co - stea[i].y * si;
        int ry = stea[i].x * si + stea[i].y * co;
        tmp[i].x = rx + xc;
        tmp[i].y = ry + yc;
    }
    
    tmp[NRN] = tmp[0];
    moveto(tmp[0].x, tmp[0].y);
    
    for (i = 1; i <= NRN; i++) {
        lineto(tmp[i].x, tmp[i].y);
    }
}

int main() {
    initwindow(800, 600, "Circuit Vizibil - Animatie Stea");
    
    int xo, yo;
    double u;
    
    setcolor(WHITE);
    for(yo = 450; yo > 150; yo -= 10) {
        putpixel(100, yo, WHITE);
    }
    for(u = PI; u > 0; u -= 0.1) {
        putpixel(250 + 150 * cos(u), 150 - 150 * sin(u), WHITE);
    }
    for(yo = 150; yo < 450; yo += 10) {
        putpixel(400, yo, WHITE);
    }
    
    // inchide circuit
    for(u = 0; u < PI; u += 0.1) {
        putpixel(250 + 150 * cos(u), 450 + 100 * sin(u), WHITE);
    }
    
    setwritemode(XOR_PUT);
    // animatie fluida
    do {
        // linie sus
        for (yo = 450; yo > 150; yo -= 5) {
            if (kbhit()) break;
            Dsn(100, yo, 0);
            delay(25);
            Dsn(100, yo, 0);
        }
        
        // arc dreapta
        for (u = PI; u > 0; u -= 0.05) {
            if (kbhit()) break;
            xo = 250 + 150 * cos(u);
            yo = 150 - 150 * sin(u);
            Dsn(xo, yo, PI - u);
            delay(25);
            Dsn(xo, yo, PI - u);
        }
        
        // Linie jos
        for (yo = 150; yo < 450; yo += 5) {
            if (kbhit()) break;
            Dsn(400, yo, PI);
            delay(25);
            Dsn(400, yo, PI);
        }
        
        // reintoarcere la start
        for (u = 0; u < PI; u += 0.05) {
            if (kbhit()) break;
            xo = 250 + 150 * cos(u);
            yo = 450 + 100 * sin(u);
            Dsn(xo, yo, u);
            delay(25);
            Dsn(xo, yo, u);
        }
        
        if (kbhit()) break;
    } while (1);
    
    closegraph();
    return 0;
}