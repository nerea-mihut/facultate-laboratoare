USE proiect_salon;

-- Platile pentru inscrieri, cu numele clientului si cursul.

SELECT
    p.id_plata,
    p.id_inscriere,
    cl.nume AS nume_client,
    cl.prenume AS prenume_client,
    c.denumire AS denumire_curs,
    sc.id_sesiune,
    p.data AS data_platii,
    p.suma,
    p.tip AS tip_plata,
    p.moneda
FROM plata AS p
INNER JOIN inscriere AS i
    ON p.id_inscriere = i.id_inscriere
INNER JOIN client AS cl
    ON i.id_client = cl.id_client
INNER JOIN sesiune_curs AS sc
    ON i.id_sesiune = sc.id_sesiune
INNER JOIN curs AS c
    ON sc.id_curs = c.id_curs
ORDER BY p.data DESC, p.id_plata DESC;