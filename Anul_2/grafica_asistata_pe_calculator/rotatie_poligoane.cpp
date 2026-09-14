#include <winbgim.h>
#include <iostream>
#include <cstdio>
#include <cmath>

#define PI 3.14159265

void rotatePoint(int& x, int& y, int centerX, int centerY, double angle_rad) {
    double tempX = x - centerX;
    double tempY = y - centerY;
    x = centerX + (int)(tempX * cos(angle_rad) - tempY * sin(angle_rad));
    y = centerY + (int)(tempX * sin(angle_rad) + tempY * cos(angle_rad));
}

int main() {
    initwindow(800, 600);
    
    double global_rotation_rad = 20.0 * PI / 180.0;
    int rotation_centerX = 400;
    int rotation_centerY = 300;
    
    int pointsh[14] = {0};
    int nh = 6, Rh = 120, Xh = 400, Yh = 300;
    double rotatieh = PI / 6;
    
    for(int i = 0; i < nh; i++) {
        double alfa = i * 2 * PI / nh + rotatieh;
        int current_x = Xh + Rh * cos(alfa);
        int current_y = Yh + Rh * sin(alfa);
        rotatePoint(current_x, current_y, rotation_centerX, rotation_centerY, global_rotation_rad);
        pointsh[2 * i] = current_x;
        pointsh[2 * i + 1] = current_y;
    }
    pointsh[12] = pointsh[0];
    pointsh[13] = pointsh[1];
    
    setcolor(BLUE);
    drawpoly(nh + 1, pointsh);
    
    int np = 5;
    int rp = 100;
    int xp = 584, yp = 300;
    double rotatiep = PI * 20;
    int pointsp[12] = {0};
    
    for(int i = 0; i < np; i++) {
        double alfa = i * 2 * PI / np + rotatiep;
        int current_x = xp + rp * cos(alfa);
        int current_y = yp + rp * sin(alfa);
        rotatePoint(current_x, current_y, rotation_centerX, rotation_centerY, global_rotation_rad);
        pointsp[2 * i] = current_x;
        pointsp[2 * i + 1] = current_y;
    }
    pointsp[10] = pointsp[0];
    pointsp[11] = pointsp[1];
    
    setcolor(RED);
    drawpoly(np + 1, pointsp);
    
    int dx = -277;
    int dy = -159;
    int xp2 = xp + dx;
    int yp2 = yp + dy;
    double rotatiep2 = 3.334 * PI;
    int pointsp2[12] = {0};
    
    for(int i = 0; i < np; i++) {
        double alfa = i * 2 * PI / np + rotatiep2;
        int current_x = xp2 + rp * cos(alfa);
        int current_y = yp2 + rp * sin(alfa);
        rotatePoint(current_x, current_y, rotation_centerX, rotation_centerY, global_rotation_rad);
        pointsp2[2 * i] = current_x;
        pointsp2[2 * i + 1] = current_y;
    }
    pointsp2[10] = pointsp2[0];
    pointsp2[11] = pointsp2[1];
    
    setcolor(RED);
    drawpoly(np + 1, pointsp2);
    
    int ex = -277;
    int ey = 161;
    int xp3 = xp + ex;
    int yp3 = yp + ey;
    double rotatiep3 = PI * 5.069;
    int pointsp3[12] = {0};
    
    for(int i = 0; i < np; i++) {
        double alfa = i * 2 * PI / np + rotatiep3;
        int current_x = xp3 + rp * cos(alfa);
        int current_y = yp3 + rp * sin(alfa);
        rotatePoint(current_x, current_y, rotation_centerX, rotation_centerY, global_rotation_rad);
        pointsp3[2 * i] = current_x;
        pointsp3[2 * i + 1] = current_y;
    }
    pointsp3[10] = pointsp3[0];
    pointsp3[11] = pointsp3[1];
    
    setcolor(RED);
    drawpoly(np + 1, pointsp3);
    
    while(!kbhit());
    closegraph();
    
    return 0;
}