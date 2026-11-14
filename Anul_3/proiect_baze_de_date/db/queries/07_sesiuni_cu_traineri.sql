USE proiect_salon;

--Sesiunile de curs impreuna cu numele trainerilor.

SELECT
    sc.id_sesiune,
    c.denumire AS denumire_curs,
    c.nivel,
    a.nume AS nume_trainer,
    a.prenume AS prenume_trainer,
    sc.pret,
    sc.nr_maxim_cursanti,
    sc.stare
FROM sesiune_curs AS sc
INNER JOIN curs AS c
    ON sc.id_curs = c.id_curs
INNER JOIN trainer AS t
    ON sc.id_trainer = t.id_angajat
INNER JOIN angajat AS a
    ON t.id_angajat = a.id_angajat
ORDER BY c.denumire ASC, sc.id_sesiune ASC;