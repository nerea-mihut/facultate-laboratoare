-- Date de test pentru proiect_salon

USE proiect_salon;

START TRANSACTION;


-- 1. ANGAJATI

INSERT INTO angajat
    (id_angajat, nume, prenume, telefon, email, specializare, certificari)
VALUES
    (1, 'Mihut', 'Nerea', '0700000101', 'nerea.mihut@test.ro', 'Trainer', 'Certificat trainer, Baza 1 Acreditat, Baza 2, Baza 3, Trainers in Training, Master Technician, Extreme Nails, SLim Course'),
    (2, 'Berinde', 'Bianca', '0700000102', 'bianca.berinde@test.ro', 'Tehnician si trainer', 'Certificat trainer, Baza 1 Acreditat, Baza 2, Master Technician'),
    (3, 'Velasco', 'Sara', '0700000103', 'sara.velasco@test.ro', 'Tehnician si trainer', 'Certificat trainer'),
    (4, 'Munteanu', 'Ioana', '0700000104', 'ioana.munteanu@test.ro', 'Tehnician', 'Baza 1 Acreditat, Baza 2, Salon Work, Intretineri rapide'),
    (5, 'Radu', 'Ana-Maria', '0700000105', 'ana.radu@test.ro', 'Tehnician', 'Baza 1 Acreditat, Intretineri rapide'),
    (6, 'Neagu', 'Bianca', '0700000106', 'bianca.neagu@test.ro', 'Tehnician', 'Baza1, Slim Course, Intretineri rapide'),
    (7, 'Petrescu', 'Camelia', '0700000107', 'camelia.petrescu@test.ro', 'Tehnician', 'Baza 1 Acreditat, Baza 2'),
    (8, 'Marin', 'Valentina', '0700000108', 'valentina.marin@test.ro', 'Tehnician', 'Baza 1 Acreditat, Baza 2, Baza 3'),
    (9, 'Fernandez', 'Lucia', '0700000109', 'lucia.fernandez@test.ro', 'Tehnician', 'Baza 1 Acreditat, Baza 2, Master Technician'),
    (10, 'Moreau', 'Camille', '0700000110', 'camille.moreau@test.ro', 'Tehnician', 'Baza 1 Acreditat, Salon Work');


-- 2. TEHNICIENI
-- Bianca Berinde si Sara Velasco sunt si tehnicieni.

INSERT INTO tehnician
    (id_angajat)
VALUES
    (2),
    (3),
    (4),
    (5),
    (6),
    (7),
    (8),
    (9),
    (10);


-- 3. TRAINERI

INSERT INTO trainer
    (id_angajat)
VALUES
    (1),
    (2),
    (3);


-- 4. CLIENTI
-- Clientii 1..26 au programari in salon; clientii 27..30 nu au programari (pentru 17_clienti_fara_programari.sql).

INSERT INTO client
    (id_client, nume, prenume, telefon, email, alergii, detalii_alergii)
VALUES
    (1, 'Popa', 'Andrei', '0700000001', 'andrei.popa@test.ro', 'Nu', NULL),
    (2, 'Iordache', 'Mihai', '0700000002', 'mihai.iordache@test.ro', 'Nu', NULL),
    (3, 'Dumitru', 'Ana', '0700000003', 'ana.dumitru@test.ro', 'Nu', NULL),
    (4, 'Vasilescu', 'Elena', '0700000004', 'elena.vasilescu@test.ro', 'Nu', NULL),
    (5, 'Munteanu', 'Ioana', '0700000005', 'ioana.munteanu@test.ro', 'Nu', NULL),
    (6, 'Radu', 'Maria', '0700000006', 'maria.radu@test.ro', 'Nu', NULL),
    (7, 'Neagu', 'Bianca', '0700000007', 'bianca.neagu@test.ro', 'Nu', NULL),
    (8, 'Petrescu', 'Camelia', '0700000008', 'camelia.petrescu@test.ro', 'Nu', NULL),
    (9, 'Marin', 'Valentina', '0700000009', 'valentina.marin@test.ro', 'Da', 'Alergie la acetona'),
    (10, 'Ilie', 'Sofia', '0700000010', 'sofia.ilie@test.ro', 'Nu', NULL),
    (11, 'Stan', 'Diana', '0700000011', 'diana.stan@test.ro', 'Nu', NULL),
    (12, 'Barbu', 'Raluca', '0700000012', 'raluca.barbu@test.ro', 'Nu', NULL),
    (13, 'Toma', 'Alexandra', '0700000013', 'alexandra.toma@test.ro', 'Nu', NULL),
    (14, 'Nistor', 'Cristina', '0700000014', 'cristina.nistor@test.ro', 'Nu', NULL),
    (15, 'Mihai', 'Laura', '0700000015', 'laura.mihai@test.ro', 'Nu', NULL),
    (16, 'Enache', 'Paula', '0700000016', 'paula.enache@test.ro', 'Da', 'Alergie la cirese- cleaner'),
    (17, 'Vlad', 'Denisa', '0700000017', 'denisa.vlad@test.ro', 'Nu', NULL),
    (18, 'Calin', 'Andreea', '0700000018', 'andreea.calin@test.ro', 'Da', 'Iritatie la produse cu hemma'),
    (19, 'Marin', 'Gabriela', '0700000019', 'gabriela.marin@test.ro', 'Nu', NULL),
    (20, 'Dobre', 'Roxana', '0700000020', 'roxana.dobre@test.ro', 'Nu', NULL),
    (21, 'Fernandez', 'Lucia', '0700000021', 'lucia.fernandez@test.ro', 'Nu', NULL),
    (22, 'Lopez', 'Marta', '0700000022', 'marta.lopez@test.ro', 'Nu', NULL),
    (23, 'Moreau', 'Camille', '0700000023', 'camille.moreau@test.ro', 'Nu', NULL),
    (24, 'Laurent', 'Elodie', '0700000024', 'elodie.laurent@test.ro', 'Da', 'Alergie la cleaner cu ulei'),
    (25, 'Rossi', 'Valentina', '0700000025', 'valentina.rossi@test.ro', 'Nu', NULL),
    (26, 'Ferrari', 'Giulia', '0700000026', 'giulia.ferrari@test.ro', 'Nu', NULL),
    (27, 'Garcia', 'Mateo', '0700000027', 'mateo.garcia@test.ro', 'Nu', NULL),
    (28, 'Munteanu', 'Adrian', '0700000028', 'adrian.munteanu@test.ro', 'Da', 'Alergie la produse cu hemma'),
    (29, 'Georgescu', 'Irina', '0700000029', 'irina.georgescu@test.ro', 'Nu', NULL),
    (30, 'Petrescu', 'Alina', '0700000030', 'alina.petrescu@test.ro', 'Nu', NULL);


