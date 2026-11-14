
-- Date de test pentru proiect_salon

USE proiect_salon;

START TRANSACTION;


-- 1. ANGAJATI


INSERT INTO angajat
    (id_angajat, nume, prenume)
VALUES
    (1, 'Mihut', 'Nerea'),
    (2, 'Berinde', 'Bianca'),
    (3, 'Velasco', 'Sara'),
    (4, 'Munteanu', 'Ioana'),
    (5, 'Radu', 'Ana-Maria'),
    (6, 'Neagu', 'Bianca'),
    (7, 'Petrescu', 'Camelia'),
    (8, 'Marin', 'Valentina'),
    (9, 'Fernandez', 'Lucia'),
    (10, 'Moreau', 'Camille');


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

INSERT INTO client
    (id_client, nume, prenume, telefon, email)
VALUES
    (1, 'Popa', 'Andrei', '0700000001', 'andrei.popa@test.ro'),
    (2, 'Iordache', 'Mihai', '0700000002', 'mihai.iordache@test.ro'),
    (3, 'Dumitru', 'Ana', '0700000003', 'ana.dumitru@test.ro'),
    (4, 'Vasilescu', 'Elena', '0700000004', 'elena.vasilescu@test.ro'),
    (5, 'Munteanu', 'Ioana', '0700000005', 'ioana.munteanu@test.ro'),
    (6, 'Radu', 'Maria', '0700000006', 'maria.radu@test.ro'),
    (7, 'Neagu', 'Bianca', '0700000007', 'bianca.neagu@test.ro'),
    (8, 'Petrescu', 'Camelia', '0700000008', 'camelia.petrescu@test.ro'),
    (9, 'Marin', 'Valentina', '0700000009', 'valentina.marin@test.ro'),
    (10, 'Ilie', 'Sofia', '0700000010', 'sofia.ilie@test.ro'),
    (11, 'Stan', 'Diana', '0700000011', 'diana.stan@test.ro'),
    (12, 'Barbu', 'Raluca', '0700000012', 'raluca.barbu@test.ro'),
    (13, 'Toma', 'Alexandra', '0700000013', 'alexandra.toma@test.ro'),
    (14, 'Nistor', 'Cristina', '0700000014', 'cristina.nistor@test.ro'),
    (15, 'Mihai', 'Laura', '0700000015', 'laura.mihai@test.ro'),
    (16, 'Enache', 'Paula', '0700000016', 'paula.enache@test.ro'),
    (17, 'Vlad', 'Denisa', '0700000017', 'denisa.vlad@test.ro'),
    (18, 'Calin', 'Andreea', '0700000018', 'andreea.calin@test.ro'),
    (19, 'Marin', 'Gabriela', '0700000019', 'gabriela.marin@test.ro'),
    (20, 'Dobre', 'Roxana', '0700000020', 'roxana.dobre@test.ro'),
    (21, 'Fernandez', 'Lucia', '0700000021', 'lucia.fernandez@test.ro'),
    (22, 'Lopez', 'Marta', '0700000022', 'marta.lopez@test.ro'),
    (23, 'Moreau', 'Camille', '0700000023', 'camille.moreau@test.ro'),
    (24, 'Laurent', 'Elodie', '0700000024', 'elodie.laurent@test.ro'),
    (25, 'Rossi', 'Valentina', '0700000025', 'valentina.rossi@test.ro'),
    (26, 'Ferrari', 'Giulia', '0700000026', 'giulia.ferrari@test.ro'),
    (27, 'Garcia', 'Mateo', '0700000027', 'mateo.garcia@test.ro'),
    (28, 'Munteanu', 'Adrian', '0700000028', 'adrian.munteanu@test.ro'),
    (29, 'Georgescu', 'Irina', '0700000029', 'irina.georgescu@test.ro'),
    (30, 'Petrescu', 'Alina', '0700000030', 'alina.petrescu@test.ro');


-- 5. PRODUSE Stocul este numarat in ambalaje, nu in ml/g.

