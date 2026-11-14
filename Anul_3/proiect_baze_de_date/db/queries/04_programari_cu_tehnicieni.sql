USE proiect_salon;

-- 04. Programarile cu numele clientului si al tehnicianului.

SELECT
    p.id_programare,
    c.nume AS nume_client,
    c.prenume AS prenume_client,
    a.nume AS nume_tehnician,
    a.prenume AS prenume_tehnician,
    p.data,
    p.ora,
    p.stare
FROM programare AS p
INNER JOIN client AS c
    ON p.id_client = c.id_client
INNER JOIN tehnician AS t
    ON p.id_tehnician = t.id_angajat
INNER JOIN angajat AS a
    ON t.id_angajat = a.id_angajat
ORDER BY p.data ASC, p.ora ASC, p.id_programare ASC;