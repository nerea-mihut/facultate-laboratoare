# Sistem de Gestiune și Administrare pentru Salon de Manichiură și Academie de Nail Art

Acesta este depozitul oficial aferent proiectului la disciplina **Baze de Date** (Anul 3, 2026-2027), Facultatea de Automatică și Calculatoare.

Proiectul implementează un sistem informatic complet (bază de date relațională și aplicație grafică desktop în Java Swing) destinat digitalizării și optimizării fluxurilor operaționale, logistice, sanitare și educaționale dintr-un salon de manichiură modern care funcționează simultan ca salon de înfrumusețare și academie de formare profesională în nail art.

---

## Cuprins
1. [Prezentare Generală](#prezentare-generală)
2. [Funcționalități Principale](#funcționalități-principale)
3. [Arhitectura Bazei de Date](#arhitectura-bazei-de-date)
   - [Cele 24 de Tabele Relaționale](#cele-24-de-tabele-relaționale)
   - [Integritate, Constrângeri și Indecși](#integritate-constrângeri-și-indecși)
   - [Normalizare (FN1, FN2, FN3)](#normalizare-fn1-fn2-fn3)
4. [Date de Test și Populare Tranzacțională](#date-de-test-și-populare-tranzacțională)
5. [Interogări SQL și Rapoarte de Analiză (20 de Scripturi)](#interogări-sql-și-rapoarte-de-analiză-20-de-scripturi)
6. [Aplicația Practică Java Swing](#aplicația-practică-java-swing)
   - [Arhitectura Software](#arhitectura-software)
   - [Autentificare și Roluri Dinamice](#autentificare-și-roluri-dinamice)
   - [Modulul Client (ClientDashboard)](#modulul-client-clientdashboard)
   - [Modulul Tehnician (TechnicianDashboard)](#modulul-tehnician-techniciandashboard)
   - [Registrul Digital de Sterilizare cu Martor Foto](#registrul-digital-de-sterilizare-cu-martor-foto)
   - [Managementul Tranzacțiilor ACID](#managementul-tranzacțiilor-acid)
7. [Structura Depozitului](#structura-depozitului)
8. [Ghid de Instalare și Rulare](#ghid-de-instalare-și-rulare)
9. [Autor și Date Academice](#autor-și-date-academice)

---

## Prezentare Generală

În mediul actual al serviciilor de înfrumusețare, numeroase saloane se confruntă cu ineficiențe cauzate de evidența fragmentată pe hârtie sau în tabele Excel disparate: suprapuneri de programări, gestiune deficitară a stocurilor, ignorarea alergiilor declarate de clienți, pierderea evidenței sanitare din registrul de sterilizare și dificultăți în coordonarea cursurilor de perfecționare.

Sistemul rezolvă aceste provocări printr-o arhitectură unificată:
- **Salon de manichiură:** rezervare dinamică a serviciilor în pași de 30 de minute, verificare anti-suprapunere, calcul compus de durate și tarife (serviciu de bază, mărimi, ojă semi / tehnică hybrid cu polygel, design Easy/Mediu/Complex, corecții de arhitectură), colectare feedback calitativ (note 1-5 și recenzii).
- **Academie de Nail Art:** catalog de cursuri pe niveluri, planificare sesiuni cu capacitate limitată la maximum 8 cursanți confirmați, încărcare documente, avans minim, listă de așteptare automată conform principiului FIFO și alertare la eliberarea locurilor.
- **Gestiunea stocurilor:** separare strictă între stocul destinat salonului și stocul pentru academie, evidența mișcărilor (Intrare, Consum, Transfer, Eliminare) și monitorizarea pragurilor minime critice.
- **Registru Sanitar de Sterilizare:** evidență electronică conform normelor DSP (aparat Pupinel la 180°C/160°C sau Autoclav la 134°C/121°C, parametri orari, temperaturi, descriere instrumente, încărcare fotografie martor/test biologic).

---

## Funcționalități Principale

- **Gestiunea Clienților și Personalului:** profiluri detaliate, acreditări, certificări profesionale, alerte medicale de alergii (fără blocare automată a produselor, dar semnalate vizual).
- **Rezervare Inteligentă a Serviciilor:** algoritm de căutare a disponibilității care exclude concediile, programările existente și sesiunile de curs unde angajatul este trainer.
- **Configurare Zilnică Flexibilă a Tehnicianului:** stabilirea zilelor de lucru vs. libere și selecția serviciilor oferite în fiecare zi calendaristică.
- **Registru Digital de Sterilizare:** salvare securizată a imaginilor martor și asocierea responsabilului direct.
- **Rapoarte Manageriale și Contabile:** 20 de interogări SQL optimizate pentru decizii de business, calcul de rest de plată, clienți fideli sau inactivi și încasări lunare.

---

## Arhitectura Bazei de Date

Baza de date relațională se numește **`proiect_salon`**, este implementată pe motorul de stocare tranzacțional **InnoDB** și utilizează setul de caractere **`utf8mb4`** cu colaționarea **`utf8mb4_unicode_ci`**.

### Cele 24 de Tabele Relaționale

1. **`angajat`** — datele comune de identificare, contact, specializare și certificări ale personalului.
2. **`tehnician`** — identifică angajații care prestează servicii în salon (subtip al entității angajat).
3. **`trainer`** — identifică angajații autorizați să predea cursuri în academie (subtip al entității angajat).
4. **`client`** — date de identificare, contact, alergii declarate (`Da`/`Nu`) și detalii text despre alergii.
5. **`utilizator`** — conturi de acces, adrese de e-mail unice, parole criptate (hash SHA-256), roluri (`Client`, `Tehnician`, `Trainer`) și chei străine către `client` sau `angajat`.
6. **`interval_disponibilitate`** — intervale de timp planificate pentru angajați (`Lucru` sau `Absenta`).
7. **`configurare_zi_tehnician`** — configurarea regimului fiecărei zile din calendar pentru tehnician (`Lucru` sau `Libera`).
8. **`serviciu_zi_tehnician`** — selecția granulară a serviciilor active pe care tehnicianul le oferă într-o anumită zi de lucru (`ON DELETE CASCADE`).
9. **`serviciu`** — catalogul general de servicii (denumire, categorie).
10. **`tehnician_serviciu`** — oferta activă/inactivă per tehnician: durata în minute și tariful aplicat.
11. **`programare`** — vizita rezervată: data, ora, client, tehnician, stare (`Confirmata`, `Finalizata`, `Anulata`, `Neprezentare`), motiv anulare.
12. **`programare_serviciu`** — serviciile asociate unei programări, memorând prețul și durata aplicate istoric.
13. **`feedback`** — evaluare (1-5 stele) și comentariu text lăsate de client după finalizarea programării (relație 1:1 prin constrângere `UNIQUE`).
14. **`curs`** — tipurile de cursuri academice (denumire, nivel, complexitate).
15. **`sesiune_curs`** — instanțele concrete ale cursurilor: curs, trainer, preț, capacitate maximă (implicit 8), stare (`Planificata`, `In_desfasurare`, `Finalizata`, `Anulata`).
16. **`interval_sesiune`** — intervalele orare și zilele în care se desfășoară sesiunea de curs.
17. **`inscriere`** — asocierea dintre client și sesiune: dată, stare (`In_asteptare`, `Confirmata`, `In_curs`, `Finalizata`, `Retrasa`, `Inlocuita`), preț aplicat, dată retragere, referință la înscrierea înlocuită.
18. **`plata`** — evidența financiară a plăților asociate înscrierilor: sumă, dată, tip (`Avans`, `Plata_ulterioara`), monedă (`RON`).
19. **`document_inscriere`** — documente depuse de cursanți (act identitate, diplome) cu tip, dată și referință fișier.
20. **`lista_asteptare`** — solicitările cursanților pentru sesiunile ocupate, ordonate după marcajul temporal al adăugării.
21. **`produs`** — catalogul articolelor cosmetice, consumabilelor și instrumentelor (cod unic, categorie, unitate de măsură, formulă: `Clasic` / `Hipoalergenic`).
22. **`stoc`** — pozițiile fizice de inventar, separate pe destinațiile `Salon` și `Academie`, cu cantitate disponibilă și prag minim.
23. **`miscare_stoc`** — jurnalul tranzacțional al mișcărilor: tip (`Intrare`, `Consum`, `Transfer`, `Eliminare`), cantitate, dată, angajat responsabil, stoc sursă și stoc destinație.
24. **`registru_sterilizare`** — registrul sanitar electronic: data sterilizării, aparat (`Pupinel`, `Autoclav`), temperatură, oră început/sfârșit, descriere material, referință imagine test biologic, angajat responsabil.

### Integritate, Constrângeri și Indecși

- **Chei Străine (FK):** asigură integritatea referențială pe întreg lanțul de entități. La nivelul `serviciu_zi_tehnician`, ștergerea configurării unei zile declanșează ștergerea în cascadă (`ON DELETE CASCADE`).
- **Constrângeri de Unicitate (UNIQUE):**
  - E-mail și telefon unice în `client` și `angajat`;
  - E-mail unic în `utilizator` (`uq_utilizator_email`);
  - Cod unic produs în `produs` (`uq_produs_cod`);
  - O singură recenzie per programare în `feedback` (`uq_feedback_programare`);
  - O singură înscriere activă per client și sesiune în `inscriere` (`uq_inscriere_client_sesiune`);
  - O singură poziție per produs și destinație în `stoc` (`uq_stoc_produs_destinatie`).
- **Verificări de Validitate (CHECK):**
  - Sfârșitul unui interval strict mai mare decât începutul său (`chk_disponibilitate_interval`, `chk_sesiune_interval`);
  - Tarife și prețuri nenegative, sume plătite strict pozitive (`chk_plata_suma`);
  - Durate de servicii strict pozitive (`chk_oferta_durata`, `chk_programare_serviciu_durata`);
  - Evaluare feedback cuprinsă între 1 și 5 (`chk_feedback_evaluare`);
  - Capacitate sesiune între 1 și 8 cursanți (`chk_sesiune_capacitate`);
  - Cantități și praguri minime de stoc nenegative, cantitate mișcare strict pozitivă (`chk_miscare_cantitate`);
  - Validarea strictă a sursei și destinației la mișcările de stoc (`chk_miscare_sursa_destinatie`);
  - Temperatura consemnată la sterilizare strict pozitivă (`chk_registru_temperatura`).
- **Optimizare prin Indecși (peste 30 de indecși definiți în `db/schema/03_indexes.sql`):**
  - Indexuri pe toate cheile străine pentru accelerarea clauzelor `JOIN`;
  - Indexuri compuse temporale: `idx_programare_data_ora`, `idx_serviciu_zi_oferta`, `idx_interval_sesiune_inceput`, `idx_registru_sterilizare_data`.

### Normalizare (FN1, FN2, FN3)

Toate tabelele respectă Forma Normală 3 (FN3):
- **FN1:** valorile atributelor sunt atomice, fără liste sau tabele imbricate în celule.
- **FN2:** toate atributele non-cheie depind în totalitate de cheia primară întreagă (nu doar de o parte a unei chei compuse).
- **FN3:** nu există dependențe tranzitive între atributele non-cheie; atributele dependente sunt izolate în tabele separate (ex: serviciile dintr-o programare, plățile unei înscrieri, utilizatorii independenți de detaliile de profil).

---

## Date de Test și Populare Tranzacțională

Popularea bazei de date este realizată tranzacțional prin scriptul **`db/seed/01_insert_date.sql`** (`START TRANSACTION ... COMMIT`), conținând scenarii de test complexe și realiste:
- **10 angajați:** 1 trainer dedicat (Mihuț Nerea), 2 persoane cu dublu rol tehnician & trainer (Berinde Bianca, Velasco Sara), 7 tehnicieni dedicați, cu certificări de top (Master Technician, Slim Course, Extreme Nails etc.);
- **9 tehnicieni** înregistrați și **3 traineri**;
- **30 clienți:** 26 de clienți activi cu istoric de programări, 4 clienți fără nicio programare (pentru testarea interogării 17), 5 clienți cu alergii declarate (acetonă, cleaner, produse cu monomeri HEMA);
- **57 de produse** cosmetice, geluri, baze și instrumentar profesional;
- **114 poziții de stoc** (separate strict: 57 pentru Salon, 57 pentru Academie) cu praguri critice de alertare a deficitului;
- **14 servicii:** Ojă semipermanentă, Întreținere (mărimi 2-5), Slim (mărimi 2-4), Construcție (mărimi 2-5), Tehnica Hybrid cu polygel, Design (Easy/Mediu/Complex), Schimbare / corectare arhitectură;
- **126 oferte active** asociate tehnicienilor prin `tehnician_serviciu`;
- **9 cursuri** academice și **8 sesiuni** în diferite stadii;
- **105 programări** în salon acoperind luna noiembrie 2026, cu toate stările (`Confirmata`, `Finalizata`, `Anulata`, `Neprezentare`);
- **121 asocieri de servicii** la programări;
- **8 recenzii de feedback** de la cliente;
- **6 înscrieri** la cursuri, **7 plăți** (avansuri și tranșe integrale) și **4 cereri pe listele de așteptare**;
- **38 de înregistrări complete** în registrul de sterilizare (Pupinel la 180°C/160°C și Autoclav la 134°C/121°C pe toată durata lunii noiembrie 2026, cu ore, descrieri și coduri de teste);
- **6 mișcări de stoc** trasabile (Intrare, Consum, Transfer salon-academie, Eliminare).

---

## Interogări SQL și Rapoarte de Analiză (20 de Scripturi)

Cele 20 de interogări din folderul **`db/queries/`** acoperă cerințele de raportare operațională, decizională, financiară și de conformitate sanitară:

| Fișier Script | Titlu și Scop de Business | Concepte SQL Utilizate |
|---|---|---|
| `01_programari_cronologic.sql` | Agenda completă a programărilor în ordine cronologică | `ORDER BY data ASC, ora ASC` |
| `02_programari_confirmate_azi.sql` | Lista programărilor confirmate pentru ziua curentă | `WHERE data = CURDATE() AND stare = 'Confirmata'` |
| `03_programari_clienti.sql` | Programările corelate cu numele beneficiarilor | `INNER JOIN client` |
| `04_programari_cu_tehnicieni.sql` | Relația tripartită completă: client, tehnician și dată | Joncțiune pe 4 tabele (`client`, `tehnician`, `angajat`) |
| `05_servicii_tehnicieni.sql` | Catalogul ofertelor active per tehnician (tarife, durate) | `INNER JOIN tehnician_serviciu`, `WHERE stare_oferta = 'Activa'` |
| `06_servicii_programari.sql` | Serviciile efectiv incluse într-o programare și costurile aplicate | `INNER JOIN programare_serviciu`, `serviciu` |
| `07_sesiuni_cu_traineri.sql` | Sesiunile academiei cu trainerii titulari și capacitatea (max 8) | Joncțiune `sesiune_curs`, `curs`, `trainer`, `angajat` |
| `08_inscrieri_clienti.sql` | Înscrierile cursanților și stadiul participării | Joncțiune `inscriere`, `client`, `sesiune_curs`, `curs` |
| `09_plati_inscrieri.sql` | Încasările financiare per înscriere (avansuri și plăți) | `INNER JOIN plata`, `inscriere`, `client` |
| `10_total_platit_inscriere.sql` | Situația soldului per cursant: total achitat și rest de plată | `LEFT JOIN plata`, `SUM(suma)`, `COALESCE`, `GROUP BY` |
| `11_numar_programari_client.sql` | Frecvența de vizitare a clienților în salon | `GROUP BY client`, `COUNT(p.id_programare)` |
| `12_clienti_programari_multiple.sql` | Identificarea clienților fideli (cel puțin două vizite) | `GROUP BY`, `HAVING COUNT(p.id_programare) >= 2` |
| `13_stocuri_produse.sql` | Inventarul cantitativ separat pe Salon și Academie | Joncțiune `produs`, `stoc`, ordonare pe destinație |
| `14_stocuri_sub_prag.sql` | Alertă stocuri critice și calculul deficitului de reaprovizionare | `WHERE cantitate_disponibila < prag_minim`, calcul diferență |
| `15_sterilizari_responsabili.sql` | Registrul de sterilizare cu parametri tehnici și tehnicieni | Joncțiune `registru_sterilizare`, `angajat`, audit DSP |
| `16_total_programare.sql` | Preț total și durată totală cumulate per bon/programare | `LEFT JOIN programare_serviciu`, `SUM(pret)`, `SUM(durata)` |
| `17_clienti_fara_programari.sql` | Identificarea clienților inactivi pentru campanii de marketing | Subinterogare corelată cu `WHERE NOT EXISTS (...)` |
| `18_incasari_lunare.sql` | Raport financiar managerial: încasări lunare din cursuri | `YEAR(data)`, `MONTH(data)`, `SUM(suma)`, `GROUP BY` |
| `19_lista_asteptare_cursuri.sql` | Listele de așteptare pentru sesiuni ocupate (prioritizare FIFO) | Joncțiune `lista_asteptare`, ordonare `data_adaugarii ASC` |
| `20_miscari_stoc_responsabili.sql` | Jurnal complet de audit logistic pentru mișcările de inventar | Multiple `LEFT JOIN` cu `stoc` și `produs` (sursă/destinație) |

---

## Aplicația Practică Java Swing

Aplicația desktop este dezvoltată în **Java 17+**, folosește biblioteca **FlatLaf** pentru o grafică modernă și elegantă (paletă cromatică crem/bej, carduri albe rotunjite, accente negre și butoane stilizate) și se conectează prin **JDBC** la serverul bazei de date.

### Arhitectura Software

Codul sursă este modularizat în pachetul **`salon`**:
- **`salon.Main`** — punctul de pornire, inițializarea temei `FlatLightLaf` și a fonturilor sistemului;
- **`salon.db.DatabaseConnection`** — furnizor singleton de conexiuni JDBC (`jdbc:mysql://localhost:3306/proiect_salon`);
- **`salon.service`** — logica de afaceri decuplată de interfață:
  - `AppointmentService` — programări salon și modificări de stare;
  - `AvailabilityService` — algoritm de calcul al sloturilor disponibile (pași de 30 min, validare suprapunere);
  - `TechnicianDayService` — configurarea dinamică a zilelor de lucru și a serviciilor active per zi;
  - `SterilizationService` — operații registru de sterilizare și salvarea securizată a imaginilor martor;
  - `ClientService` — centralizator activitate clienți;
  - `TechnicianService`, `TrainerService`, `ServiceCatalogService`;
- **`salon.ui`** — ferestrele și ecranele principale (`LoginFrame`, `RegisterFrame`, `ClientDashboard`, `TechnicianDashboard`, `AppointmentBookingPanel`, `CourseBookingPanel`);
- **`salon.ui.client`** — panourile modulului client (`ClientHomePanel`, `ClientAppointmentsPanel`, `ClientActivityPanel`, `ClientProfilePanel`, `ClientWaitingListPanel`);
- **`salon.ui.technician`** — panourile modulului tehnician (`TechnicianHomePanel`, `TechnicianAppointmentsPanel`, `TechnicianAvailabilityPanel`, `TechnicianDayDialog`, `TechnicianProfilePanel`);
- **`salon.ui.registry`** — registrul sanitar (`SterilizationRegistryPanel`, `SterilizationRegistryDialog`);
- **`salon.ui.components`** — componente vizuale reutilizabile (`CalendarGrid`, `DatePickerDialog`, `RoundedCard`, `StatusBanner`, `UiStyle`);
- **`salon.ui.model`** — clase record și DTO-uri pentru transferul datelor (`AppointmentItem`, `BookingDraft`, `RegistryRecord`, `ServiceOption`, `TimeInterval`).

### Autentificare și Roluri Dinamice

- **`LoginFrame`:** permite autentificarea securizată prin adresă de e-mail și parolă. Verifică hash-ul SHA-256 salvat în tabela `utilizator` și direcționează utilizatorul direct către panoul corespunzător (`ClientDashboard` pentru rolul Client, `TechnicianDashboard` pentru rolul Tehnician).
- **`RegisterFrame`:** la crearea unui cont nou, aplicația validează formatul e-mailului, potrivirea parolei și numărul de telefon.
- **Atribuire Automată a Rolului:** utilizatorul nu își alege manual rolul. Aplicația interoghează baza de date:
  - dacă e-mailul aparține unui angajat din `trainer`, i se atribuie automat rolul `Trainer`;
  - dacă e-mailul aparține unui angajat din `tehnician`, i se atribuie automat rolul `Tehnician`;
  - dacă e-mailul nu este găsit în personalul salonului, se creează automat o înregistrare în tabela `client` și i se atribuie rolul `Client`.

### Modulul Client (ClientDashboard)

1. **Home (`ClientHomePanel`):** sumarul vizitelor programate, acțiuni rapide și alerte utile.
2. **Rezervare Salon (`AppointmentBookingPanel`):**
   - Selectare tehnician din lista specialiștilor activi;
   - Configurare dinamică a serviciilor: Ojă semipermanentă, Întreținere, Slim sau Construcție;
   - Selectare mărime (2-5) cu recalculare automată de tarif și durată;
   - Tehnica de lucru: Simplă sau Hybrid cu polygel pentru întărirea unghiei;
   - Nivel de design: Fără, Easy, Mediu sau Complex;
   - Opțiuni avansate: Corectare sau schimbare de arhitectură;
   - **Calendar lunar de disponibilitate:** zilele disponibile sunt marcate printr-o bară verde; la selectarea unei zile, sunt afișate orele de start în pași de 30 de minute;
   - **Validare anti-suprapunere:** algoritmul elimină automat intervalele care se suprapun cu programările existente neanulate ale tehnicianului, perioadele sale de absență/concediu sau cursurile unde acesta este trainer;
   - Salvare tranzacțională atomică în `programare` și `programare_serviciu`.
3. **Înscriere Cursuri Academie (`CourseBookingPanel`):**
   - Explorare catalog cursuri pe niveluri;
   - Listă sesiuni planificate, evidențiind capacitatea (max 8 cursanți) și locurile ocupate;
   - Dialog de înscriere cu încărcare documente justificative (`JFileChooser`) și plată avans minim;
   - Buton inteligent de înscriere pe lista de așteptare dacă sesiunea este plină;
   - Notificări automate la deschiderea panoului dacă s-a eliberat un loc ca urmare a unei retrageri.
4. **Istoric Programări (`ClientAppointmentsPanel`):** vizualizare programări viitoare și trecute, anulare rezervare, formular de feedback cu acordare de 1-5 stele și comentariu post-serviciu.
5. **Activitate Academie (`ClientActivityPanel`):** situație cursuri, avans achitat, rest de plată datorat și documente depuse.
6. **Profil și Alerte Medicale (`ClientProfilePanel`):** gestionare telefon/email, declarare alergii la substanțe (acetonă, HEMA, uleiuri) vizibile pentru tehnicieni, schimbare parolă.

### Modulul Tehnician (TechnicianDashboard)

1. **Sumar Zilnic (`TechnicianHomePanel`):** indicatori operativi, număr cliente, prima programare a zilei și alerte de alergii.
2. **Programările Zilei (`TechnicianAppointmentsPanel`):** navigare calendaristică pe zile, afișare servicii/durate per client și comutare operativă a stării: `Confirmata`, `Finalizata`, `Anulata`, `Neprezentare`.
3. **Disponibilitate și Configurare Zilnică (`TechnicianAvailabilityPanel`, `TechnicianDayDialog`):**
   - Calendar lunar vizual;
   - Stabilire regim zi: `Lucru` sau `Libera` în `configurare_zi_tehnician`;
   - Definire orare de lucru și pauze de masă în `interval_disponibilitate`;
   - **Selecție granulară a serviciilor:** tehnicianul bifează exact ce servicii dorește să presteze în acea zi calendaristică (salvate în `serviciu_zi_tehnician`), oferind o flexibilitate operațională unică.
4. **Profil Profesional (`TechnicianProfilePanel`):** specializare, date de contact și acreditări absolvite.

### Registrul Digital de Sterilizare cu Martor Foto

Implementat în **`SterilizationRegistryPanel`** și **`SterilizationRegistryDialog`**, modulul digitalizează cerințele stricte de igienă:
- Consemnare ciclu: dată, aparat (**Pupinel** la 180°C/160°C sau **Autoclav** la 134°C/121°C), temperatură, oră început și oră sfârșit;
- Text descriptiv al instrumentelor sterilizate (forfecuțe, pushere, capete de freză carbid/diamant);
- Încărcare fotografie martor/test biologic: copiere automată a fișierului imagine în directorul securizat al aplicației și salvarea referinței în coloana `referinta_test`;
- Asocierea tehnicianului conectat ca responsabil direct.

### Managementul Tranzacțiilor ACID

Pentru a împiedica stările inconsistente sau înregistrările orfane, toate fluxurile compuse de scriere folosesc tranzacții native JDBC:
```java
connection.setAutoCommit(false);
try {
    // 1. Inserare programare în tabela programare
    // 2. Inserare servicii componente în programare_serviciu
    connection.commit();
} catch (SQLException ex) {
    connection.rollback();
    throw ex;
} finally {
    connection.setAutoCommit(true);
}
```

---

## Structura Depozitului

```text
proiect_baze_de_date/
├── docs/
│   ├── Proiect_BD.docx         # Documentația academică completă (24 tabele, 20 interogări, arhitectură aplicație)
│   └── Proiect_BD_backup.docx  # Copie de siguranță a documentației inițiale
├── db/
│   ├── schema/
│   │   ├── 01_create_tables.sql # Crearea celor 24 de tabele relaționale
│   │   ├── 02_constraints.sql   # Chei străine, unicități și verificări CHECK
│   │   └── 03_indexes.sql       # Cei 30+ indecși de optimizare
│   ├── seed/
│   │   └── 01_insert_date.sql   # Popularea tranzacțională cu setul complet de date
│   ├── queries/
│   │   ├── 01_programari_cronologic.sql
│   │   ├── 02_programari_confirmate_azi.sql
│   │   ├── ... (03 - 19)
│   │   └── 20_miscari_stoc_responsabili.sql
│   └── diagrams/
│       ├── diagrama_ER/         # Modelul conceptual ER (PNG și Draw.io)
│       ├── model_logic/         # Modelul relațional logic (PNG și Draw.io)
│       └── diagrama_cazuri_utilizare/ # Diagrame use-case (Salon, Academie, Angajat)
├── app/
│   ├── requirements.txt         # Referințe dependințe
│   └── BD_proiect/              # Proiectul Java IntelliJ IDEA
│       ├── src/
│       │   └── salon/
│       │       ├── Main.java
│       │       ├── db/          # DatabaseConnection.java
│       │       ├── service/     # Serviciile de business (Appointment, Availability, Day, Sterilization etc.)
│       │       ├── ui/          # Ferestrele principale și dashboard-urile
│       │       ├── ui/client/   # Panourile clientului
│       │       ├── ui/technician/# Panourile tehnicianului
│       │       ├── ui/registry/ # Panourile registrului de sterilizare
│       │       ├── ui/components/# Componente grafice (calendar, butoane, carduri)
│       │       └── ui/model/    # DTO-uri și modele de date
│       └── .idea/               # Configurări IntelliJ IDEA
├── CHANGELOG.md                 # Istoricul detaliat al versiunilor și etapelor
└── README.md                    # Această documentație
```

---

## Ghid de Instalare și Rulare

### 1. Cerințe de Sistem
- **Sistem de Operare:** Windows 10/11, Linux sau macOS;
- **Java Development Kit (JDK):** Versiunea 17 sau mai recentă;
- **Server Bază de Date:** MariaDB (10.4+) sau MySQL (8.0+);
- **IDE recomandat:** IntelliJ IDEA Community sau Ultimate Edition.

### 2. Configurarea Bazei de Date
Porniți serverul MySQL/MariaDB (de exemplu prin panoul XAMPP sau serviciul local) și executați scripturile în ordine:
```sql
-- 1. Crearea bazei de date și a tabelelor
mysql -u root < db/schema/01_create_tables.sql

-- 2. Aplicarea constrângerilor de integritate
mysql -u root < db/schema/02_constraints.sql

-- 3. Crearea indecșilor de optimizare
mysql -u root < db/schema/03_indexes.sql

-- 4. Popularea bazei de date cu date de test
mysql -u root < db/seed/01_insert_date.sql
```

### 3. Configurarea Conexiunii în Aplicație
Dacă parola serverului local MariaDB/MySQL este diferită de șirul vid, ajustați credențialele în fișierul `app/BD_proiect/src/salon/db/DatabaseConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/proiect_salon";
private static final String USER = "root";
private static final String PASSWORD = "";
```

### 4. Rularea Aplicației
1. Deschideți directorul `app/BD_proiect` în **IntelliJ IDEA**;
2. Asigurați-vă că biblioteca FlatLaf și driverul MariaDB/MySQL JDBC sunt detectate (configurate în fișierele `.idea/libraries`);
3. Rulați clasa principală: **`salon.Main`**;
4. Autentificați-vă cu un cont existent din baza de date sau creați un cont nou prin opțiunea *„Creează cont”*.

---

## Autor și Date Academice

- **Autor:** Mihuț Nerea
- **Specializarea:** Calculatoare, Anul 3
- **Disciplina:** Baze de Date
- **An Universitar:** 2026–2027
