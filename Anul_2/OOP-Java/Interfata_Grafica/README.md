# Interfață Grafică Formular (Java Swing)

Aplicație desktop simplă pentru preluarea și gestionarea datelor dintr-un formular. Interfața a fost construită direct din GUI Designer-ul de la IntelliJ IDEA.

## Ce face codul?
* **Preluare date:** Citește datele introduse în câmpurile text, butoanele radio (tip locuință, gen) și checkbox-uri (bursier).
* **Validare dată:** Convertește textul introdus pentru data nașterii direct în format `LocalDate`. Dacă formatul (AAAA-LL-ZZ) e greșit, aruncă o eroare vizuală pe ecran (`JOptionPane`).
* **Sistem de căutare:** Butonul "Caută" verifică dacă numele introdus există deja în listă (`ArrayList`) și, dacă îl găsește, deschide un pop-up cu toate datele persoanei.

## Fișierele proiectului
* **`Lab6.java`** - Aici este scrisă toată logica în spate: ce se întâmplă când dai click pe "Adaugă" sau "Caută", popularea listei și validările.
* **`Lab6.form`** - Fișierul generat automat de IntelliJ care reține tot designul vizual (poziția butoanelor, dimensiunea ferestrei etc.). *Notă: Fără acest fișier, interfața grafică nu se poate încărca sau edita vizual în IDE.*

## Cum se rulează
Proiectul trebuie deschis în **IntelliJ IDEA** (pentru a recunoaște corect fișierul `.form`). Se rulează direct metoda `main` din clasa `Lab6.java` pentru a deschide fereastra aplicației.