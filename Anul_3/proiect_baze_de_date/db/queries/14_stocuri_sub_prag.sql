USE proiect_salon;

--Pozitiile de stoc cu o cantitate sub pragul minim

SELECT
    s.id_stoc,
    p.cod_produs,
    p.nume AS nume_produs,
    s.destinatie,
    p.unitate_masura,
    s.cantitate_disponibila,
    s.prag_minim,
    s.prag_minim - s.cantitate_disponibila AS deficit_pana_la_prag
FROM stoc AS s
INNER JOIN produs AS p
    ON s.id_produs = p.id_produs
WHERE s.cantitate_disponibila < s.prag_minim
ORDER BY
    s.destinatie ASC,
    p.nume ASC,
    s.id_stoc ASC;