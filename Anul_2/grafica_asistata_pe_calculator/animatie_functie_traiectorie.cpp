#include <winbgim.h>
#include <math.h>
#include <stdio.h>

#define NRN 10
#define PI 3.141592

typedef struct {
    int x, y;
} NOD;

// Figura
NOD stea[NRN] = {
    {0, -20}, {5, -7}, {20, -7}, {8, 4}, {14, 20},
    {0, 10}, {-14, 20}, {-8, 4}, {-20, -7}, {-5, -7}
};

void Dsn(int xc, int yc, double rot, int culoare) {
    int i;
    double co = cos(rot), si = sin(rot);
    int puncte[2 * (NRN + 1)];
    
    for (i = 0; i < NRN; i++) {
        puncte[2 * i] = xc + (int)(stea[i].x * co - stea[i].y * si);
        puncte[2 * i + 1] = yc + (int)(stea[i].x * si + stea[i].y * co);
    }
    puncte[2 * NRN] = puncte[0];
    puncte[2 * NRN + 1] = puncte[1];
    
    setcolor(culoare);
    drawpoly(NRN + 1, puncte);
}

int main() {
    initwindow(800, 600, "Animatie cu Traiectorie Vizibila");
    
    int x_min = 200, y_min = 150, x_max = 600, y_max = 450;
    int X_centru = 400, Y_centru = 300;
    double Sy = 200;
    
    setcolor(WHITE);
    rectangle(x_min, y_min, x_max, y_max);
    
    setcolor(DARKGRAY);
    for (double t = 0; t <= 12; t += 0.01) {
        int tx = 50 + (int)(t * 60);
        int ty = Y_centru + (int)(exp(-t/5.0) * sin(t) * Sy);
        if (tx >= 0 && tx < 800) {
            putpixel(tx, ty, DARKGRAY);
        }
    }
    
    setwritemode(XOR_PUT);
    
    while (!kbhit()) {
        for (double t = 0; t <= 12; t += 0.05) {
            if (kbhit()) break;
            
            int xc = 50 + (int)(t * 60);
            int yc = Y_centru + (int)(exp(-t/5.0) * sin(t) * Sy);
            int col = WHITE;
            
            if (xc >= x_min && xc <= x_max && yc >= y_min && yc <= y_max) {
                col = RED;
            }
            
            Dsn(xc, yc, t, col);
            delay(30);
            Dsn(xc, yc, t, col);
        }
    }
    
    closegraph();
    return 0;
}