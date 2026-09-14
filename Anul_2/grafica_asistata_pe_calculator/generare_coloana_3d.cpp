#include <stdlib.h>
#include <stdio.h>

struct POINT3D {
    double x, y, z;
};

void genereazaColoana() {
    int nrModule = 4;
    int nivele = nrModule * 2 + 1;
    int totalPuncte = nivele * 4;
    struct POINT3D p[200];
    double rMare = 30.0;
    double rMica = 10.0;
    double inaltimePas = 20.0;
    
    // 1. Calcul puncte
    for (int i = 0; i < nivele; i++) {
        double y = i * inaltimePas;
        double r = (i % 2 == 0) ? rMica : rMare;
        int off = i * 4;
        p[off + 0] = (struct POINT3D){-r, y, -r};
        p[off + 1] = (struct POINT3D){ r, y, -r};
        p[off + 2] = (struct POINT3D){ r, y,  r};
        p[off + 3] = (struct POINT3D){-r, y,  r};
    }
    
    FILE* f = fopen("corp3d.txt", "w");
    if (!f) {
        printf("Eroare la crearea fisierului!\n");
        return;
    }
    
    // Scriem DOAR numarul de puncte
    fprintf(f, "%d", totalPuncte);
    
    // 2. Coordonate
    for (int i = 0; i < totalPuncte; i++) {
        fprintf(f, "\n%.2f %.2f %.2f", p[i].x, p[i].y, p[i].z);
    }
    
    // 3. Muchii
    for (int i = 0; i < nivele; i++) {
        int curent = i * 4;
        // Muchii orizontale (inchidem patratul)
        for (int j = 0; j < 4; j++) {
            fprintf(f, "\n%d %d", curent + j, curent + ((j + 1) % 4));
        }
        // Muchii verticale
        if (i < nivele - 1) {
            int urmator = (i + 1) * 4;
            for (int j = 0; j < 4; j++) {
                fprintf(f, "\n%d %d", curent + j, urmator + j);
            }
        }
    }
    
    fclose(f);
}

int main() {
    genereazaColoana();
    printf("Fisierul corp3d.txt a fost generat.\n");
    printf("Apasati Enter pentru a lansa reprezentarea...\n");
    getchar(); // Pauza sa vezi mesajul
    
    // Executam vizualizatorul
    system("reprezentare.exe");
    return 0;
}