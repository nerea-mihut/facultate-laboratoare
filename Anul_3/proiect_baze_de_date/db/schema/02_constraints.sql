USE proiect_salon;


-- CHEI STRAINE

ALTER TABLE tehnician
    ADD CONSTRAINT fk_tehnician_angajat
    FOREIGN KEY (id_angajat)
    REFERENCES angajat(id_angajat);

ALTER TABLE trainer
    ADD CONSTRAINT fk_trainer_angajat
    FOREIGN KEY (id_angajat)
    REFERENCES angajat(id_angajat);

ALTER TABLE interval_disponibilitate
    ADD CONSTRAINT fk_disponibilitate_angajat
    FOREIGN KEY (id_angajat)
    REFERENCES angajat(id_angajat);

ALTER TABLE programare
    ADD CONSTRAINT fk_programare_client
    FOREIGN KEY (id_client)
    REFERENCES client(id_client),

    ADD CONSTRAINT fk_programare_tehnician
    FOREIGN KEY (id_tehnician)
    REFERENCES tehnician(id_angajat);

ALTER TABLE sesiune_curs
    ADD CONSTRAINT fk_sesiune_curs
    FOREIGN KEY (id_curs)
    REFERENCES curs(id_curs),

    ADD CONSTRAINT fk_sesiune_trainer
    FOREIGN KEY (id_trainer)
    REFERENCES trainer(id_angajat);

ALTER TABLE interval_sesiune
    ADD CONSTRAINT fk_interval_sesiune
    FOREIGN KEY (id_sesiune)
    REFERENCES sesiune_curs(id_sesiune);

ALTER TABLE inscriere
    ADD CONSTRAINT fk_inscriere_client
    FOREIGN KEY (id_client)
    REFERENCES client(id_client),

    ADD CONSTRAINT fk_inscriere_sesiune
    FOREIGN KEY (id_sesiune)
    REFERENCES sesiune_curs(id_sesiune),

    ADD CONSTRAINT fk_inscriere_inlocuita
    FOREIGN KEY (id_inscriere_inlocuita)
    REFERENCES inscriere(id_inscriere);

ALTER TABLE plata
    ADD CONSTRAINT fk_plata_inscriere
    FOREIGN KEY (id_inscriere)
    REFERENCES inscriere(id_inscriere);

ALTER TABLE document_inscriere
    ADD CONSTRAINT fk_document_inscriere
    FOREIGN KEY (id_inscriere)
    REFERENCES inscriere(id_inscriere);

ALTER TABLE feedback
    ADD CONSTRAINT fk_feedback_programare
    FOREIGN KEY (id_programare)
    REFERENCES programare(id_programare);

ALTER TABLE stoc
    ADD CONSTRAINT fk_stoc_produs
    FOREIGN KEY (id_produs)
    REFERENCES produs(id_produs);

ALTER TABLE miscare_stoc
    ADD CONSTRAINT fk_miscare_angajat
    FOREIGN KEY (id_angajat)
    REFERENCES angajat(id_angajat),

    ADD CONSTRAINT fk_miscare_sursa
    FOREIGN KEY (id_stoc_sursa)
    REFERENCES stoc(id_stoc),

    ADD CONSTRAINT fk_miscare_destinatie
    FOREIGN KEY (id_stoc_destinatie)
    REFERENCES stoc(id_stoc);

ALTER TABLE registru_sterilizare
    ADD CONSTRAINT fk_registru_angajat
    FOREIGN KEY (id_angajat)
    REFERENCES angajat(id_angajat);

ALTER TABLE tehnician_serviciu
    ADD CONSTRAINT fk_oferta_tehnician
    FOREIGN KEY (id_tehnician)
    REFERENCES tehnician(id_angajat),

    ADD CONSTRAINT fk_oferta_serviciu
    FOREIGN KEY (id_serviciu)
    REFERENCES serviciu(id_serviciu);

ALTER TABLE programare_serviciu
    ADD CONSTRAINT fk_detaliu_programare
    FOREIGN KEY (id_programare)
    REFERENCES programare(id_programare),

    ADD CONSTRAINT fk_detaliu_serviciu
    FOREIGN KEY (id_serviciu)
    REFERENCES serviciu(id_serviciu);