INSERT INTO produs
    (id_produs, cod_produs, nume, categorie, unitate_masura, tip_formula)
VALUES
    (1, 'P001', 'Liquid Builder Gel Light Nude 40 ml ONE', 'Geluri si baze', 'buc', 'Liquid builder gel'),
    (2, 'P002', 'Liquid Builder Gel Light Natural 40 ml ONE', 'Geluri si baze', 'buc', 'Liquid builder gel'),
    (3, 'P003', 'Liquid Builder Gel Light Pink 40 ml ONE', 'Geluri si baze', 'buc', 'Liquid builder gel'),
    (4, 'P004', 'Liquid Builder Gel Soft Rose 40 ml ONE', 'Geluri si baze', 'buc', 'Liquid builder gel'),
    (5, 'P005', 'Liquid Builder Gel Natural 40 ml ONE', 'Geluri si baze', 'buc', 'Liquid builder gel'),
    (6, 'P006', 'Liquid Builder Gel White 40 ml ONE', 'Geluri si baze', 'buc', 'Liquid builder gel'),
    (7, 'P007', 'Liquid Builder Gel Milky White 40 ml ONE', 'Geluri si baze', 'buc', 'Liquid builder gel'),
    (8, 'P008', 'Liquid Builder Gel Nude Pink Shimmer 40 ml ONE', 'Geluri si baze', 'buc', 'Liquid builder gel'),
    (9, 'P009', 'Liquid Builder Gel White Blue Shimmer 40 ml ONE', 'Geluri si baze', 'buc', 'Liquid builder gel'),
    (10, 'P010', 'Liquid Builder Gel Nude Gold Shimmer 40 ml ONE', 'Geluri si baze', 'buc', 'Liquid builder gel'),
    (11, 'P011', 'Polygel Black 30 g ONE', 'Geluri si baze', 'buc', 'Polygel'),
    (12, 'P012', 'Polygel Light Nude 30 g ONE', 'Geluri si baze', 'buc', 'Polygel'),
    (13, 'P013', 'Polygel White 30 g ONE', 'Geluri si baze', 'buc', 'Polygel'),
    (14, 'P014', 'Polygel Milky White 30 g ONE', 'Geluri si baze', 'buc', 'Polygel'),
    (15, 'P015', 'Polygel Mint 30 g ONE', 'Geluri si baze', 'buc', 'Polygel'),
    (16, 'P016', 'Polygel Pink 30 g ONE', 'Geluri si baze', 'buc', 'Polygel'),
    (17, 'P017', 'Acrygel Nude 60 g ONE', 'Geluri si baze', 'buc', 'Acrygel'),
    (18, 'P018', 'Acrygel Natural 60 g ONE', 'Geluri si baze', 'buc', 'Acrygel'),
    (19, 'P019', 'Acrygel Maro 60 g ONE', 'Geluri si baze', 'buc', 'Acrygel'),
    (20, 'P020', 'Liquid Polygel Light Nude 15 ml ONE', 'Geluri si baze', 'buc', 'Liquid polygel'),
    (21, 'P021', 'Liquid Polygel Natural 15 ml ONE', 'Geluri si baze', 'buc', 'Liquid polygel'),
    (22, 'P022', 'Rubber Base Light Nude 12 ml ONE', 'Geluri si baze', 'buc', 'Rubber base'),
    (23, 'P023', 'Rubber Base Natural 12 ml ONE', 'Geluri si baze', 'buc', 'Rubber base'),
    (24, 'P024', 'Rubber Base Pink Shimmer 12 ml ONE', 'Geluri si baze', 'buc', 'Rubber base'),
    (25, 'P025', 'Oja Semipermanenta Rosu 13 ml CANNI', 'Nail art system', 'buc', 'Oja semipermanenta'),
    (26, 'P026', 'Oja Semipermanenta Negru 13 ml CANNI', 'Nail art system', 'buc', 'Oja semipermanenta'),
    (27, 'P027', 'Oja Semipermanenta Cat Eye Albastru 13 ml YAS', 'Nail art system', 'buc', 'Oja semipermanenta'),
    (28, 'P028', 'Oja Semipermanenta Metallic Argintiu 13 ml CORIA', 'Nail art system', 'buc', 'Oja semipermanenta'),
    (29, 'P029', 'Spider Gel Argintiu 5 ml CANNI', 'Nail art system', 'buc', 'Spider gel'),
    (30, 'P030', 'Spider Gel Auriu 5 ml YAS', 'Nail art system', 'buc', 'Spider gel'),
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
    (51, 'P051', 'Cleaner Tiramisu ONE', 'Solutii pregatire', 'buc', NULL),
    (52, 'P052', 'Dezinfectant Instrumentar Barbicid', 'Dezinfectanti', 'buc', NULL),
    (53, 'P053', 'Dezinfectant Suprafete Microzid', 'Dezinfectanti', 'buc', NULL),
    (54, 'P054', 'Top Coat Glossy', 'Solutii pregatire', 'buc', NULL),
    (55, 'P055', 'Top Coat Matte', 'Solutii pregatire', 'buc', NULL),
    (56, 'P056', 'Nail Prep', 'Solutii pregatire', 'buc', NULL),
    (57, 'P057', 'Base Coat', 'Solutii pregatire', 'buc', NULL);
