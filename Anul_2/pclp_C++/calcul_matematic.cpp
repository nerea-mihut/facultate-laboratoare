#include <iostream>
using namespace std;

int main() {
    int date[] = {2, 22, 9};

    int a = date[0];
    int b = date[1];
    int c = date[2];

    int t1= a*b;
    int t2 = 10* a;
    int e=  t1 + t2 -c;

    cout<< "e =" << e;
    return 0;
}