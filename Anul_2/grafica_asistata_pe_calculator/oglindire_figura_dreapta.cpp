#include <stdio.h>
#include <winbgim.h>
#include <math.h>

#define PI 3.141592
#define N 4
#define NMAX 3

// coord polig si diag
double poli_orig[] = {150, 100, 300, 120, 320, 250, 120, 220, 150, 100};
double diag_orig[] = {150, 100, 320, 250};
double poli_trans[10], diag_trans[4];

void desen_obiect(double poly[], double diag[], int culoare);
void inmult(double A[3][3], double B[3][3]);
void invers(int n, double a[3][3], double eps, double b[3][3], double *det_a, int *err);

int main() {
    int i, e;
    double eps = exp(-9), d, det;
    double P[N][3], L[2][3];
    double M[3][3] = {{1,0,0}, {0,1,0}, {0,0,1}};
    int x1 = 100, y1 = 450, x2 = 600, y2 = 100;
    
    initwindow(800, 600);
    
    for (i = 0; i < N; i++) {
        P[i][0] = poli_orig[2*i];
        P[i][1] = poli_orig[2*i+1];
        P[i][2] = 1;
    }
    for (i = 0; i < 2; i++) {
        L[i][0] = diag_orig[2*i];
        L[i][1] = diag_orig[2*i+1];
        L[i][2] = 1;
    }
    
    //cal matricea de oglindire fata de dreapta oarecare
    d = sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2));
    double sinu = (y2 - y1) / d;
    double cosu = (x2 - x1) / d;
    
    double T[3][3] = {{1,0,0}, {0,1,0}, {(double)-x1, (double)-y1, 1}};
    double R[3][3] = {{cosu, -sinu, 0}, {sinu, cosu, 0}, {0, 0, 1}};
    double O[3][3] = {{1, 0, 0}, {0, -1, 0}, {0, 0, 1}};
    double Ri[3][3], Ti[3][3];
    
    invers(3, R, eps, Ri, &det, &e);
    invers(3, T, eps, Ti, &det, &e);
    
    inmult(M, T);
    inmult(M, R);
    inmult(M, O);
    inmult(M, Ri);
    inmult(M, Ti);
    
    // transformarea pt a obt fig2
    for (i = 0; i < N; i++) {
        double x = P[i][0], y = P[i][1];
        poli_trans[2*i] = x * M[0][0] + y * M[1][0] + M[2][0];
        poli_trans[2*i+1] = x * M[0][1] + y * M[1][1] + M[2][1];
    }
    poli_trans[2*N] = poli_trans[0];
    poli_trans[2*N+1] = poli_trans[1];
    
    for (i = 0; i < 2; i++) {
        double x = L[i][0], y = L[i][1];
        diag_trans[2*i] = x * M[0][0] + y * M[1][0] + M[2][0];
        diag_trans[2*i+1] = x * M[0][1] + y * M[1][1] + M[2][1];
    }
    
    setcolor(YELLOW);
    line(x1, y1, x2, y2); //dreapta
    
    desen_obiect(poli_orig, diag_orig, WHITE);
    desen_obiect(poli_trans, diag_trans, WHITE); // oglindire
    
    getch();
    closegraph();
    return 0;
}

void desen_obiect(double poly[], double diag[], int culoare) {
    int P_int[10], D_int[4];
    setcolor(culoare);
    for (int i = 0; i < 10; i++) P_int[i] = (int)poly[i];
    for (int i = 0; i < 4; i++) D_int[i] = (int)diag[i];
    drawpoly(5, P_int);
    line(D_int[0], D_int[1], D_int[2], D_int[3]);
}

void inmult(double A[3][3], double B[3][3]) {
    double X[3][3];
    for (int i = 0; i < 3; i++)
        for (int j = 0; j < 3; j++) {
            X[i][j] = 0;
            for (int k = 0; k < 3; k++)
                X[i][j] += A[i][k] * B[k][j];
        }
    for (int i = 0; i < 3; i++)
        for (int j = 0; j < 3; j++)
            A[i][j] = X[i][j];
}

void invers(int n, double a[3][3], double eps, double b[3][3], double *det_a, int *err) {
    int i, j, k, pozmax;
    double amax, aux, X[3][3];
    
    for (i = 0; i < n; i++)
        for (j = 0; j < n; j++)
            X[i][j] = a[i][j];
            
    for (i = 0; i < n; i++)
        for (j = 0; j < n; j++)
            b[i][j] = (i == j) ? 1.0 : 0.0;
            
    *det_a = 1.0;
    k = 0;
    *err = 0;
    
    while ((k < n) && (*err == 0)) {
        amax = fabs(X[k][k]);
        pozmax = k;
        for (i = k + 1; i < n; i++)
            if (fabs(X[i][k]) > amax) {
                amax = fabs(X[i][k]);
                pozmax = i;
            }
            
        if (k != pozmax) {
            for (j = 0; j < n; j++) {
                aux = X[k][j];
                X[k][j] = X[pozmax][j];
                X[pozmax][j] = aux;
                aux = b[k][j];
                b[k][j] = b[pozmax][j];
                b[pozmax][j] = aux;
            }
            *det_a = -*det_a;
        }
        
        if (fabs(X[k][k]) < eps)
            *err = 1;
        else {
            *det_a *= X[k][k];
            aux = X[k][k];
            for (j = 0; j < n; j++) {
                X[k][j] /= aux;
                b[k][j] /= aux;
            }
            for (i = 0; i < n; i++)
                if (i != k) {
                    aux = X[i][k];
                    for (j = 0; j < n; j++) {
                        X[i][j] -= X[k][j] * aux;
                        b[i][j] -= b[k][j] * aux;
                    }
                }
        }
        k++;
    }
}