-- 6. STOCURI. Salon si Academie sunt stocuri separate. Cantitatea reprezinta numarul de ambalaje


INSERT INTO stoc
    (id_stoc, id_produs, destinatie, cantitate_disponibila, prag_minim)
VALUES
    (1, 1, 'Salon', 3, 1),
    (2, 1, 'Academie', 5, 2),
    (3, 2, 'Salon', 2, 1),
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
    (15, 8, 'Salon', 2, 1),
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
    (37, 19, 'Salon', 1, 1),
    (38, 19, 'Academie', 2, 1),
    (39, 20, 'Salon', 3, 1),
    (40, 20, 'Academie', 4, 2),
    (41, 21, 'Salon', 3, 1),
    (42, 21, 'Academie', 4, 2),
    (43, 22, 'Salon', 4, 2),
    (44, 22, 'Academie', 6, 2),
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
    (85, 43, 'Salon', 1, 1),
    (86, 43, 'Academie', 2, 1),
    (87, 44, 'Salon', 2, 1),
    (88, 44, 'Academie', 2, 1),
    (89, 45, 'Salon', 1, 1),
    (90, 45, 'Academie', 2, 1),
    (91, 46, 'Salon', 10, 3),
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

-- 11. PROGRAMARI
-- Bianca Berinde are mai multe programari.
-- Sara Velasco are mai putine programari.
-- Nerea Mihut nu are programari.

INSERT INTO programare
    (id_programare, id_client, id_tehnician, data, ora, stare)
