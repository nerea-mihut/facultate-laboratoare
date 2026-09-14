
# Arhivă Proiecte Python - Anul 1

Acest director conține proiectele, laboratoarele și exercițiile algoritmice dezvoltate pe parcursul primului an de studiu. Codul scris reflectă tranziția de la conceptele fundamentale de programare procedurală la programarea orientată pe obiect (OOP), manipularea structurilor de date complexe și dezvoltarea de interfețe grafice (GUI).

Proiectele sunt independente și abordează probleme specifice de logică, optimizare și management al datelor.

## Structura și Descrierea Proiectelor

**1. task_manager_gui.py**
Aplicație desktop completă pentru gestionarea sarcinilor (To-Do List). 
* Permite operațiuni CRUD (Create, Read, Update, Delete) prin intermediul unei interfețe grafice dezvoltate cu biblioteca Tkinter. 
* Implementează persistența datelor prin salvarea și încărcarea automată a stării aplicației într-un fișier JSON local.
* Include funcționalități de validare a input-ului (format dată, câmpuri obligatorii), sistem de căutare internă și export de date în format CSV.

**2. coin_change_dp.py**
Implementarea problemei clasice „Coin Change” adaptată la un scenariu de business real (simularea unei case de marcat).
* Rezolvă limitările abordării de tip Greedy (care poate eșua în obținerea restului exact) prin utilizarea Programării Dinamice (Dynamic Programming).
* Calculează combinația optimă de bancnote pentru a oferi restul corect, ținând cont simultan de un stoc finit și variabil al resurselor disponibile.

**3. cnp_database_simulation.py**
Analiza performanței și implementarea unei structuri de date de tip Hash Table (tabelă de dispersie).
* Include un generator de date de test (mock data) capabil să creeze volume mari de CNP-uri valide, respectând probabilitățile demografice și regulile oficiale de generare (coduri de județ, an, sex, cifră de control).
* Evaluează eficiența funcției de hashing custom prin numărarea coliziunilor și calcularea mediei iterațiilor necesare pentru regăsirea unui set de înregistrări.

**4. password_cracker.py**
Exercițiu de securitate și complexitate algoritmică.
* Demonstrează decriptarea unui hash SHA-256 predefinit prin metoda forței brute (Brute Force).
* Utilizează algoritmul de Backtracking pentru a genera recursiv toate combinațiile posibile de parole care respectă un set strict de reguli de complexitate (număr exact de litere mari, cifre, caractere speciale și litere mici).

**5. cantina_inventory.py**
Modelarea unui flux operațional folosind structuri de date fundamentale abstracte.
* Simulează procesarea comenzilor dintr-o cantină studențească aplicând corect principiile de Coadă (FIFO - First In, First Out) pentru servirea succesivă a clienților.
* Implementează principiul de Stivă (LIFO - Last In, First Out) pentru managementul și eliberarea tăvilor de servire.

**6. simulare_algoritmi_sortare.py**
Analiza și compararea performanței metodelor de organizare a datelor.
* Scriptul implementează și execută diferiți algoritmi de sortare pentru a evidenția modul în care aceștia manipulează listele de date și pentru a testa eficiența lor pe seturi variabile de informații.

**7. spanzuratoarea.py**
Implementarea jocului clasic de consolă.
* Proiectul se concentrează pe controlul strict al fluxului de execuție, gestionarea stărilor jocului (numărul de încercări, litere ghicite), prelucrarea input-ului valid de la utilizator și manipularea dinamică a șirurilor de caractere ascunse.

**8. string_manipulation.py**
Modul de procesare tehnică a textelor.
* Demonstrează utilizarea eficientă a metodelor încorporate din limbajul Python pentru secționarea șirurilor (slicing), curățarea de caractere nedorite, formatarea cazurilor literelor și inversarea sintaxei pe baza unor cerințe logice specifice.

## Cerințe de Execuție
Toate scripturile sunt concepute pentru a rula în mediul Python 3.8 sau o versiune ulterioară. Acestea utilizează exclusiv biblioteci standard (precum `tkinter`, `json`, `hashlib`, `csv`, `random`), nefiind necesară instalarea unor pachete externe (fără `pip install`).
