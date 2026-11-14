USE proiect_salon;

-- Totalul achitat si diferenta pentru fiecare inscriere.sumele sunt interpretate in RON

SELECT
    i.id_inscriere,
    c.nume AS nume_client,
    c.prenume AS prenume_client,
    i.stare AS stare_inscriere,
    i.pret_aplicat,
    COALESCE(SUM(p.suma), 0) AS total_achitat_ron,
    i.pret_aplicat - COALESCE(SUM(p.suma), 0) AS diferenta_ron
FROM inscriere AS i
INNER JOIN client AS c
    ON i.id_client = c.id_client
LEFT JOIN plata AS p
    ON i.id_inscriere = p.id_inscriere
    AND p.moneda = 'RON'
GROUP BY
    i.id_inscriere,
    c.nume,
    c.prenume,
    i.stare,
    i.pret_aplicat
ORDER BY i.id_inscriere ASC;