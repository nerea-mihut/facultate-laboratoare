# Joc "Circle Clicker" (Java Swing, Threads & Serializare)

Acest proiect este un mini-joc interactiv dezvoltat în Java Swing. Scopul jocului este de a da click pe cercurile care apar pe ecran înainte ca acestea să se micșoreze și să dispară, acumulând astfel puncte.

## Concepte Tehnice Implementate

* **Multithreading & Concurrency:** Jocul rulează o buclă principală (game loop) pe un fir de execuție separat (`Thread`) care actualizează dimensiunea cercurilor și forțează redesenarea la fiecare 80ms. Pentru a preveni erorile de tip `ConcurrentModificationException` (când utilizatorul dă click în timp ce thread-ul din fundal modifică lista), se utilizează o colecție thread-safe: `CopyOnWriteArrayList`.
* **Serializare (Salvare/Încărcare):** Starea jocului (lista de cercuri și scorul curent) poate fi salvată într-un fișier binar (`salvare_joc.dat`) utilizând `ObjectOutputStream` și restaurată cu `ObjectInputStream`. Clasa `Cerc` implementează interfața `Serializable`.
* **Matematică & Hitbox-uri:** Detectarea click-urilor pe cercuri nu se face prin forme dreptunghiulare (bounding boxes), ci calculând distanța matematică dintre coordonatele click-ului și centrul cercului, verificând dacă aceasta este mai mică sau egală cu raza cercului.
* **Grafică Dinamică:** Generare de culori, dimensiuni și viteze aleatoare folosind clasa `Random` și randarea customizată prin metoda `paintComponent()`.

## Structura Proiectului
* **`Main.java`** - Punctul de intrare în aplicație.
* **`Joc.java`** - Clasa principală care gestionează logica jocului, firele de execuție, evenimentele de mouse și procesul de serializare/deserializare.
* **`Cerc.java`** - Modelul obiectului grafic (stochează coordonatele, culoarea, viteza de micșorare și logica de intersecție).
* **`Joc.form`** - Fișierul de layout generat de IntelliJ IDEA GUI Designer.

## Cum se rulează
Deschideți proiectul în IntelliJ IDEA. Rulați clasa `Main.java`. Jocul va porni automat, iar butoanele din dreapta pot fi folosite pentru a pune pauză, a salva progresul sau a încărca o salvare anterioară.