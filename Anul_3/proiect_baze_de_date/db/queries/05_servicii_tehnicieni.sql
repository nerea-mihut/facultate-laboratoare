USE proiect_salon;

-- 05. Serviciile oferite activ de fiecare tehnician.

SELECT
    t.id_angajat AS id_tehnician,
    a.nume AS nume_tehnician,
    a.prenume AS prenume_tehnician,
    s.denumire AS serviciu,
    s.categorie,
    ts.durata AS durata_minute,
    ts.pret,
    ts.stare_oferta
FROM tehnician_serviciu AS ts
INNER JOIN tehnician AS t
    ON ts.id_tehnician = t.id_angajat
INNER JOIN angajat AS a
    ON t.id_angajat = a.id_angajat
INNER JOIN serviciu AS s
    ON ts.id_serviciu = s.id_serviciu
WHERE ts.stare_oferta = 'Activa'
ORDER BY a.nume ASC, a.prenume ASC, s.denumire ASC;