-- 5. PRODUSE
-- Stocul este numarat in ambalaje, nu in ml/g.
-- tip_formula respecta ENUM('Clasic', 'Hipoalergenic') NULL din 01_create_tables.sql.

INSERT INTO produs
    (id_produs, cod_produs, nume, categorie, unitate_masura, tip_formula)
VALUES
    (1, 'P001', 'Liquid Builder Gel Light Nude 40 ml ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (2, 'P002', 'Liquid Builder Gel Light Natural 40 ml ONE', 'Geluri si baze', 'buc', 'Hipoalergenic'),
    (3, 'P003', 'Liquid Builder Gel Light Pink 40 ml ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (4, 'P004', 'Liquid Builder Gel Soft Rose 40 ml ONE', 'Geluri si baze', 'buc', 'Hipoalergenic'),
    (5, 'P005', 'Liquid Builder Gel Natural 40 ml ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (6, 'P006', 'Liquid Builder Gel White 40 ml ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (7, 'P007', 'Liquid Builder Gel Milky White 40 ml ONE', 'Geluri si baze', 'buc', 'Hipoalergenic'),
    (8, 'P008', 'Liquid Builder Gel Nude Pink Shimmer 40 ml ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (9, 'P009', 'Liquid Builder Gel White Blue Shimmer 40 ml ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (10, 'P010', 'Liquid Builder Gel Nude Gold Shimmer 40 ml ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (11, 'P011', 'Polygel Black 30 g ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (12, 'P012', 'Polygel Light Nude 30 g ONE', 'Geluri si baze', 'buc', 'Hipoalergenic'),
    (13, 'P013', 'Polygel White 30 g ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (14, 'P014', 'Polygel Milky White 30 g ONE', 'Geluri si baze', 'buc', 'Hipoalergenic'),
    (15, 'P015', 'Polygel Mint 30 g ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (16, 'P016', 'Polygel Pink 30 g ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (17, 'P017', 'Acrygel Nude 60 g ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (18, 'P018', 'Acrygel Natural 60 g ONE', 'Geluri si baze', 'buc', 'Hipoalergenic'),
    (19, 'P019', 'Acrygel Maro 60 g ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (20, 'P020', 'Liquid Polygel Light Nude 15 ml ONE', 'Geluri si baze', 'buc', 'Hipoalergenic'),
    (21, 'P021', 'Liquid Polygel Natural 15 ml ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (22, 'P022', 'Rubber Base Light Nude 12 ml ONE', 'Geluri si baze', 'buc', 'Hipoalergenic'),
    (23, 'P023', 'Rubber Base Natural 12 ml ONE', 'Geluri si baze', 'buc', 'Hipoalergenic'),
    (24, 'P024', 'Rubber Base Pink Shimmer 12 ml ONE', 'Geluri si baze', 'buc', 'Clasic'),
    (25, 'P025', 'Oja Semipermanenta Rosu 13 ml CANNI', 'Nail art system', 'buc', 'Clasic'),
    (26, 'P026', 'Oja Semipermanenta Negru 13 ml CANNI', 'Nail art system', 'buc', 'Clasic'),
    (27, 'P027', 'Oja Semipermanenta Cat Eye Albastru 13 ml YAS', 'Nail art system', 'buc', 'Clasic'),
    (28, 'P028', 'Oja Semipermanenta Metallic Argintiu 13 ml CORIA', 'Nail art system', 'buc', 'Clasic'),
    (29, 'P029', 'Spider Gel Argintiu 5 ml CANNI', 'Nail art system', 'buc', 'Clasic'),
    (30, 'P030', 'Spider Gel Auriu 5 ml YAS', 'Nail art system', 'buc', 'Clasic'),
    (31, 'P031', 'Pensula Gel Brush 8', 'Ustensile', 'buc', NULL),
    (32, 'P032', 'Pensula Gel Brush 10', 'Ustensile', 'buc', NULL),
    (33, 'P033', 'Pensula Acrygel 10', 'Ustensile', 'buc', NULL),
    (34, 'P034', 'Pensula Acrygel 12', 'Ustensile', 'buc', NULL),
    (35, 'P035', 'Pensula Liner 9 mm', 'Ustensile', 'buc', NULL),
    (36, 'P036', 'Pensula Liner 11 mm', 'Ustensile', 'buc', NULL),
    (37, 'P037', 'Pensula Liner 15 mm', 'Ustensile', 'buc', NULL),
    (38, 'P038', 'Pensula Pictura', 'Ustensile', 'buc', NULL),
    (39, 'P039', 'Pensula Spider Gel 5 ml CANNI', 'Ustensile', 'buc', NULL),
    (40, 'P040', 'Pensula Spider Gel 5 ml YAS', 'Ustensile', 'buc', NULL),
    (41, 'P041', 'Pensula Spider Gel 5 ml CORIA', 'Ustensile', 'buc', NULL),
    (42, 'P042', 'Foarfeca Dreptaci Staleks', 'Ustensile', 'buc', NULL),
    (43, 'P043', 'Foarfeca Stangaci Staleks', 'Ustensile', 'buc', NULL),
    (44, 'P044', 'Foarfeca Dreptaci Eugen Tocilarul', 'Ustensile', 'buc', NULL),
    (45, 'P045', 'Foarfeca Stangaci Eugen Tocilarul', 'Ustensile', 'buc', NULL),
    (46, 'P046', 'Pila 100/180', 'Consumabile', 'buc', NULL),
    (47, 'P047', 'Buffer 240', 'Consumabile', 'buc', NULL),
    (48, 'P048', 'Sablon', 'Consumabile', 'buc', NULL),
    (49, 'P049', 'Tipsuri Natural', 'Consumabile', 'buc', NULL),
    (50, 'P050', 'Acetona', 'Consumabile', 'buc', NULL),
    (51, 'P051', 'Cleaner Tiramisu ONE', 'Solutii pregatire', 'buc', 'Clasic'),
    (52, 'P052', 'Dezinfectant Instrumentar Barbicid', 'Dezinfectanti', 'buc', NULL),
    (53, 'P053', 'Dezinfectant Suprafete Microzid', 'Dezinfectanti', 'buc', NULL),
    (54, 'P054', 'Top Coat Glossy', 'Solutii pregatire', 'buc', 'Hipoalergenic'),
    (55, 'P055', 'Top Coat Matte', 'Solutii pregatire', 'buc', 'Clasic'),
    (56, 'P056', 'Nail Prep', 'Solutii pregatire', 'buc', 'Hipoalergenic'),
    (57, 'P057', 'Base Coat', 'Solutii pregatire', 'buc', 'Hipoalergenic');


-- 6. STOCURI
-- Include pozitii cu cantitate_disponibila < prag_minim (id_stoc: 3, 15, 37, 44, 85, 91) pentru 14_stocuri_sub_prag.sql.

INSERT INTO stoc
    (id_stoc, id_produs, destinatie, cantitate_disponibila, prag_minim)
VALUES
    (1, 1, 'Salon', 3, 1),
    (2, 1, 'Academie', 5, 2),
    (3, 2, 'Salon', 0, 1),
    (4, 2, 'Academie', 4, 2),
    (5, 3, 'Salon', 2, 1),
    (6, 3, 'Academie', 3, 1),
    (7, 4, 'Salon', 3, 1),
    (8, 4, 'Academie', 4, 2),
    (9, 5, 'Salon', 2, 1),
    (10, 5, 'Academie', 3, 1),
    (11, 6, 'Salon', 4, 2),
    (12, 6, 'Academie', 6, 2),
    (13, 7, 'Salon', 3, 1),
    (14, 7, 'Academie', 5, 2),
    (15, 8, 'Salon', 0, 1),
    (16, 8, 'Academie', 3, 1),
    (17, 9, 'Salon', 2, 1),
    (18, 9, 'Academie', 3, 1),
    (19, 10, 'Salon', 2, 1),
    (20, 10, 'Academie', 3, 1),
    (21, 11, 'Salon', 2, 1),
    (22, 11, 'Academie', 3, 1),
    (23, 12, 'Salon', 3, 1),
    (24, 12, 'Academie', 4, 2),
    (25, 13, 'Salon', 2, 1),
    (26, 13, 'Academie', 3, 1),
    (27, 14, 'Salon', 3, 1),
    (28, 14, 'Academie', 4, 2),
    (29, 15, 'Salon', 2, 1),
    (30, 15, 'Academie', 3, 1),
    (31, 16, 'Salon', 2, 1),
    (32, 16, 'Academie', 3, 1),
    (33, 17, 'Salon', 2, 1),
    (34, 17, 'Academie', 3, 1),
    (35, 18, 'Salon', 2, 1),
    (36, 18, 'Academie', 3, 1),
    (37, 19, 'Salon', 0, 1),
    (38, 19, 'Academie', 2, 1),
    (39, 20, 'Salon', 3, 1),
    (40, 20, 'Academie', 4, 2),
    (41, 21, 'Salon', 3, 1),
    (42, 21, 'Academie', 4, 2),
    (43, 22, 'Salon', 4, 2),
    (44, 22, 'Academie', 1, 2),
    (45, 23, 'Salon', 4, 2),
    (46, 23, 'Academie', 6, 2),
    (47, 24, 'Salon', 3, 1),
    (48, 24, 'Academie', 4, 2),
    (49, 25, 'Salon', 5, 2),
    (50, 25, 'Academie', 8, 3),
    (51, 26, 'Salon', 4, 2),
    (52, 26, 'Academie', 6, 2),
    (53, 27, 'Salon', 3, 1),
    (54, 27, 'Academie', 4, 2),
    (55, 28, 'Salon', 3, 1),
    (56, 28, 'Academie', 4, 2),
    (57, 29, 'Salon', 2, 1),
    (58, 29, 'Academie', 3, 1),
    (59, 30, 'Salon', 2, 1),
    (60, 30, 'Academie', 3, 1),
    (61, 31, 'Salon', 4, 1),
    (62, 31, 'Academie', 6, 2),
    (63, 32, 'Salon', 3, 1),
    (64, 32, 'Academie', 4, 2),
    (65, 33, 'Salon', 3, 1),
    (66, 33, 'Academie', 4, 2),
    (67, 34, 'Salon', 3, 1),
    (68, 34, 'Academie', 4, 2),
    (69, 35, 'Salon', 3, 1),
    (70, 35, 'Academie', 4, 2),
    (71, 36, 'Salon', 3, 1),
    (72, 36, 'Academie', 4, 2),
    (73, 37, 'Salon', 3, 1),
    (74, 37, 'Academie', 4, 2),
    (75, 38, 'Salon', 3, 1),
    (76, 38, 'Academie', 4, 2),
    (77, 39, 'Salon', 2, 1),
    (78, 39, 'Academie', 3, 1),
    (79, 40, 'Salon', 2, 1),
    (80, 40, 'Academie', 3, 1),
    (81, 41, 'Salon', 2, 1),
    (82, 41, 'Academie', 3, 1),
    (83, 42, 'Salon', 2, 1),
    (84, 42, 'Academie', 2, 1),
    (85, 43, 'Salon', 0, 1),
    (86, 43, 'Academie', 2, 1),
    (87, 44, 'Salon', 2, 1),
    (88, 44, 'Academie', 2, 1),
    (89, 45, 'Salon', 1, 1),
    (90, 45, 'Academie', 2, 1),
    (91, 46, 'Salon', 2, 3),
    (92, 46, 'Academie', 15, 5),
    (93, 47, 'Salon', 8, 2),
    (94, 47, 'Academie', 12, 4),
    (95, 48, 'Salon', 10, 3),
    (96, 48, 'Academie', 15, 5),
    (97, 49, 'Salon', 5, 2),
    (98, 49, 'Academie', 8, 3),
    (99, 50, 'Salon', 2, 1),
    (100, 50, 'Academie', 3, 1),
    (101, 51, 'Salon', 3, 1),
    (102, 51, 'Academie', 4, 2),
    (103, 52, 'Salon', 2, 1),
    (104, 52, 'Academie', 3, 1),
    (105, 53, 'Salon', 2, 1),
    (106, 53, 'Academie', 3, 1),
    (107, 54, 'Salon', 3, 1),
    (108, 54, 'Academie', 4, 2),
    (109, 55, 'Salon', 3, 1),
    (110, 55, 'Academie', 4, 2),
    (111, 56, 'Salon', 3, 1),
    (112, 56, 'Academie', 4, 2),
    (113, 57, 'Salon', 3, 1),
    (114, 57, 'Academie', 4, 2);


-- 7. SERVICII

INSERT INTO serviciu
    (id_serviciu, denumire, categorie)
VALUES
    (1, 'Oja semipermanenta', 'Manichiura'),
    (2, 'Constructie marimea 2', 'Constructie'),
    (3, 'Constructie marimea 3', 'Constructie'),
    (4, 'Constructie marimea 4', 'Constructie'),
    (5, 'Intretinere marimea 2', 'Intretinere'),
    (6, 'Intretinere marimea 3', 'Intretinere'),
    (7, 'Intretinere marimea 4', 'Intretinere'),
    (8, 'Slim', 'Constructie'),
    (9, 'Corectie arhitectura', 'Supliment'),
    (10, 'Schimbare arhitectura', 'Supliment'),
    (11, 'Design simplu', 'Design'),
    (12, 'Design mediu', 'Design'),
    (13, 'Design complex', 'Design'),
    (14, 'Manichiura clasica', 'Manichiura');


-- 8. OFERTE TEHNICIENI
-- Toti tehnicienii ofera toate serviciile active.

INSERT INTO tehnician_serviciu
    (id_tehnician, id_serviciu, durata, pret, stare_oferta)
SELECT
    t.id_angajat,
    s.id_serviciu,
    o.durata,
    o.pret,
    'Activa'
FROM tehnician AS t
INNER JOIN serviciu AS s
    ON s.id_serviciu = o.id_serviciu
CROSS JOIN (
    SELECT 1 AS id_serviciu, 60 AS durata, 130.00 AS pret
    UNION ALL SELECT 2, 90, 150.00
    UNION ALL SELECT 3, 115, 165.00
    UNION ALL SELECT 4, 140, 180.00
    UNION ALL SELECT 5, 60, 130.00
    UNION ALL SELECT 6, 75, 140.00
    UNION ALL SELECT 7, 90, 150.00
    UNION ALL SELECT 8, 90, 200.00
    UNION ALL SELECT 9, 45, 25.00
    UNION ALL SELECT 10, 30, 50.00
    UNION ALL SELECT 11, 15, 15.00
    UNION ALL SELECT 12, 30, 30.00
    UNION ALL SELECT 13, 60, 100.00
    UNION ALL SELECT 14, 35, 100.00
) AS o;


-- 9. CURSURI

INSERT INTO curs
    (id_curs, denumire, nivel, complexitate)
VALUES
    (1, 'Baza nivel 1', 'Incepatori', 'Simpla'),
    (2, 'Baza nivel 2', 'Intermediar', 'Medie'),
    (3, 'Baza nivel 3', 'Avansat', 'Medie'),
    (4, 'Master Technician', 'Avansat', 'Avansat'),
    (5, 'Extreme Nails', 'Expert', 'Complexa'),
    (6, 'Trainer', 'Expert', 'Complexa'),
    (7, 'Trainers in Training', 'Avansat', 'Complexa'),
    (8, 'Intretineri rapide', 'Intermediar', 'Medie'),
    (9, 'Salon Work', 'Incepatori', 'Simpla');


-- 10. SESIUNI DE CURS

INSERT INTO sesiune_curs
    (id_sesiune, id_curs, id_trainer, pret, nr_maxim_cursanti, stare)
VALUES
    (1, 1, 2, 2500.00, 8, 'Planificata'),
    (2, 1, 2, 2500.00, 8, 'Planificata'),
    (3, 1, 3, 2500.00, 8, 'Planificata'),
    (4, 2, 3, 2800.00, 8, 'Planificata'),
    (5, 3, 3, 3500.00, 8, 'Planificata'),
    (6, 4, 1, 4000.00, 8, 'Planificata'),
    (7, 5, 1, 5700.00, 8, 'Planificata'),
    (8, 6, 1, 19000.00, 8, 'Planificata');


-- 11. PROGRAMARI (105 inregistrari)
-- Bianca Berinde (id 2) are cele mai multe programari.
-- Sara Velasco (id 3) are mai putine programari.
-- Nerea Mihut (id 1) nu are programari (este exclusiv trainer).


INSERT INTO programare
    (id_programare, id_client, id_tehnician, data, ora, stare, motiv_anulare)
VALUES
    -- Septembrie 2026 (istoric finalizat pentru 18_incasari_lunare.sql)
    (1, 1, 2, '2026-09-14', '09:30:00', 'Finalizata', NULL),
    (2, 2, 2, '2026-09-14', '12:30:00', 'Finalizata', NULL),
    (3, 3, 4, '2026-09-15', '10:00:00', 'Finalizata', NULL),
    (4, 4, 5, '2026-09-16', '13:00:00', 'Finalizata', NULL),
    (5, 5, 6, '2026-09-17', '10:00:00', 'Finalizata', NULL),
    (6, 6, 7, '2026-09-18', '11:30:00', 'Finalizata', NULL),
    (7, 7, 8, '2026-09-21', '10:00:00', 'Finalizata', NULL),
    (8, 8, 9, '2026-09-22', '13:00:00', 'Finalizata', NULL),
    (9, 9, 10, '2026-09-23', '09:30:00', 'Finalizata', NULL),
    (10, 10, 2, '2026-09-24', '12:00:00', 'Finalizata', NULL),

    -- Octombrie 2026 (istoric: Finalizata, Anulata, Neprezentare)
    (11, 11, 2, '2026-10-05', '09:30:00', 'Finalizata', NULL),
    (12, 12, 2, '2026-10-05', '12:30:00', 'Finalizata', NULL),
    (13, 13, 4, '2026-10-06', '10:00:00', 'Finalizata', NULL),
    (14, 14, 5, '2026-10-06', '14:00:00', 'Anulata', 'Reprogramare solicitata telefonic de clienta'),
    (15, 15, 6, '2026-10-07', '09:00:00', 'Finalizata', NULL),
    (16, 16, 7, '2026-10-07', '11:30:00', 'Finalizata', NULL),
    (17, 17, 8, '2026-10-08', '10:00:00', 'Neprezentare', NULL),
    (18, 18, 9, '2026-10-08', '13:00:00', 'Finalizata', NULL),
    (19, 19, 10, '2026-10-09', '09:30:00', 'Finalizata', NULL),
    (20, 20, 2, '2026-10-09', '12:00:00', 'Finalizata', NULL),
    (21, 21, 2, '2026-10-10', '10:00:00', 'Anulata', 'Probleme personale'),
    (22, 22, 3, '2026-10-10', '14:00:00', 'Finalizata', NULL),
    (23, 23, 4, '2026-10-12', '09:00:00', 'Finalizata', NULL),
    (24, 24, 5, '2026-10-12', '12:30:00', 'Finalizata', NULL),
    (25, 25, 6, '2026-10-13', '10:00:00', 'Anulata', 'Clienta a anuntat cu 24h inainte ca nu mai poate ajunge'),
    (26, 26, 7, '2026-10-13', '14:00:00', 'Finalizata', NULL),
    (27, 1, 8, '2026-10-14', '09:30:00', 'Finalizata', NULL),
    (28, 2, 9, '2026-10-14', '13:00:00', 'Finalizata', NULL),
    (29, 3, 10, '2026-10-15', '10:00:00', 'Finalizata', NULL),
    (30, 4, 2, '2026-10-15', '14:00:00', 'Finalizata', NULL),
    (31, 5, 2, '2026-10-16', '09:30:00', 'Finalizata', NULL),
    (32, 6, 4, '2026-10-16', '12:30:00', 'Neprezentare', NULL),
    (33, 7, 5, '2026-10-17', '10:00:00', 'Finalizata', NULL),
    (34, 8, 6, '2026-10-17', '13:30:00', 'Finalizata', NULL),
    (35, 9, 7, '2026-10-19', '09:00:00', 'Anulata', 'Raceala'),
    (36, 10, 8, '2026-10-19', '12:00:00', 'Finalizata', NULL),
    (37, 11, 9, '2026-10-20', '10:00:00', 'Finalizata', NULL),
    (38, 12, 10, '2026-10-20', '14:00:00', 'Finalizata', NULL),
    (39, 13, 2, '2026-10-21', '09:30:00', 'Finalizata', NULL),
    (40, 20, 10, '2026-10-29', '14:00:00', 'Finalizata', NULL),

    -- Prima jumatate din Noiembrie 2026 (02 - 14 Nov)
    (41, 14, 2, '2026-11-02', '09:30:00', 'Finalizata', NULL),
    (42, 15, 2, '2026-11-02', '12:30:00', 'Finalizata', NULL),
    (43, 16, 4, '2026-11-03', '10:00:00', 'Finalizata', NULL),
    (44, 17, 5, '2026-11-03', '14:00:00', 'Anulata', 'Clienta a plecat din localitate'),
    (45, 18, 6, '2026-11-04', '09:30:00', 'Finalizata', NULL),
    (46, 19, 7, '2026-11-04', '13:00:00', 'Finalizata', NULL),
    (47, 21, 8, '2026-11-05', '10:00:00', 'Finalizata', NULL),
    (48, 22, 9, '2026-11-05', '14:00:00', 'Neprezentare', NULL),
    (49, 23, 10, '2026-11-06', '09:30:00', 'Finalizata', NULL),
    (50, 24, 2, '2026-11-06', '12:30:00', 'Finalizata', NULL),
    (51, 25, 3, '2026-11-07', '10:00:00', 'Anulata', 'Schimbare planuri weekend'),
    (52, 26, 4, '2026-11-07', '13:30:00', 'Finalizata', NULL),
    (53, 1, 5, '2026-11-09', '09:30:00', 'Finalizata', NULL),
    (54, 2, 6, '2026-11-09', '13:00:00', 'Finalizata', NULL),
    (55, 3, 7, '2026-11-10', '10:00:00', 'Anulata', 'Anulare din motive medicale'),
    (56, 4, 8, '2026-11-10', '14:00:00', 'Finalizata', NULL),
    (57, 5, 9, '2026-11-11', '09:30:00', 'Finalizata', NULL),
    (58, 6, 10, '2026-11-11', '13:00:00', 'Finalizata', NULL),
    (59, 7, 2, '2026-11-12', '10:00:00', 'Finalizata', NULL),
    (60, 8, 2, '2026-11-12', '13:30:00', 'Neprezentare', NULL),
    (61, 9, 4, '2026-11-13', '09:30:00', 'Finalizata', NULL),
    (62, 10, 5, '2026-11-13', '13:30:00', 'Anulata', 'Clienta a solicitat amanarea cu o saptamana'),
    (63, 11, 2, '2026-11-14', '10:00:00', 'Confirmata', NULL),
    (64, 12, 4, '2026-11-14', '12:30:00', 'Confirmata', NULL),
    (65, 13, 6, '2026-11-14', '15:00:00', 'Confirmata', NULL),
    (66, 14, 8, '2026-11-14', '17:30:00', 'Confirmata', NULL),

    -- A doua jumatate din Noiembrie 2026 (20 - 30 Nov)
    (67, 3, 2, '2026-11-20', '10:00:00', 'Confirmata', NULL),
    (68, 4, 2, '2026-11-20', '12:30:00', 'Confirmata', NULL),
    (69, 5, 4, '2026-11-21', '09:00:00', 'Confirmata', NULL),
    (70, 6, 4, '2026-11-21', '11:30:00', 'Anulata', 'Reprogramata pentru inceputul lunii decembrie'),
    (71, 7, 3, '2026-11-22', '10:00:00', 'Confirmata', NULL),
    (72, 8, 5, '2026-11-22', '14:00:00', 'Confirmata', NULL),
    (73, 1, 6, '2026-11-23', '10:00:00', 'Confirmata', NULL),
    (74, 2, 6, '2026-11-23', '12:00:00', 'Anulata', 'Clientul a anulat din cauza programului incarcat'),
    (75, 9, 7, '2026-11-24', '10:00:00', 'Confirmata', NULL),
    (76, 10, 8, '2026-11-24', '13:00:00', 'Confirmata', NULL),
    (77, 11, 9, '2026-11-25', '10:00:00', 'Anulata', 'Urgenta familiala'),
    (78, 12, 10, '2026-11-25', '14:00:00', 'Confirmata', NULL),
    (79, 13, 2, '2026-11-26', '10:00:00', 'Confirmata', NULL),
    (80, 14, 4, '2026-11-26', '13:00:00', 'Confirmata', NULL),
    (81, 15, 5, '2026-11-27', '10:00:00', 'Anulata', 'Clienta a ales o alta data'),
    (82, 16, 6, '2026-11-27', '13:00:00', 'Confirmata', NULL),
    (83, 17, 7, '2026-11-28', '10:00:00', 'Anulata', 'Nu mai poate ajunge la ora stabilita'),
    (84, 18, 8, '2026-11-28', '14:00:00', 'Confirmata', NULL),
    (85, 19, 9, '2026-11-29', '10:00:00', 'Confirmata', NULL),
    (86, 20, 2, '2026-11-29', '13:30:00', 'Confirmata', NULL),
    (87, 21, 2, '2026-11-30', '10:00:00', 'Confirmata', NULL),
    (88, 22, 3, '2026-11-30', '14:00:00', 'Confirmata', NULL),

    -- Decembrie 2026
    (89, 23, 4, '2026-12-01', '10:00:00', 'Confirmata', NULL),
    (90, 24, 5, '2026-12-01', '14:00:00', 'Confirmata', NULL),
    (91, 25, 6, '2026-12-02', '10:00:00', 'Confirmata', NULL),
    (92, 26, 7, '2026-12-02', '14:00:00', 'Anulata', 'Deplasare in interes de serviciu'),
    (93, 15, 8, '2026-12-03', '10:00:00', 'Confirmata', NULL),
    (94, 16, 9, '2026-12-03', '14:00:00', 'Confirmata', NULL),
    (95, 17, 2, '2026-12-04', '09:30:00', 'Confirmata', NULL),
    (96, 18, 2, '2026-12-04', '13:00:00', 'Anulata', 'Anulare anuntata cu 3 zile inainte'),
    (97, 3, 4, '2026-12-05', '10:00:00', 'Confirmata', NULL),
    (98, 5, 5, '2026-12-05', '14:00:00', 'Confirmata', NULL),
    (99, 7, 6, '2026-12-07', '10:00:00', 'Confirmata', NULL),
    (100, 9, 7, '2026-12-07', '13:30:00', 'Confirmata', NULL),
    (101, 11, 8, '2026-12-08', '10:00:00', 'Confirmata', NULL),
    (102, 13, 9, '2026-12-08', '14:00:00', 'Anulata', 'Clienta a solicitat reprogramare pentru sarbatori'),
    (103, 19, 10, '2026-12-09', '10:00:00', 'Confirmata', NULL),
    (104, 21, 2, '2026-12-09', '13:00:00', 'Confirmata', NULL),
    (105, 23, 3, '2026-12-10', '10:00:00', 'Confirmata', NULL);


-- 12. SERVICII DIN PROGRAMARI (acopera toate cele 105 programari)

INSERT INTO programare_serviciu
    (id_programare, id_serviciu, pret_aplicat, durata_aplicata)
VALUES
    (1, 1, 130.00, 60),
    (2, 2, 150.00, 90),
    (2, 11, 15.00, 15),
    (3, 5, 130.00, 60),
    (4, 1, 130.00, 60),
    (5, 8, 200.00, 90),
    (6, 3, 165.00, 115),
    (7, 14, 100.00, 35),
    (8, 14, 100.00, 35),
    (9, 1, 130.00, 60),
    (9, 12, 30.00, 30),
    (10, 6, 140.00, 75),
    (11, 4, 180.00, 140),
    (12, 1, 130.00, 60),
    (13, 5, 130.00, 60),
    (13, 9, 25.00, 45),
    (14, 8, 200.00, 90),
    (15, 1, 130.00, 60),
    (16, 2, 150.00, 90),
    (17, 7, 150.00, 90),
    (18, 1, 130.00, 60),
    (18, 13, 100.00, 60),
    (19, 3, 165.00, 115),
    (20, 1, 130.00, 60),
    (21, 5, 130.00, 60),
    (22, 1, 130.00, 60),
    (23, 2, 150.00, 90),
    (24, 5, 130.00, 60),
    (25, 1, 130.00, 60),
    (26, 8, 200.00, 90),
    (27, 14, 100.00, 35),
    (28, 14, 100.00, 35),
    (29, 1, 130.00, 60),
    (30, 6, 140.00, 75),
    (31, 2, 150.00, 90),
    (32, 5, 130.00, 60),
    (32, 11, 15.00, 15),
    (33, 1, 130.00, 60),
    (34, 6, 140.00, 75),
    (35, 3, 165.00, 115),
    (36, 1, 130.00, 60),
    (37, 8, 200.00, 90),
    (38, 14, 100.00, 35),
    (39, 5, 130.00, 60),
    (39, 12, 30.00, 30),
    (40, 1, 130.00, 60),
    (41, 2, 150.00, 90),
    (42, 6, 140.00, 75),
    (43, 1, 130.00, 60),
    (44, 5, 130.00, 60),
    (45, 3, 165.00, 115),
    (46, 1, 130.00, 60),
    (46, 11, 15.00, 15),
    (47, 7, 150.00, 90),
    (48, 14, 100.00, 35),
    (49, 1, 130.00, 60),
    (50, 8, 200.00, 90),
    (51, 5, 130.00, 60),
    (52, 2, 150.00, 90),
    (53, 1, 130.00, 60),
    (53, 12, 30.00, 30),
    (54, 6, 140.00, 75),
    (55, 1, 130.00, 60),
    (56, 14, 100.00, 35),
    (57, 4, 180.00, 140),
    (58, 1, 130.00, 60),
    (59, 5, 130.00, 60),
    (59, 10, 50.00, 30),
    (60, 2, 150.00, 90),
    (61, 1, 130.00, 60),
    (62, 6, 140.00, 75),
    (63, 2, 150.00, 90),
    (63, 11, 15.00, 15),
    (64, 5, 130.00, 60),
    (65, 1, 130.00, 60),
    (66, 6, 140.00, 75),
    (67, 1, 130.00, 60),
    (68, 2, 150.00, 90),
    (68, 11, 15.00, 15),
    (69, 5, 130.00, 60),
    (70, 1, 130.00, 60),
    (71, 8, 200.00, 90),
    (72, 3, 165.00, 115),
    (73, 14, 100.00, 35),
    (74, 14, 100.00, 35),
    (75, 1, 130.00, 60),
    (75, 12, 30.00, 30),
    (76, 6, 140.00, 75),
    (77, 4, 180.00, 140),
    (78, 1, 130.00, 60),
    (79, 5, 130.00, 60),
    (79, 9, 25.00, 45),
    (80, 8, 200.00, 90),
    (81, 1, 130.00, 60),
    (82, 2, 150.00, 90),
    (83, 7, 150.00, 90),
    (84, 1, 130.00, 60),
    (84, 13, 100.00, 60),
    (85, 3, 165.00, 115),
    (86, 5, 130.00, 60),
    (87, 5, 130.00, 60),
    (88, 1, 130.00, 60),
    (89, 2, 150.00, 90),
    (90, 5, 130.00, 60),
    (91, 1, 130.00, 60),
    (92, 8, 200.00, 90),
    (93, 14, 100.00, 35),
    (94, 14, 100.00, 35),
    (95, 2, 150.00, 90),
    (95, 12, 30.00, 30),
    (96, 1, 130.00, 60),
    (97, 1, 130.00, 60),
    (98, 6, 140.00, 75),
    (99, 5, 130.00, 60),
    (100, 2, 150.00, 90),
    (101, 1, 130.00, 60),
    (102, 7, 150.00, 90),
    (103, 14, 100.00, 35),
    (104, 8, 200.00, 90),
    (104, 11, 15.00, 15),
    (105, 1, 130.00, 60);


-- 13. FEEDBACK (pentru programari finalizate)

INSERT INTO feedback
    (id_feedback, id_programare, evaluare, comentariu, data)
VALUES
    (1, 1, 5, 'Rezultat impecabil, rezistenta foarte buna.', '2026-09-15'),
    (2, 2, 5, 'Foarte multumita de constructie si de design.', '2026-09-15'),
    (3, 12, 4, 'Atmosfera placuta si atentie la detalii.', '2026-10-06'),
    (4, 20, 5, 'Bianca lucreaza rapid si foarte curat.', '2026-10-10'),
    (5, 28, 5, 'Recomand cu drag, forma perfecta.', '2026-10-15'),
    (6, 40, 5, 'Manichiura clasica executata impecabil.', '2026-10-30'),
    (7, 41, 5, 'Ca de obicei, nota 10!', '2026-11-03'),
    (8, 50, 5, 'Superba tehnica slim.', '2026-11-07');


-- 14. INSCRIERI LA CURSURI
-- Include si un caz de inlocuire (inscrierea 6 o inlocuieste pe 5 la sesiunea 2).

INSERT INTO inscriere
    (id_inscriere, id_client, id_sesiune, data_inscrierii, stare, pret_aplicat, data_anuntarii_retragerii, id_inscriere_inlocuita)
VALUES
    (1, 6, 1, '2026-11-10', 'Confirmata', 2500.00, NULL, NULL),
    (2, 10, 3, '2026-11-11', 'Confirmata', 2500.00, NULL, NULL),
    (3, 14, 4, '2026-11-12', 'Confirmata', 2800.00, NULL, NULL),
    (4, 18, 6, '2026-11-13', 'Confirmata', 4000.00, NULL, NULL),
    (5, 12, 2, '2026-11-05', 'Inlocuita', 2500.00, '2026-11-12', NULL),
    (6, 20, 2, '2026-11-12', 'Confirmata', 2500.00, NULL, 5);


-- 15. PLATI PENTRU INSCRIERI
-- Include atat 'Avans', cat si 'Plata_ulterioara' pentru 09_plati_inscrieri.sql si 10_total_platit_inscriere.sql.

INSERT INTO plata
    (id_plata, id_inscriere, data, suma, tip, moneda)
VALUES
    (1, 1, '2026-11-10', 500.00, 'Avans', 'RON'),
    (2, 1, '2026-11-14', 2000.00, 'Plata_ulterioara', 'RON'),
    (3, 2, '2026-11-11', 500.00, 'Avans', 'RON'),
    (4, 3, '2026-11-12', 700.00, 'Avans', 'RON'),
    (5, 3, '2026-11-14', 1000.00, 'Plata_ulterioara', 'RON'),
    (6, 4, '2026-11-13', 1000.00, 'Avans', 'RON'),
    (7, 6, '2026-11-12', 500.00, 'Avans', 'RON');


-- 16. LISTE DE ASTEPTARE (pentru 19_lista_asteptare_cursuri.sql)

INSERT INTO lista_asteptare
    (id_client, id_sesiune, data_adaugarii)
VALUES
    (25, 1, '2026-11-12 10:00:00'),
    (26, 3, '2026-11-13 15:50:29'),
    (29, 1, '2026-11-14 09:20:00'),
    (30, 6, '2026-11-14 14:10:00');


-- 17. REGISTRU STERILIZARE (pentru 15_sterilizari_responsabili.sql)
-- Autoclav: 134.00 C (30-35 min) / 121.00 C (45-50 min).
-- Pupinel: 180.00 C (60 min) / 160.00 C (120 min) - exclusiv instrumente metalice.

INSERT INTO registru_sterilizare
    (id_inregistrare, data_sterilizarii, aparat, material_sterilizat, temperatura, ora_inceperii, ora_terminarii, referinta_test, id_angajat)
VALUES
    (1,  '2026-11-01', 'Autoclav', 'Bituri diamantate, capete de freza carbid, forfecute cuticule', 134.00, '17:14:00', '17:46:00', 'TST-2026-001', 2),
    (2,  '2026-11-02', 'Pupinel',  'Forfecute Staleks, pushere inox', 180.00, '17:35:00', '18:35:00', 'TST-2026-002', 4),
    (3,  '2026-11-03', 'Autoclav', 'Pushere, forfecute cuticule, bituri flacara', 134.00, '18:49:00', '19:21:00', 'TST-2026-003', 5),
    (4,  '2026-11-04', 'Pupinel',  'Bituri diamantate, capete de freza, pushere', 180.00, '17:22:00', '18:22:00', 'TST-2026-004', 6),
    (5,  '2026-11-05', 'Autoclav', 'Forfecute dreptaci, forfecute stangaci, pushere', 134.00, '17:41:00', '18:13:00', 'TST-2026-005', 7),
    (6,  '2026-11-06', 'Autoclav', 'Capete de freza silicon-carbid, bituri, pushere', 121.00, '18:08:00', '18:55:00', 'TST-2026-006', 8),
    (7,  '2026-11-06', 'Pupinel',  'Forfecute Eugen Tocilarul, pushere inox', 180.00, '19:12:00', '20:12:00', 'TST-2026-007', 2),
    (8,  '2026-11-07', 'Autoclav', 'Bituri, capete de freza, forfecute, pushere', 134.00, '17:19:00', '17:51:00', 'TST-2026-008', 9),
    (9,  '2026-11-08', 'Pupinel',  'Forfecute cuticule, bituri diamantate, pushere', 180.00, '17:53:00', '18:53:00', 'TST-2026-009', 10),
    (10, '2026-11-09', 'Autoclav', 'Pushere, bituri conice, forfecute Staleks', 134.00, '18:17:00', '18:49:00', 'TST-2026-010', 3),
    (11, '2026-11-10', 'Pupinel',  'Capete de freza carbid, pushere inox, forfecute', 160.00, '17:11:00', '19:11:00', 'TST-2026-011', 4),
    (12, '2026-11-11', 'Autoclav', 'Forfecute cuticule, bituri, capete de freza', 134.00, '17:38:00', '18:10:00', 'TST-2026-012', 5),
    (13, '2026-11-12', 'Pupinel',  'Forfecute dreptaci, pushere, bituri diamantate', 180.00, '18:27:00', '19:27:00', 'TST-2026-013', 2),
    (14, '2026-11-13', 'Autoclav', 'Bituri, pushere, forfecute Staleks, clesti cuticule', 134.00, '17:26:00', '17:58:00', 'TST-2026-014', 6),
    (15, '2026-11-13', 'Pupinel',  'Capete de freza metalice, forfecute, pushere', 180.00, '18:44:00', '19:44:00', 'TST-2026-015', 7),
    (16, '2026-11-14', 'Autoclav', 'Capete de freza, bituri diamantate, forfecute, pushere', 134.00, '17:33:00', '18:05:00', 'TST-2026-016', 2),
    (17, '2026-11-14', 'Pupinel',  'Forfecute cuticule, pushere inox', 180.00, '19:07:00', '20:07:00', 'TST-2026-017', 8),
    (18, '2026-11-15', 'Autoclav', 'Pushere, forfecute, bituri flacara si bila', 134.00, '17:47:00', '18:19:00', 'TST-2026-018', 9),
    (19, '2026-11-16', 'Pupinel',  'Bituri carbid, forfecute Eugen Tocilarul, pushere', 180.00, '18:13:00', '19:13:00', 'TST-2026-019', 10),
    (20, '2026-11-17', 'Autoclav', 'Forfecute cuticule, capete de freza, pushere', 134.00, '17:16:00', '17:48:00', 'TST-2026-020', 4),
    (21, '2026-11-18', 'Autoclav', 'Capete de freza polisher, bituri, pushere', 121.00, '17:52:00', '18:40:00', 'TST-2026-021', 5),
    (22, '2026-11-19', 'Pupinel',  'Forfecute Staleks, pushere inox, bituri diamantate', 180.00, '18:31:00', '19:31:00', 'TST-2026-022', 6),
    (23, '2026-11-20', 'Autoclav', 'Clesti, pushere, forfecute cuticule', 134.00, '17:23:00', '17:55:00', 'TST-2026-023', 4),
    (24, '2026-11-20', 'Pupinel',  'Bituri diamantate, capete de freza, forfecute', 180.00, '18:54:00', '19:54:00', 'TST-2026-024', 2),
    (25, '2026-11-21', 'Autoclav', 'Bituri, capete de freza, pushere, forfecute', 134.00, '17:09:00', '17:41:00', 'TST-2026-025', 7),
    (26, '2026-11-21', 'Pupinel',  'Forfecute dreptaci, pushere inox', 180.00, '18:36:00', '19:36:00', 'TST-2026-026', 3),
    (27, '2026-11-22', 'Autoclav', 'Forfecute cuticule, pushere, bituri conice', 134.00, '17:43:00', '18:15:00', 'TST-2026-027', 8),
    (28, '2026-11-23', 'Pupinel',  'Capete de freza carbid, bituri, pushere', 160.00, '17:18:00', '19:18:00', 'TST-2026-028', 9),
    (29, '2026-11-24', 'Autoclav', 'Pushere, forfecute Staleks, bituri diamantate', 134.00, '18:21:00', '18:53:00', 'TST-2026-029', 10),
    (30, '2026-11-25', 'Pupinel',  'Forfecute cuticule, pushere, bituri flacara', 180.00, '17:39:00', '18:39:00', 'TST-2026-030', 2),
    (31, '2026-11-26', 'Autoclav', 'Capete de freza, bituri, forfecute, pushere', 134.00, '17:29:00', '18:01:00', 'TST-2026-031', 4),
    (32, '2026-11-27', 'Pupinel',  'Forfecute Eugen Tocilarul, bituri diamantate, pushere', 180.00, '18:11:00', '19:11:00', 'TST-2026-032', 5),
    (33, '2026-11-27', 'Autoclav', 'Pushere inox, forfecute stangaci, capete de freza', 134.00, '19:18:00', '19:50:00', 'TST-2026-033', 6),
    (34, '2026-11-28', 'Autoclav', 'Bituri, pushere, forfecute cuticule', 134.00, '17:35:00', '18:07:00', 'TST-2026-034', 7),
    (35, '2026-11-28', 'Pupinel',  'Capete de freza metalice, pushere', 180.00, '18:49:00', '19:49:00', 'TST-2026-035', 8),
    (36, '2026-11-29', 'Autoclav', 'Forfecute, pushere, bituri diamantate si carbid', 134.00, '17:46:00', '18:18:00', 'TST-2026-036', 9),
    (37, '2026-11-30', 'Pupinel',  'Forfecute Staleks, pushere inox, bituri', 180.00, '17:24:00', '18:24:00', 'TST-2026-037', 2),
    (38, '2026-11-30', 'Autoclav', 'Capete de freza, bituri, pushere, forfecute cuticule', 134.00, '18:57:00', '19:29:00', 'TST-2026-038', 10);


-- 18. MISCARI DE STOC (pentru 20_miscari_stoc_responsabili.sql)

INSERT INTO miscare_stoc
    (id_miscare, data, tip_miscare, cantitate, id_angajat, id_stoc_sursa, id_stoc_destinatie)
VALUES
    (1, '2026-11-10 10:15:00', 'Intrare',   5, 2, NULL, 92),
    (2, '2026-11-12 14:20:00', 'Consum',    2, 5, 91, NULL),
    (3, '2026-11-14 11:05:00', 'Eliminare', 1, 8, 85, NULL),
    (4, '2026-11-18 09:15:00', 'Transfer',  1, 4, 2, 1),
    (5, '2026-11-19 11:40:00', 'Transfer',  1, 7, 12, 11),
    (6, '2026-11-20 16:30:00', 'Consum',    1, 6, 3, NULL);

COMMIT;