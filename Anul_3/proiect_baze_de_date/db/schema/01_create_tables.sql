USE proiect_salon;

CREATE TABLE angajat (
    id_angajat INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nume VARCHAR(50) NOT NULL,
    prenume VARCHAR(50) NOT NULL,
    telefon VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    specializare VARCHAR(100) NULL,
    certificari TEXT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE client (
    id_client INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nume VARCHAR(50) NOT NULL,
    prenume VARCHAR(50) NOT NULL,
    telefon VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    alergii ENUM('Da', 'Nu') NOT NULL,
    detalii_alergii TEXT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE tehnician (
    id_angajat INT UNSIGNED PRIMARY KEY
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE trainer (
    id_angajat INT UNSIGNED PRIMARY KEY
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE serviciu (
    id_serviciu INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    denumire VARCHAR(100) NOT NULL,
    categorie VARCHAR(50) NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE curs (
    id_curs INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    denumire VARCHAR(100) NOT NULL,
    nivel VARCHAR(50) NOT NULL,
    complexitate VARCHAR(100) NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE produs (
    id_produs INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cod_produs VARCHAR(50) NOT NULL,
    nume VARCHAR(100) NOT NULL,
    categorie VARCHAR(50) NOT NULL,
    unitate_masura VARCHAR(20) NOT NULL,
    tip_formula ENUM('Clasic', 'Hipoalergenic') NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE interval_disponibilitate (
    id_interval INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_angajat INT UNSIGNED NOT NULL,
    inceput DATETIME NOT NULL,
    sfarsit DATETIME NOT NULL,
    tip_interval ENUM('Lucru', 'Absenta') NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE programare (
    id_programare INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_client INT UNSIGNED NOT NULL,
    id_tehnician INT UNSIGNED NOT NULL,
    data DATE NOT NULL,
    ora TIME NOT NULL,
    stare ENUM(
        'Confirmata',
        'Finalizata',
        'Anulata',
        'Neprezentare'
    ) NOT NULL DEFAULT 'Confirmata',
    motiv_anulare TEXT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE sesiune_curs (
    id_sesiune INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_curs INT UNSIGNED NOT NULL,
    id_trainer INT UNSIGNED NOT NULL,
    pret DECIMAL(10,2) NOT NULL,
    nr_maxim_cursanti TINYINT UNSIGNED NOT NULL DEFAULT 8,
    stare ENUM(
        'Planificata',
        'In_desfasurare',
        'Finalizata',
        'Anulata'
    ) NOT NULL DEFAULT 'Planificata'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE interval_sesiune (
    id_interval INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_sesiune INT UNSIGNED NOT NULL,
    inceput DATETIME NOT NULL,
    sfarsit DATETIME NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE inscriere (
    id_inscriere INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_client INT UNSIGNED NOT NULL,
    id_sesiune INT UNSIGNED NOT NULL,
    data_inscrierii DATE NOT NULL,
    stare ENUM(
        'In_asteptare',
        'Confirmata',
        'In_curs',
        'Finalizata',
        'Retrasa',
        'Inlocuita'
    ) NOT NULL DEFAULT 'In_asteptare',
    pret_aplicat DECIMAL(10,2) NOT NULL,
    data_anuntarii_retragerii DATE NULL,
    id_inscriere_inlocuita INT UNSIGNED NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE plata (
    id_plata INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_inscriere INT UNSIGNED NOT NULL,
    data DATE NOT NULL,
    suma DECIMAL(10,2) NOT NULL,
    tip ENUM('Avans', 'Plata_ulterioara') NOT NULL,
    moneda CHAR(3) NOT NULL DEFAULT 'RON'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE document_inscriere (
    id_document INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_inscriere INT UNSIGNED NOT NULL,
    tip_document VARCHAR(100) NOT NULL,
    data_depunerii DATE NOT NULL,
    referinta_fisier VARCHAR(255) NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE feedback (
    id_feedback INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_programare INT UNSIGNED NOT NULL,
    evaluare TINYINT UNSIGNED NOT NULL,
    comentariu TEXT NULL,
    data DATE NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE stoc (
    id_stoc INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_produs INT UNSIGNED NOT NULL,
    destinatie ENUM('Salon', 'Academie') NOT NULL,
    cantitate_disponibila DECIMAL(12,3) NOT NULL DEFAULT 0,
    prag_minim DECIMAL(12,3) NOT NULL DEFAULT 0
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE miscare_stoc (
    id_miscare INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tip_miscare ENUM(
        'Intrare',
        'Consum',
        'Transfer',
        'Eliminare'
    ) NOT NULL,
    cantitate DECIMAL(12,3) NOT NULL,
    data DATETIME NOT NULL,
    id_angajat INT UNSIGNED NOT NULL,
    id_stoc_sursa INT UNSIGNED NULL,
    id_stoc_destinatie INT UNSIGNED NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE registru_sterilizare (
    id_inregistrare INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    data_sterilizarii DATE NOT NULL,
    aparat ENUM('Pupinel', 'Autoclav') NOT NULL,
    material_sterilizat TEXT NOT NULL,
    temperatura DECIMAL(5,2) NOT NULL,
    ora_inceperii TIME NOT NULL,
    ora_terminarii TIME NOT NULL,
    referinta_test VARCHAR(255) NULL,
    id_angajat INT UNSIGNED NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE tehnician_serviciu (
    id_tehnician INT UNSIGNED NOT NULL,
    id_serviciu INT UNSIGNED NOT NULL,
    durata INT UNSIGNED NOT NULL,
    pret DECIMAL(10,2) NOT NULL,
    stare_oferta ENUM('Activa', 'Inactiva')
        NOT NULL DEFAULT 'Activa',

    PRIMARY KEY (id_tehnician, id_serviciu)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE programare_serviciu (
    id_programare INT UNSIGNED NOT NULL,
    id_serviciu INT UNSIGNED NOT NULL,
    pret_aplicat DECIMAL(10,2) NOT NULL,
    durata_aplicata INT UNSIGNED NOT NULL,

    PRIMARY KEY (id_programare, id_serviciu)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE lista_asteptare (
    id_client INT UNSIGNED NOT NULL,
    id_sesiune INT UNSIGNED NOT NULL,
    data_adaugarii DATETIME NOT NULL,

    PRIMARY KEY (id_client, id_sesiune)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;