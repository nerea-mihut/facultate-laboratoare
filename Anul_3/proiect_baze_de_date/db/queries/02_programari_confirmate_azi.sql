USE proiect_salon;

-- Afisarea programarilor confirmate din ziua curenta.

SELECT
    id_programare,
    id_client,
    id_tehnician,
    data,
    ora,
    stare
FROM programare
WHERE data = CURDATE()
  AND stare = 'Confirmata'
ORDER BY ora ASC;