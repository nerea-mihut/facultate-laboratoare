#include <winbgim.h>
#include <math.h>
#include <stdio.h>

int main() {
    initwindow(800, 600, "Grafice de functii - Decupare si Scalare");
    
    int x_min = 150, y_min = 100, x_max = 650, y_max = 500;
    setcolor(WHITE);
    rectangle(x_min, y_min, x_max, y_max);
    outtextxy(x_min, y_min - 20, (char*)"DREPTUNGHI DE DECUPARE");
    
    double t;
    double Sx = 200, Sy = 200;
    int X_centru = 400, Y_centru = 300;
    
    setcolor(YELLOW);
    for (t = 0; t <= 5.0; t += 0.01) {
        double x_math = exp(-t) * cos(8 * t);
        double y_math = exp(-t) * sin(8 * t);
        int px = X_centru + (int)(x_math * Sx);
        int py = Y_centru + (int)(y_math * Sy);
        if (px >= x_min && px <= x_max && py >= y_min && py <= y_max) {
            putpixel(px, py, YELLOW);
        }
    }
    
    setcolor(CYAN);
    double Sx2 = 25, Sy2 = 100;
    for (t = 0; t <= 6 * M_PI; t += 0.01) {
        double x_math = t;
        double y_math = exp(-t / 5.0) * sin(t);
        int px = x_min + 20 + (int)(x_math * Sx2);
        int py = Y_centru + (int)(y_math * Sy2);
        if (px >= x_min && px <= x_max && py >= y_min && py <= y_max) {
            putpixel(px, py, CYAN);
        }
    }
    
    getch();
    closegraph();
    return 0;
}