USE proiect_salon;

-- Clientii care au cel putin doua programari.sunt incluse programarile din toate starile

SELECT
    c.id_client,
    c.nume,
    c.prenume,
    COUNT(p.id_programare) AS numar_programari
FROM client AS c
INNER JOIN programare AS p
    ON c.id_client = p.id_client
GROUP BY
    c.id_client,
    c.nume,
    c.prenume
HAVING COUNT(p.id_programare) >= 2
ORDER BY
    numar_programari DESC,
    c.nume ASC,
    c.prenume ASC,
    c.id_client ASC;