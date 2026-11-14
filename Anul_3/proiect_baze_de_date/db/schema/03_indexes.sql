USE proiect_salon;


-- INDEXURI PENTRU ACTIVITATEA SALONULUI

CREATE INDEX idx_programare_client
    ON programare (id_client);

CREATE INDEX idx_programare_tehnician
    ON programare (id_tehnician);

CREATE INDEX idx_programare_data
    ON programare (data);

CREATE INDEX idx_programare_stare
    ON programare (stare);

CREATE INDEX idx_programare_data_ora
    ON programare (data, ora);

CREATE INDEX idx_feedback_programare
    ON feedback (id_programare);

CREATE INDEX idx_interval_disponibilitate_angajat
    ON interval_disponibilitate (id_angajat);

CREATE INDEX idx_interval_disponibilitate_inceput
    ON interval_disponibilitate (inceput);

CREATE INDEX idx_tehnician_serviciu_serviciu
    ON tehnician_serviciu (id_serviciu);


-- INDEXURI PENTRU ACTIVITATEA ACADEMIEI

CREATE INDEX idx_sesiune_curs_curs
    ON sesiune_curs (id_curs);

CREATE INDEX idx_sesiune_curs_trainer
    ON sesiune_curs (id_trainer);

CREATE INDEX idx_sesiune_curs_stare
    ON sesiune_curs (stare);

CREATE INDEX idx_interval_sesiune_sesiune
    ON interval_sesiune (id_sesiune);

CREATE INDEX idx_interval_sesiune_inceput
    ON interval_sesiune (inceput);

CREATE INDEX idx_inscriere_client
    ON inscriere (id_client);

CREATE INDEX idx_inscriere_sesiune
    ON inscriere (id_sesiune);

CREATE INDEX idx_inscriere_stare
    ON inscriere (stare);

CREATE INDEX idx_plata_inscriere
    ON plata (id_inscriere);

CREATE INDEX idx_plata_data
    ON plata (data);

CREATE INDEX idx_document_inscriere
    ON document_inscriere (id_inscriere);

CREATE INDEX idx_lista_asteptare_data
    ON lista_asteptare (data_adaugarii);


-- INDEXURI PENTRU GESTIUNEA STOCURILOR

CREATE INDEX idx_stoc_produs
    ON stoc (id_produs);

CREATE INDEX idx_stoc_destinatie
    ON stoc (destinatie);

CREATE INDEX idx_miscare_stoc_angajat
    ON miscare_stoc (id_angajat);

CREATE INDEX idx_miscare_stoc_sursa
    ON miscare_stoc (id_stoc_sursa);

CREATE INDEX idx_miscare_stoc_destinatie
    ON miscare_stoc (id_stoc_destinatie);

CREATE INDEX idx_miscare_stoc_tip
    ON miscare_stoc (tip_miscare);

CREATE INDEX idx_miscare_stoc_data
    ON miscare_stoc (data);


-- INDEXURI PENTRU REGISTRUL DE STERILIZARE

CREATE INDEX idx_registru_sterilizare_angajat
    ON registru_sterilizare (id_angajat);

CREATE INDEX idx_registru_sterilizare_data
    ON registru_sterilizare (data_sterilizarii);