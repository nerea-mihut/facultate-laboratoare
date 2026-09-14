# Monitorizare Parcare Supermarket (Java Swing & Threads)

Acest proiect este o aplicație Java care simulează vizual o parcare cu 10 locuri. Aplicația monitorizează în timp real un fișier text și actualizează interfața grafică pe măsură ce date noi (mașini parcate) sunt adăugate în fișier.

## Funcționalități și Concepte Tehnice

* **Multithreading (Fire de execuție):** Se folosește interfața `Runnable` pentru a rula un proces în fundal (`MonitorFisier`). Acesta verifică la fiecare secundă dacă fișierul `parcare.txt` a fost modificat, fără să blocheze interfața grafică.
* **Sincronizare (Thread-Safety):** Actualizarea locurilor de parcare se face printr-o metodă marcată cu `synchronized` (`vopsesteLoc`), prevenind erorile ce pot apărea când firul de fundal și firul principal accesează aceleași resurse.
* **File I/O:** Citirea datelor se face prin `BufferedReader`. Aplicația este capabilă să creeze automat fișierul `parcare.txt` dacă acesta nu există.
* **Desenare Customizată (Graphics):** Reprezentarea parcării se realizează prin suprascrierea metodei `paintComponent(Graphics g)`.

## Structura Fișierelor
* **`Main.java`** - Clasa de bază care inițializează fereastra (`JFrame`) și pornește procesul de monitorizare.
* **`PanelParcare.java`** - Conține logica principală a aplicației (interfața, desenarea și monitorizarea fișierului).
* **`PanelParcare.form`** - Fișierul XML generat de IntelliJ IDEA pentru structura vizuală.

## Cum se rulează și cum apar mașinile
1. Se deschide proiectul în **IntelliJ IDEA** și se rulează `Main.java`. Interfața se va deschide afișând parcarea goală.
2. **Pentru a face mașinile să apară în parcare:**
   * Căutați și deschideți fișierul `parcare.txt` (se generează automat în folderul proiectului).
   * Scrieți pe o linie nouă culoarea dorită și slotul (locul) de parcare, despărțite prin virgulă.
   * **Format:** `culoare, slot`
   * **Exemplu:** `rosu, 3` sau `verde, 7`
3. Imediat ce salvați fișierul text (`Ctrl + S`), thread-ul din spate detectează modificarea și mașina va apărea instantaneu pe slotul respectiv în interfața grafică.