VALUES
    (1, 3, 2, '2026-11-20', '10:00:00', 'Confirmata'),
    (2, 4, 2, '2026-11-20', '12:00:00', 'Confirmata'),
    (3, 5, 4, '2026-11-21', '09:00:00', 'Confirmata'),
    (4, 6, 4, '2026-11-21', '11:00:00', 'Confirmata'),
    (5, 7, 3, '2026-11-22', '10:00:00', 'Confirmata'),
    (6, 8, 5, '2026-11-22', '14:00:00', 'Confirmata'),
    (7, 1, 6, '2026-11-23', '10:00:00', 'Confirmata'),
    (8, 2, 6, '2026-11-23', '11:00:00', 'Confirmata'),
    (9, 9, 7, '2026-11-24', '10:00:00', 'Confirmata'),
    (10, 10, 8, '2026-11-24', '13:00:00', 'Confirmata'),
    (11, 11, 9, '2026-11-25', '10:00:00', 'Confirmata'),
    (12, 12, 10, '2026-11-25', '14:00:00', 'Confirmata'),
    (13, 13, 2, '2026-11-26', '10:00:00', 'Confirmata'),
    (14, 14, 4, '2026-11-26', '13:00:00', 'Confirmata'),
    (15, 15, 5, '2026-11-27', '10:00:00', 'Confirmata'),
    (16, 16, 6, '2026-11-27', '13:00:00', 'Confirmata'),
    (17, 17, 7, '2026-11-28', '10:00:00', 'Confirmata'),
    (18, 18, 8, '2026-11-28', '14:00:00', 'Confirmata'),
    (19, 19, 9, '2026-11-29', '10:00:00', 'Confirmata'),
    (20, 20, 10, '2026-11-29', '14:00:00', 'Confirmata'),
    (21, 21, 2, '2026-11-30', '10:00:00', 'Programata'),
    (22, 22, 3, '2026-11-30', '14:00:00', 'Programata'),
    (23, 23, 4, '2026-12-01', '10:00:00', 'Programata'),
    (24, 24, 5, '2026-12-01', '14:00:00', 'Programata'),
    (25, 25, 6, '2026-12-02', '10:00:00', 'Programata'),
    (26, 26, 7, '2026-12-02', '14:00:00', 'Programata'),
    (27, 27, 8, '2026-12-03', '10:00:00', 'Programata'),
    (28, 28, 9, '2026-12-03', '14:00:00', 'Programata'),
    (29, 3, 4, '2026-12-05', '10:00:00', 'Confirmata'),
    (30, 5, 5, '2026-12-05', '14:00:00', 'Confirmata');


-- 12. SERVICII DIN PROGRAMARI

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
    (30, 6, 140.00, 75);

-- 13. INSCRIERI LA CURSURI

INSERT INTO inscriere
    (id_inscriere, id_client, id_sesiune, data_inscrierii, stare, pret_aplicat, data_anuntarii_retragerii, id_inscriere_inlocuita)
VALUES
    (1, 6, 1, '2026-11-10', 'Confirmata', 2500.00, NULL, NULL),
    (2, 10, 3, '2026-11-11', 'Confirmata', 2500.00, NULL, NULL),
    (3, 14, 4, '2026-11-12', 'Confirmata', 2800.00, NULL, NULL),
    (4, 18, 6, '2026-11-13', 'Confirmata', 4000.00, NULL, NULL);

-- 14. PLATI PENTRU INSCRIERI

INSERT INTO plata
    (id_plata, id_inscriere, data, suma, tip, moneda)
VALUES
    (1, 1, '2026-11-10', 500.00, 'Avans', 'RON'),
    (2, 2, '2026-11-11', 500.00, 'Avans', 'RON'),
    (3, 3, '2026-11-12', 700.00, 'Avans', 'RON'),
    (4, 4, '2026-11-13', 1000.00, 'Avans', 'RON');

-- 15. LISTE DE ASTEPTARE
 

INSERT INTO lista_asteptare
    (id_client, id_sesiune, data_adaugarii)
VALUES
    (25, 1, '2026-11-12'),
    (26, 3, '2026-11-13');

-- 16. REGISTRU STERILIZARE

INSERT INTO registru_sterilizare
    (id_inregistrare, data_sterilizarii, aparat, material_sterilizat, temperatura, ora_inceperii, ora_terminarii, referinta_test, id_angajat)
VALUES
    (1, '2026-11-20', 'Autoclava 1', 'Clești, pushere', 134, '09:00:00', '09:30:00', 'TST-2026-001', 4),
    (2, '2026-11-21', 'Autoclava 1', 'Bituri, pensule', 134, '17:00:00', '17:30:00', 'TST-2026-002', 7);

-- 17. MISCARI DE STOC


INSERT INTO miscare_stoc
    (id_miscare, data, tip_miscare, cantitate, id_angajat, id_stoc_sursa, id_stoc_destinatie)
VALUES
    (1, '2026-11-18', 'Transfer', 1, 4, 2, 1),
    (2, '2026-11-19', 'Transfer', 1, 7, 12, 11);

COMMIT;