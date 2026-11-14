USE proiect_salon;

-- Clientii care nu au nicio programare inregistrata

SELECT
    c.id_client,
    c.nume,
    c.prenume,
    c.telefon,
    c.email
FROM client AS c
WHERE NOT EXISTS (
    SELECT 1
    FROM programare AS p
    WHERE p.id_client = c.id_client
)
ORDER BY
    c.nume ASC,
    c.prenume ASC,
    c.id_client ASC;