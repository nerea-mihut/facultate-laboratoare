USE proiect_salon;

-- Afisarea programarilor impreuna cu numele clientilor.

SELECT
    p.id_programare,
    c.nume AS nume_client,
    c.prenume AS prenume_client,
    p.data,
    p.ora,
    p.stare
FROM programare AS p
INNER JOIN client AS c
    ON p.id_client = c.id_client
ORDER BY p.data ASC, p.ora ASC;