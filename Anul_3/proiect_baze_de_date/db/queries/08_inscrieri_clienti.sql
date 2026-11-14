USE proiect_salon;

--Inscrierile clientilor la sesiunile de curs.

SELECT
    i.id_inscriere,
    cl.nume AS nume_client,
    cl.prenume AS prenume_client,
    c.denumire AS denumire_curs,
    sc.id_sesiune,
    i.data_inscrierii,
    i.stare AS stare_inscriere,
    i.pret_aplicat
FROM inscriere AS i
INNER JOIN client AS cl
    ON i.id_client = cl.id_client
INNER JOIN sesiune_curs AS sc
    ON i.id_sesiune = sc.id_sesiune
INNER JOIN curs AS c
    ON sc.id_curs = c.id_curs
ORDER BY i.data_inscrierii DESC, i.id_inscriere DESC;