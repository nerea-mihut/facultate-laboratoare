#include <graphics.h>
#include <fstream>
#include <vector>
#include <iostream>
#include <string>

using namespace std;

#define OFFSET_X 70
#define OFFSET_Y 400

int main() {
    ifstream fisier("date.txt");
    vector<double> date;
    double val;
    
    while (fisier >> val) {
        date.push_back(val);
    }
    fisier.close();
    
    if (date.empty()) {
        return 1;
    }
    
    int start_index = 0;
    int view_size = 8;
    double scal_y = 4.0; //scalare initiala
    
    initwindow(900, 600, "Grafic Curs Valutar - Interactiv");
    
    while (true) {
        cleardevice();
        
        //axele
        setcolor(WHITE);
        line(OFFSET_X, OFFSET_Y, OFFSET_X, 50);
        line(OFFSET_X, OFFSET_Y, 850, OFFSET_Y);
        
        outtextxy(10, 10, (char*)"Navigare: SAGETI | Zoom: +/- | Vertical: W/S | Iesire: ESC");
        outtextxy(OFFSET_X - 50, 40, (char*)"Pret");
        outtextxy(860, OFFSET_Y, (char*)"Timp");
        
        float dist_x = 750.0 / (view_size > 1 ? view_size - 1 : 1);
        
        for (int i = 0; i < view_size; i++) {
            int current_idx = start_index + i;
            if (current_idx >= date.size()) {
                break;
            }
            
            int x = OFFSET_X + (int)(i * dist_x);
            int y = OFFSET_Y - (int)(date[current_idx] * scal_y);
            
            setcolor(LIGHTGRAY);
            line(x, OFFSET_Y, x, OFFSET_Y + 10); // linie de gradatie
            
            char timp_text[10];
            sprintf(timp_text, "%d", current_idx + 1);
            outtextxy(x - 5, OFFSET_Y + 15, timp_text);
            
            setcolor(YELLOW);
            circle(x, y, 4);
            floodfill(x, y, YELLOW);
            
            char val_text[20];
            sprintf(val_text, "%.1f", date[current_idx]);
            outtextxy(x - 10, y - 25, val_text);
            
            if (i > 0) {
                int x_prev = OFFSET_X + (int)((i - 1) * dist_x);
                int y_prev = OFFSET_Y - (int)(date[current_idx - 1] * scal_y);
                setcolor(GREEN);
                line(x_prev, y_prev, x, y);
            }
        }
        
        // Logica taste
        int tasta = getch();
        if (tasta == 27) {
            break;
        }
        if (tasta == 0 || tasta == 224) {
            tasta = getch();
            if (tasta == 77 && start_index + view_size < date.size()) {
                start_index++;
            }
            if (tasta == 75 && start_index > 0) {
                start_index--;
            }
        } else if (tasta == '+') {
            if (view_size > 2) {
                view_size--;
            }
        } else if (tasta == '-') {
            if (view_size < date.size()) {
                view_size++;
            }
        } else if (tasta == 'w' || tasta == 'W') {
            scal_y *= 1.2;
        } else if (tasta == 's' || tasta == 'S') {
            scal_y /= 1.2;
        }
    }
    
    closegraph();
    return 0;
}