USE proiect_salon;

--Pretul total si durata totala pentru fiecare programar

SELECT
    p.id_programare,
    c.nume AS nume_client,
    c.prenume AS prenume_client,
    p.data,
    p.ora,
    p.stare,
    COUNT(ps.id_serviciu) AS numar_servicii,
    COALESCE(SUM(ps.pret_aplicat), 0) AS pret_total,
    COALESCE(SUM(ps.durata_aplicata), 0) AS durata_totala_minute
FROM programare AS p
INNER JOIN client AS c
    ON p.id_client = c.id_client
LEFT JOIN programare_serviciu AS ps
    ON p.id_programare = ps.id_programare
GROUP BY
    p.id_programare,
    c.nume,
    c.prenume,
    p.data,
    p.ora,
    p.stare
ORDER BY
    p.data ASC,
    p.ora ASC,
    p.id_programare ASC;