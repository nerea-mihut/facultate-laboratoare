#include <winbgim.h>
#include <iostream>
#include <cstdio>
#include <cmath>

#define PI 3.14159265

int main() {
    initwindow(800, 600);
    
    //hexagon regulat
    int points[14] = {0};
    int poligon[128];
    int i, n = 6, R = 120, Xc = 400, Yc = 300;
    
    for(int i = 0; i < n; i++) {
        double alfa = i * 2 * PI / n;
        points[2 * i] = Xc + R * cos(alfa);
        points[2 * i + 1] = Yc + R * sin(alfa);
    }
    points[12] = points[0];
    points[13] = points[1];
    
    setcolor(YELLOW);
    drawpoly(n + 1, points);
    
    //translatia
    int dx = 100;
    int dy = 50;
    
    for(int i = 0; i < 14; i += 2) {
        points[i] += dx;
        points[i + 1] += dy;
    }
    
    setcolor(RED);
    drawpoly(n + 1, points);
    Xc += dx;
    Yc += dy;
    
    //scalare
    double sx = 1.5, sy = 1.5;
    
    for(int i = 0; i < n + 1; i++) {
        points[2 * i] = Xc + (points[2 * i] - Xc) * sx;
        points[2 * i + 1] = Yc + (points[2 * i + 1] - Yc) * sy;
    }
    
    for(int i = 0; i < 2 * n + 2; i++) {
        poligon[i] = (int)points[i];
    }
    
    setcolor(BLUE);
    drawpoly(n + 1, poligon);
    
    while(!kbhit());
    closegraph();
    return 0;
}