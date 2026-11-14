USE proiect_salon;

--Miscarile de stoc cu sursa, destinatia si responsabilul.

SELECT
    ms.id_miscare,
    ms.data AS data_miscarii,
    ms.tip_miscare,
    ms.cantitate,
    ps.nume AS produs_sursa,
    ss.destinatie AS locatie_sursa,
    pd.nume AS produs_destinatie,
    sd.destinatie AS locatie_destinatie,
    a.nume AS nume_responsabil,
    a.prenume AS prenume_responsabil
FROM miscare_stoc AS ms
INNER JOIN angajat AS a
    ON ms.id_angajat = a.id_angajat
LEFT JOIN stoc AS ss
    ON ms.id_stoc_sursa = ss.id_stoc
LEFT JOIN produs AS ps
    ON ss.id_produs = ps.id_produs
LEFT JOIN stoc AS sd
    ON ms.id_stoc_destinatie = sd.id_stoc
LEFT JOIN produs AS pd
    ON sd.id_produs = pd.id_produs
ORDER BY
    ms.data DESC,
    ms.id_miscare DESC;