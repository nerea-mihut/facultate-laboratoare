#include <winbgim.h>
#include <math.h>

#define PI 3.14159265

void hxgn(int xo, int yo, int R, int colorOutline) {
    int points[14];
    for (int i = 0; i < 6; i++) {
        double a = PI/6 + i * PI/3;
        points[2*i] = xo + R*cos(a);
        points[2*i+1] = yo + R*sin(a);
    }
    points[12] = points[0];
    points[13] = points[1];
    
    setcolor(colorOutline);
    drawpoly(7, points);
}

int main() {
    initwindow(800, 600);
    
    int xc = 400, yc = 300, r = 60;
    hxgn(xc, yc, r, CYAN);
    
    double d = r * sqrt(3);
    
    for (int i = 0; i < 6; i++) {
        double angle = i * PI / 3;
        int x = xc + d * cos(angle);
        int y = yc + d * sin(angle);
        hxgn(x, y, r, CYAN);
    }
    
    while (!kbhit());
    closegraph();
    
    return 0;
}