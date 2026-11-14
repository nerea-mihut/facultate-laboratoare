USE proiect_salon;

--Inregistrarile de sterilizare si angajatii responsabili

SELECT
    rs.id_inregistrare,
    rs.data_sterilizarii,
    rs.aparat,
    rs.material_sterilizat,
    rs.temperatura,
    rs.ora_inceperii,
    rs.ora_terminarii,
    rs.referinta_test,
    a.nume AS nume_responsabil,
    a.prenume AS prenume_responsabil
FROM registru_sterilizare AS rs
INNER JOIN angajat AS a
    ON rs.id_angajat = a.id_angajat
ORDER BY
    rs.data_sterilizarii DESC,
    rs.ora_inceperii DESC,
    rs.id_inregistrare DESC;