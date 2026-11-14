USE proiect_salon;

-- 06. Serviciile incluse in fiecare programare.

SELECT
    p.id_programare,
    p.data,
    p.ora,
    c.nume AS nume_client,
    c.prenume AS prenume_client,
    s.denumire AS serviciu,
    ps.pret_aplicat,
    ps.durata_aplicata AS durata_minute
FROM programare_serviciu AS ps
INNER JOIN programare AS p
    ON ps.id_programare = p.id_programare
INNER JOIN client AS c
    ON p.id_client = c.id_client
INNER JOIN serviciu AS s
    ON ps.id_serviciu = s.id_serviciu
ORDER BY
    p.data ASC,
    p.ora ASC,
    p.id_programare ASC,
    s.denumire ASC;