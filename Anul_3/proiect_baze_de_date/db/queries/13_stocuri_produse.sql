USE proiect_salon;

--Stocurile produselor, separat pentru salon si academie

SELECT
    s.id_stoc,
    p.cod_produs,
    p.nume AS nume_produs,
    p.categorie,
    p.unitate_masura,
    p.tip_formula,
    s.destinatie,
    s.cantitate_disponibila,
    s.prag_minim
FROM stoc AS s
INNER JOIN produs AS p
    ON s.id_produs = p.id_produs
ORDER BY
    s.destinatie ASC,
    p.nume ASC,
    s.id_stoc ASC;