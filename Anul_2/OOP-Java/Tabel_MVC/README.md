# Interfață Grafică JTable - Arhitectura MVC

Aplicație desktop în Java Swing pentru gestionarea și afișarea datelor studenților într-un tabel (`JTable`). Acest proiect este implementat scriind interfața grafică manual din cod (fără GUI Designer), respectând strict **șablonul de proiectare MVC (Model-View-Controller)**.

## Structura Arhitecturii MVC

* **`TabelStudentiModel.java` (Model)** - Gestionează datele și starea aplicației. Folosește un `DefaultTableModel` care ține în memorie structura tabelului (capetele de tabel: Nume, Prenume, An, Notă) și rândurile adăugate.
* **`TabelStudentiView.java` (View)** - Componenta vizuală. Definește fereastra (`JFrame`), tabelul, scroll-ul, câmpurile text și butonul. Preia datele de la utilizator, dar nu ia decizii. Folosește `BorderLayout` și `FlowLayout` pentru aranjarea elementelor.
* **`TabelStudentiController.java` (Controller)** - "Creierul" aplicației. Ascultă click-ul de pe butonul din View, preia textul, îl validează (Anul 1-4, Nota 1-10) și, dacă datele sunt corecte, dă comandă Modelului să actualizeze tabelul. Dacă apar erori, afișează mesaje prin `JOptionPane`.
* **`MainTabel.java` (Entry Point)** - Clasa principală care inițializează și conectează Modelul, View-ul și Controller-ul, pornind interfața grafică pe un fir de execuție separat (`SwingUtilities.invokeLater`).

## Validări Implementate
- Verificare câmpuri goale (nume, prenume).
- Validare logică pentru numere (an între 1-4, notă între 1-10).
- Prinderea excepțiilor de tip `NumberFormatException` (prevenirea crash-ului dacă se introduc litere în loc de cifre).

## Cum se rulează
Se deschide proiectul în IDE-ul preferat (IntelliJ, Eclipse) și se rulează direct metoda `main` din fișierul **`MainTabel.java`**.