ALTER TABLE lista_asteptare
    ADD CONSTRAINT fk_asteptare_client
    FOREIGN KEY (id_client)
    REFERENCES client(id_client),

    ADD CONSTRAINT fk_asteptare_sesiune
    FOREIGN KEY (id_sesiune)
    REFERENCES sesiune_curs(id_sesiune);


-- REGULI DE UNICITATE

ALTER TABLE angajat
    ADD CONSTRAINT uq_angajat_telefon UNIQUE (telefon),
    ADD CONSTRAINT uq_angajat_email UNIQUE (email);

ALTER TABLE client
    ADD CONSTRAINT uq_client_telefon UNIQUE (telefon),
    ADD CONSTRAINT uq_client_email UNIQUE (email);

ALTER TABLE produs
    ADD CONSTRAINT uq_produs_cod UNIQUE (cod_produs);

ALTER TABLE feedback
    ADD CONSTRAINT uq_feedback_programare UNIQUE (id_programare);

ALTER TABLE inscriere
    ADD CONSTRAINT uq_inscriere_client_sesiune
    UNIQUE (id_client, id_sesiune);

ALTER TABLE stoc
    ADD CONSTRAINT uq_stoc_produs_destinatie
    UNIQUE (id_produs, destinatie);


-- VERIFICARI CHECK

ALTER TABLE interval_disponibilitate
    ADD CONSTRAINT chk_disponibilitate_interval
    CHECK (sfarsit > inceput);

ALTER TABLE interval_sesiune
    ADD CONSTRAINT chk_sesiune_interval
    CHECK (sfarsit > inceput);

ALTER TABLE sesiune_curs
    ADD CONSTRAINT chk_sesiune_pret
    CHECK (pret >= 0),

    ADD CONSTRAINT chk_sesiune_capacitate
    CHECK (nr_maxim_cursanti BETWEEN 1 AND 8);

ALTER TABLE inscriere
    ADD CONSTRAINT chk_inscriere_pret
    CHECK (pret_aplicat >= 0);

ALTER TABLE plata
    ADD CONSTRAINT chk_plata_suma
    CHECK (suma > 0);

ALTER TABLE feedback
    ADD CONSTRAINT chk_feedback_evaluare
    CHECK (evaluare BETWEEN 1 AND 5);

ALTER TABLE stoc
    ADD CONSTRAINT chk_stoc_cantitate
    CHECK (cantitate_disponibila >= 0),

    ADD CONSTRAINT chk_stoc_prag
    CHECK (prag_minim >= 0);

ALTER TABLE tehnician_serviciu
    ADD CONSTRAINT chk_oferta_durata
    CHECK (durata > 0),

    ADD CONSTRAINT chk_oferta_pret
    CHECK (pret >= 0);

ALTER TABLE programare_serviciu
    ADD CONSTRAINT chk_programare_serviciu_pret
    CHECK (pret_aplicat >= 0),

    ADD CONSTRAINT chk_programare_serviciu_durata
    CHECK (durata_aplicata > 0);

ALTER TABLE registru_sterilizare
    ADD CONSTRAINT chk_registru_temperatura
    CHECK (temperatura > 0);

ALTER TABLE miscare_stoc
    ADD CONSTRAINT chk_miscare_cantitate
    CHECK (cantitate > 0),

    ADD CONSTRAINT chk_miscare_sursa_destinatie
    CHECK (
        (
            tip_miscare = 'Intrare'
            AND id_stoc_sursa IS NULL
            AND id_stoc_destinatie IS NOT NULL
        )
        OR
        (
            tip_miscare IN ('Consum', 'Eliminare')
            AND id_stoc_sursa IS NOT NULL
            AND id_stoc_destinatie IS NULL
        )
        OR
        (
            tip_miscare = 'Transfer'
            AND id_stoc_sursa IS NOT NULL
            AND id_stoc_destinatie IS NOT NULL
            AND id_stoc_sursa <> id_stoc_destinatie
        )
    );