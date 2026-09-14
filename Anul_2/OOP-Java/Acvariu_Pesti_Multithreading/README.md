# Simulare Acvariu cu Pești (Java Swing & Threads)

Acest proiect este o aplicație vizuală care simulează un acvariu. Îndeplinește cerința de a desena mai mulți pești care se deplasează independent pe ecran de la stânga la dreapta și își schimbă direcția atunci când se lovesc de marginile ferestrei.

## Concepte Tehnice Implementate

* **Multithreading Independent:** Fiecare pește din acvariu este un obiect de tip `PesteMecanica` ce implementează interfața `Runnable`. La pornire, se creează un `Thread` separat pentru fiecare pește, permițându-le să înoate simultan, la viteze diferite, fără să blocheze interfața.
* **Detectarea Coliziunilor (Bouncing):** În bucla `while(true)` a fiecărui fir de execuție, se verifică coordonata `x` a peștelui raportată la lățimea panoului (`panou.getWidth()`). Când atinge o margine, variabila de direcție se inversează (`directie = -1` sau `1`).
* **Desenare Dinamică (Graphics):** Peștele își schimbă orientarea vizuală (coada se desenează pe partea opusă) în funcție de direcția de mers. Redesenarea se face constant prin apelarea metodei `repaint()` de către fiecare thread.
* **Integrare GUI Designer:** Un `JPanel` custom (`PanelPesti`) este injectat programatic în interiorul unui panou principal creat cu IntelliJ GUI Designer.

## Structura Fișierelor
* **`Acvariu.java` / `Acvariu.form`** - Clasa de legătură pentru interfața generată automat.
* **`PanelPesti.java`** - Panoul de desen (`Canvas-ul`) care randează fundalul și lista de pești.
* **`PesteMecanica.java`** - Logica de mișcare, coordonatele și desenarea individuală a unui pește.
* **`Main.java`** - Punctul de intrare care inițializează și afișează fereastra principală.

## Cum se rulează
Se deschide proiectul în **IntelliJ IDEA** (necesar pentru a citi fișierul `.form`). Se execută clasa `Main.java` pentru a deschide fereastra și a porni animația peștilor.