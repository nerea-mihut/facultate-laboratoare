USE proiect_salon;

-- Afisarea tuturor programarilor in ordine cronologica

SELECT
    id_programare,
    id_client,
    id_tehnician,
    data,
    ora,
    stare
FROM programare
ORDER BY data ASC, ora ASC;