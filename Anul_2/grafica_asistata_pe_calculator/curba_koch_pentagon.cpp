#include <winbgim.h>
#include <math.h>
#include <vector>

#define PI 3.14159265

struct Punct {
    double x, y;
};

void desenKoch(Punct p1, Punct p2, int iteratii) {
    if (iteratii == 0) {
        line((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
        return;
    }
    
    Punct A, B, C;
    // cal pct ce impart seg in 3
    A.x = p1.x + (p2.x - p1.x) / 3.0;
    A.y = p1.y + (p2.y - p1.y) / 3.0;
    C.x = p1.x + 2.0 * (p2.x - p1.x) / 3.0;
    C.y = p1.y + 2.0 * (p2.y - p1.y) / 3.0;
    
    // cal vf
    double unghi = 60.0 * PI / 180.0;
    B.x = A.x + (C.x - A.x) * cos(unghi) + (C.y - A.y) * sin(unghi);
    B.y = A.y - (C.x - A.x) * sin(unghi) + (C.y - A.y) * cos(unghi);
    
    desenKoch(p1, A, iteratii - 1);
    desenKoch(A, B, iteratii - 1);
    desenKoch(B, C, iteratii - 1);
    desenKoch(C, p2, iteratii - 1);
}

int main() {
    initwindow(800, 800, "Curba Koch pe Pentagon - 3 Iteratii");
    
    int cx = 400, cy = 400; // Centrul ecranului
    double raza = 200;
    int laturi = 5;         // Alegem un PENTAGON
    
    std::vector<Punct> varfuri;
    
    // cal vf pentag
    for (int i = 0; i < laturi; i++) {
        double unghi = 2.0 * PI * i / laturi - PI / 2.0;
        Punct p;
        p.x = cx + raza * cos(unghi);
        p.y = cy + raza * sin(unghi);
        varfuri.push_back(p);
    }
    
    setcolor(CYAN);
    
    // aplic curba pt fiecare lat
    for (int i = 0; i < laturi; i++) {
        desenKoch(varfuri[i], varfuri[(i + 1) % laturi], 3); // 3 iterații
    }
    
    getch();
    closegraph();
    return 0;
}