# Citire din Fișier și Afișare în JList (Arhitectura MVC)

Acest proiect rezolvă cerința citirii datelor dintr-un fișier text (`persoane.txt`) și afișării acestora într-o componentă vizuală de tip listă (`JList`), respectând șablonul de proiectare Model-View-Controller.

## Structura Arhitecturii
* **`StudentModel.java`** - Se ocupă de partea de date. Citește linie cu linie din fișierul `persoane.txt` (aflat în rădăcina proiectului) și returnează o colecție de tip `List<String>`.
* **`StudentView.java`** - Reprezintă interfața grafică. Folosește un `DefaultListModel` pentru a reține datele și un `JList` inclus într-un `JScrollPane` pentru a le afișa pe ecran.
* **`StudentController.java`** - Leagă modelul de interfață. Preia lista de studenți generată de model și o injectează în componenta vizuală.
* **`Main.java`** - Punctul de intrare care asamblează componentele și pornește aplicația pe firul de execuție specific Swing.

## Cum se rulează
Creați un fișier `persoane.txt` în folderul principal al proiectului, adăugați câteva nume pe rânduri separate, apoi rulați clasa `Main.java`.