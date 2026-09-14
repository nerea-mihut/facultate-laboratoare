#include <winbgim.h>
#include <math.h>
#include <vector>
#include <stdio.h>

#define M 30      // Linii
#define N 40      // Coloane
#define LATURA 15
#define OFFSET 50

int ecran[M][N];

void aprindePixel(int j, int i, int culoare) {
    if (i >= 0 && i < M && j >= 0 && j < N) {
        ecran[i][j] = culoare;
        int x1 = OFFSET + j * LATURA;
        int y1 = OFFSET + i * LATURA;
        setfillstyle(SOLID_FILL, culoare);
        bar(x1 + 1, y1 + 1, x1 + LATURA - 1, y1 + LATURA - 1);
    }
}

// Bresenham
void linieBres(int x1, int y1, int x2, int y2, int col) {
    int dx = abs(x2 - x1), dy = abs(y2 - y1);
    int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
    int err = dx - dy;
    
    while(1) {
        aprindePixel(x1, y1, col);
        if (x1 == x2 && y1 == y2) break;
        int e2 = 2 * err;
        if (e2 > -dy) {
            err -= dy;
            x1 += sx;
        }
        if (e2 < dx) {
            err += dx;
            y1 += sy;
        }
    }
}

void parcurgereContur(int r_start, int c_start, int culoare_noua) {
    int r = r_start, c = c_start;
    int dr[] = {-1, -1, 0, 1, 1, 1, 0, -1}; // astea s directiile
    int dc[] = {0, 1, 1, 1, 0, -1, -1, -1};
    int dir = 0;
    
    do {
        aprindePixel(c, r, culoare_noua); // marchez contur
        bool gasit = false;
        
        for (int i = 0; i < 8; i++) {
            int incercare = (dir + i) % 8;
            int nr = r + dr[incercare], nc = c + dc[incercare];
            if (nr >= 0 && nr < M && nc >= 0 && nc < N && ecran[nr][nc] > 0 && ecran[nr][nc] != culoare_noua) {
                r = nr;
                c = nc;
                dir = (incercare + 6) % 8; // reg urmarire
                gasit = true;
                break;
            }
        }
        if (!gasit) break;
        delay(50);
    } while (r != r_start || c != c_start);
}

int main() {
    initwindow(800, 600, "Numarare Poligoane pe Careu");
    
    for(int i = 0; i < M; i++) {
        for(int j = 0; j < N; j++) {
            ecran[i][j] = 0;
        }
    }
    
    setcolor(DARKGRAY);
    for (int i = 0; i <= M; i++) {
        line(OFFSET, OFFSET + i * LATURA, OFFSET + N * LATURA, OFFSET + i * LATURA);
    }
    for (int j = 0; j <= N; j++) {
        line(OFFSET + j * LATURA, OFFSET, OFFSET + j * LATURA, OFFSET + M * LATURA);
    }
    
    // penta
    linieBres(5, 5, 10, 2, RED);
    linieBres(10, 2, 15, 5, RED);
    linieBres(15, 5, 13, 10, RED);
    linieBres(13, 10, 7, 10, RED);
    linieBres(7, 10, 5, 5, RED);
    
    // triunghi
    linieBres(25, 5, 35, 5, GREEN);
    linieBres(35, 5, 30, 15, GREEN);
    linieBres(30, 15, 25, 5, GREEN);
    
    // patratul
    linieBres(10, 20, 20, 20, BLUE);
    linieBres(20, 20, 20, 28, BLUE);
    linieBres(20, 28, 10, 28, BLUE);
    linieBres(10, 28, 10, 20, BLUE);
    
    outtextxy(50, 10, (char*)"Apasati o tasta pentru a numara poligoanele...");
    getch();
    
    // numarare
    int nrPoligoane = 0;
    bool vizitat[M][N] = {false};
    
    for (int i = 0; i < M; i++) {
        for (int j = 0; j < N; j++) {
            if (ecran[i][j] > 0 && ecran[i][j] != YELLOW) {
                nrPoligoane++;
                parcurgereContur(i, j, YELLOW);
            }
        }
    }
    
    char mesaj[50];
    sprintf(mesaj, "S-au gasit %d poligoane in scena.", nrPoligoane);
    setcolor(WHITE);
    outtextxy(50, 520, mesaj);
    
    getch();
    closegraph();
    return 0;
}