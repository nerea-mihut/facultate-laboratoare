USE proiect_salon;

--Listele de asteptare pentru sesiunile de curs.

SELECT
    sc.id_sesiune,
    c.denumire AS denumire_curs,
    cl.id_client,
    cl.nume AS nume_client,
    cl.prenume AS prenume_client,
    cl.telefon,
    cl.email,
    la.data_adaugarii
FROM lista_asteptare AS la
INNER JOIN client AS cl
    ON la.id_client = cl.id_client
INNER JOIN sesiune_curs AS sc
    ON la.id_sesiune = sc.id_sesiune
INNER JOIN curs AS c
    ON sc.id_curs = c.id_curs
ORDER BY
    sc.id_sesiune ASC,
    la.data_adaugarii ASC,
    cl.id_client ASC;