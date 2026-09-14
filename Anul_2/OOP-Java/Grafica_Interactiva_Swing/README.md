# Aplicație de Desenare Interactivă (Java Swing)

Acest proiect reprezintă o aplicație desktop dezvoltată în Java Swing, concepută pentru desenarea, selectarea și manipularea formelor geometrice pe un canvas (panou de desenare), respectând cerințele unui test practic.

## Funcționalități Implementate

Aplicația bifează următoarele cerințe tehnice:
* **Desenare dinamică la click (`MouseListener`):** La un simplu click pe suprafața de lucru, se desenează figura selectată din meniul lateral (Cerc sau Pătrat), exact la coordonatele cursorului. Culoarea inițială este dictată de butoanele radio (Roșu, Verde, Albastru).
* **Gestiunea stării (Forma selectată):** Sistemul reține mereu care este "ultima figură desenată". Aceasta devine automat obiectul selectat activ și este evidențiată vizual prin schimbarea culorii conturului în negru.
* **Manipularea coordonatelor:** Foloșind butoanele direcționale (Sus, Jos, Stânga, Dreapta), forma selectată curent poate fi mutată pe ecran cu un pas fix (aprox. 5-10 pixeli), forțând redesenarea panoului (`repaint()`).
* **Actualizare proprietăți în timp real:** Selectarea unei alte culori din panoul lateral modifică instant culoarea formei active (selectate), actualizându-i starea.

## Detalii Tehnice
* Interfața folosește containere specifice Swing și layout-uri pentru a separa zona de desenare de zona de butoane.
* Randarea grafică se face prin suprascrierea metodei `paintComponent(Graphics g)` pe panoul principal de desen.
* Evenimentele sunt gestionate prin `ActionListener` (pentru butoane/radio buttons) și `MouseAdapter` (pentru preluarea coordonatelor X, Y la click).

## Cum se rulează
Deschideți proiectul într-un IDE (IntelliJ IDEA / Eclipse) și rulați clasa principală care conține metoda `main`. Interfața se va deschide într-o fereastră nouă.