# Changelog

Toate modificările importante aduse acestui proiect sunt documentate cronologic în acest fișier, conform convenției [Keep a Changelog](https://keepachangelog.com/).

---

## [v0.9-documentatie-finala] - 2026-11-17
### Modificat & Actualizat
- **Documentația oficială Word (`docs/Proiect_BD.docx`):**
  - Actualizarea numărului de tabele de la 21 la 24 de tabele relaționale în întreg documentul;
  - Includerea în Tabelul 0 (verificare FN3) și Tabelul 2 (chei primare compuse) a noilor tabele: `Utilizator`, `Configurare_Zi_Tehnician` și `Serviciu_Zi_Tehnician`;
  - Adăugarea dicționarului de date detaliat (coloane, tipuri de date, chei, constrângeri, descriere) pentru cele 3 tabele noi;
  - Actualizarea statisticilor de populare din Capitolul 4 conform datelor reale din `db/seed/01_insert_date.sql` (10 angajați, 30 clienți, 57 produse cu 114 stocuri, 105 programări, 14 servicii, 9 cursuri cu 8 sesiuni, 38 cicluri complete în registrul de sterilizare, 6 mișcări de stoc etc.);
  - **Adăugarea Capitolului 4.3 (Interogări SQL și rapoarte de analiză):** documentarea completă a tuturor celor 20 de scripturi SQL din `db/queries/`, cu scop de business, cod SQL sursă și explicații tehnice privind joncțiunile, agregările și optimizările;
  - **Completarea Capitolului 5 (Implementarea aplicației):** descrierea detaliată a arhitecturii pe straturi (MVC / Service Layer), autentificării, înregistrării cu atribuire automată de rol, modulului Client (`AppointmentBookingPanel`, `CourseBookingPanel`, `ClientAppointmentsPanel`, `ClientActivityPanel`, `ClientProfilePanel`), modulului Tehnician (`TechnicianHomePanel`, `TechnicianAppointmentsPanel`, `TechnicianAvailabilityPanel`, `TechnicianDayDialog`, `SterilizationRegistryPanel`, `SterilizationRegistryDialog`, `TechnicianProfilePanel`), registrului sanitar electronic cu încărcare dovadă foto și a tranzacțiilor ACID din `salon.service`;
  - **Adăugarea Capitolului 6 (Concluzii și dezvoltări ulterioare):** sinteză asupra realizării obiectivelor și perspective viitoare (BCrypt/Argon2, interfață trainer dedicată, gateway plăți online).
- **Actualizarea documentației din `README.md`:** ghid complet, prezentarea celor 24 de tabele, a celor 20 de interogări, a arhitecturii Java Swing și pașii de instalare/rulare.

---

## [v0.8-modul-tehnician] - 2026-11-15
### Adăugat
- Clasa `TechnicianDashboard`: panoul principal al tehnicianului, adaptat la tema grafică modernă FlatLaf.
- Clasa `TechnicianHomePanel`: sumar zilnic al activității, programările curente și alerte operaționale.
- Clasa `TechnicianAppointmentsPanel`: navigare calendaristică pe zile, afișare servicii/durate per client și comutare operativă a stărilor programărilor (`Confirmata`, `Finalizata`, `Anulata`, `Neprezentare`).
- Clasele `TechnicianAvailabilityPanel`, `TechnicianDayDialog` și serviciul `TechnicianDayService`: configurarea regimului de lucru (zi lucrătoare vs zi liberă în `configurare_zi_tehnician`), orare de lucru și pauze în `interval_disponibilitate`, și selecția granulară a serviciilor active pe care tehnicianul dorește să le presteze în ziua respectivă (`serviciu_zi_tehnician`).
- Clasele `SterilizationRegistryPanel`, `SterilizationRegistryDialog` și serviciul `SterilizationService`: registrul digital de sterilizare sanitară conform normelor DSP (aparat Pupinel sau Autoclav, temperatură, orar început/sfârșit, materiale sterilizate, salvare securizată pe disc a fotografiei testului martor biologic și asociere angajat responsabil).
- Clasa `TechnicianProfilePanel`: vizualizarea datelor de contact, a specializărilor și a acreditărilor profesionale.

---

## [v0.7-modul-client] - 2026-11-14
### Adăugat
- Clasa `ClientDashboard`: panoul de control principal pentru clienți.
- Clasa `AppointmentBookingPanel`: flux interactiv complet de programare salon (alegere tehnician, selecție servicii — ojă semi / întreținere / slim / construcție, mărimi 2-5, tehnică hybrid cu polygel, design Easy/Mediu/Complex, schimbare/corecție arhitectură, calendar lunar colorat cu disponibilitate, pași de 30 min, verificare automată a suprapunerilor cu concedii, alte programări sau cursuri).
- Clasa `CourseBookingPanel`: catalog educațional de cursuri, filtrare sesiuni, verificare capacitate (max 8 cursanți), dialog înscriere cu încărcare documente justificative (`JFileChooser`) și plată avans minim (500 RON), înscriere pe lista de așteptare dacă sesiunea este ocupată și sistem de notificări la eliberarea unui loc.
- Clasa `ClientAppointmentsPanel`: istoric programări viitoare și finalizate, posibilitate de anulare, formular de evaluare feedback (note 1-5 și comentarii).
- Clasa `ClientActivityPanel` și `ClientWaitingListPanel`: monitorizarea cursurilor, a plăților achitate, a restului de plată și a poziției pe listele de așteptare.
- Clasa `ClientProfilePanel`: actualizare date contact, gestionare alerte medicale (alergii declarate la acetonă, cleaner sau compuși HEMA) și modificare securizată a parolei.

---

## [v0.6-autentificare-si-arhitectura] - 2026-11-12
### Adăugat
- Integrarea bibliotecii FlatLaf (`FlatLightLaf`) pentru un aspect grafic modern, curat și profesionist (nuanțe calde de bej/crem, carduri rotunjite, butoane elegante).
- Clasa `Main`: punctul de intrare în aplicație și inițializarea temei grafice.
- Clasa `DatabaseConnection`: gestionarea unificată a conexiunilor JDBC către MariaDB/MySQL.
- Fereastra `LoginFrame`: autentificare securizată prin e-mail și parolă (verificare hash SHA-256), cu rutare automată către ecranul corespunzător rolului.
- Fereastra `RegisterFrame`: înregistrare clienți noi, validări format e-mail, număr de telefon și confirmare parolă.
- Atribuirea automată a rolurilor (`Client`, `Tehnician`, `Trainer`): rolul este dedus automat din înregistrările existente în tabelele `angajat`, `tehnician` și `trainer`, împiedicând auto-atribuirea neautorizată de roluri administrative.

---

## [v0.5-extindere-schema] - 2026-11-08
### Adăugat
- Tabela `utilizator`: stocarea credențialelor de acces (e-mail unic, parolă hash, rol ENUM, chei străine către `client` sau `angajat`).
- Tabela `configurare_zi_tehnician`: gestionarea calendarului personalizat al fiecărui tehnician (data, tip de zi: `Lucru` sau `Libera`).
- Tabela `serviciu_zi_tehnician`: activarea sau dezactivarea serviciilor oferite de tehnician într-o anumită zi calendaristică, legată prin cheie străină compusă și clauză `ON DELETE CASCADE`.
- Constrângerile referențiale și unicitățile corespunzătoare în `02_constraints.sql`.

---

## [v0.4-populare-si-interogari] - 2026-11-02
### Adăugat
- Scriptul `db/seed/01_insert_date.sql`: populare tranzacțională consistentă cu date realiste (10 angajați, 30 clienți, 57 produse cu 114 stocuri, 105 programări, 121 programare-servicii, 14 servicii, 126 oferte tehnician, 9 cursuri, 8 sesiuni, 8 feedback-uri, 6 înscrieri, 7 plăți, 4 cereri listă așteptare, 38 înregistrări registru sterilizare, 6 mișcări de stoc).
- Crearea celor 20 de interogări SQL analitice și de raportare în directorul `db/queries/`:
  - `01_programari_cronologic.sql` până la `06_servicii_programari.sql` pentru agenda operațională a salonului;
  - `07_sesiuni_cu_traineri.sql` până la `10_total_platit_inscriere.sql` pentru activitatea și contabilitatea academiei;
  - `11_numar_programari_client.sql` și `12_clienti_programari_multiple.sql` pentru fidelizarea clienților;
  - `13_stocuri_produse.sql` și `14_stocuri_sub_prag.sql` pentru inventar și stocuri critice;
  - `15_sterilizari_responsabili.sql` pentru auditul sanitar al sterilizării;
  - `16_total_programare.sql` pentru calculul bonurilor cumulate;
  - `17_clienti_fara_programari.sql` pentru clienți inactivi;
  - `18_incasari_lunare.sql` pentru raportul contabil lunar;
  - `19_lista_asteptare_cursuri.sql` pentru cozile de așteptare la cursuri;
  - `20_miscari_stoc_responsabili.sql` pentru trasabilitatea operațiunilor de magazie.

---

## [v0.3-schema-sql] - 2026-10-25
### Adăugat
- Fișierul `db/schema/01_create_tables.sql`: crearea tabelelor relaționale de bază (21 tabele inițiale), definirea tipurilor de date, motoarelor InnoDB, seturilor de caractere utf8mb4 și a cheilor primare.
- Fișierul `db/schema/02_constraints.sql`: adăugarea constrângerilor de integritate referențială (chei străine), a regulilor de unicitate (UNIQUE) și a validărilor de domeniu (CHECK).
- Fișierul `db/schema/03_indexes.sql`: definirea a 30 de indecși pe cheile străine și pe coloanele frecvent utilizate în clauzele `WHERE`, `JOIN` și `ORDER BY`.

---

## [v0.2-proiectare] - 2026-10-18
### Adăugat
- Diagrama conceptuală ER (Entity-Relationship) salvată în formatele PNG și Draw.io în `db/diagrams/diagrama_ER/`.
- Diagramele cazurilor de utilizare (Use Case) pentru Salon, Academie și Angajat în `db/diagrams/diagrama_cazuri_utilizare/`.
- Modelul logic relațional în `db/diagrams/model_logic/`.
- Procesul complet de normalizare până la forma normală 3 (FN1, FN2, FN3) documentat în dosarul de analiză.

---

## [v0.1-analiza] - 2026-10-10
### Adăugat
- Structura inițială a directoarelor pentru depozitul Git (`docs`, `db`, `app`, `teste`).
- Fișierul `.gitignore` pentru excluderea artefactelor de compilare și a fișierelor temporare IDE.
- Creare fișier `README.md` cu descrierea inițială a proiectului.
- Creare fișier `CHANGELOG.md` pentru evidența versiunilor.
- Redactarea secțiunilor *„Scopul proiectului”* și *„Obiectivele proiectului”* în documentația Word, adaptate pe fluxul real al salonului și academiei.
