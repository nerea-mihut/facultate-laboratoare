#include <winbgim.h>
#include <cmath>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

struct Point3D {
    float x, y, z;
};

Point3D rotate(Point3D p, float ax, float ay) {
    float radX = ax * M_PI / 180.0;
    float radY = ay * M_PI / 180.0;
    Point3D res;
    
    float y1 = p.y * cos(radX) - p.z * sin(radX);
    float z1 = p.y * sin(radX) + p.z * cos(radX);
    
    res.x = p.x * cos(radY) + z1 * sin(radY);
    res.y = y1;
    res.z = -p.x * sin(radY) + z1 * cos(radY);
    
    return res;
}

int main() {
    initwindow(800, 600, "Cub Sectionat Wireframe");
    
    // sectionez lat dreapta-sus-fata (100, 100, 100)
    Point3D v[10];
    v[0] = {-100, -100, 100}; // Jos-Stanga-Fata
    v[1] = { 100, -100, 100}; // Jos-Dreapta-Fata
    v[2] = { 100, 40, 100};
    v[3] = { 40, 100, 100};
    v[4] = {-100, 100, 100}; // Sus-Stanga-Fata
    v[5] = {-100, -100, -100}; // Jos-Stanga-Spate
    v[6] = { 100, -100, -100}; // Jos-Dreapta-Spate
    v[7] = { 100, 100, -100}; // Sus-Dreapta-Spate
    v[8] = {-100, 100, -100}; // Sus-Stanga-Spate
    v[9] = { 100, 100, 40};
    
    int edges[15][2] = {
        {0,1}, {1,2}, {2,9}, {9,7}, {7,8}, {8,4}, {4,0},
        {5,6}, {6,1}, {5,0}, {5,8}, {6,2}, {7,3}, {3,4}, {3,9}
    };
    
    float angX = 0, angY = 0;
    
    while (!kbhit()) {
        setvisualpage(1 - getactivepage());
        cleardevice();
        
        setcolor(WHITE);
        for (int i = 0; i < 15; i++) {
            Point3D p1 = rotate(v[edges[i][0]], angX, angY);
            Point3D p2 = rotate(v[edges[i][1]], angX, angY);
            line(400 + p1.x, 300 - p1.y, 400 + p2.x, 300 - p2.y);
        }
        
        setactivepage(1 - getactivepage());
        angX += 1.0;
        angY += 1.5;
        delay(20);
    }
    
    closegraph();
    return 0;
}