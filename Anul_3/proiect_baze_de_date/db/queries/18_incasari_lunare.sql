USE proiect_salon;

-- Incasarile lunare din platile pentru cursuri. Monedele sunt grupate separat

SELECT
    YEAR(p.data) AS an,
    MONTH(p.data) AS luna,
    p.moneda,
    COUNT(p.id_plata) AS numar_plati,
    SUM(p.suma) AS total_incasat
FROM plata AS p
GROUP BY
    YEAR(p.data),
    MONTH(p.data),
    p.moneda
ORDER BY
    an DESC,
    luna DESC,
    p.moneda ASC;