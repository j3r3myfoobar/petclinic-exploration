CREATE TABLE IF NOT EXISTS vets
(
    id
    BINARY
(
    16
) NOT NULL PRIMARY KEY,
    first_name VARCHAR
(
    30
),
    last_name VARCHAR
(
    30
),
    INDEX
(
    last_name
)
    ) engine=InnoDB;

CREATE TABLE IF NOT EXISTS specialties
(
    id
    BINARY
(
    16
) NOT NULL PRIMARY KEY,
    name VARCHAR
(
    80
),
    INDEX
(
    name
)
    ) engine=InnoDB;

CREATE TABLE IF NOT EXISTS vet_specialties
(
    vet_id
    BINARY
(
    16
) NOT NULL,
    specialty_id BINARY
(
    16
) NOT NULL,
    FOREIGN KEY
(
    vet_id
) REFERENCES vets
(
    id
),
    FOREIGN KEY
(
    specialty_id
) REFERENCES specialties
(
    id
),
    UNIQUE
(
    vet_id,
    specialty_id
)
    ) engine=InnoDB;

CREATE TABLE IF NOT EXISTS types
(
    id
    BINARY
(
    16
) NOT NULL PRIMARY KEY,
    name VARCHAR
(
    80
),
    INDEX
(
    name
)
    ) engine=InnoDB;

CREATE TABLE IF NOT EXISTS owners
(
    id
    BINARY
(
    16
) NOT NULL PRIMARY KEY,
    first_name VARCHAR
(
    30
),
    last_name VARCHAR
(
    30
),
    address VARCHAR
(
    255
),
    city VARCHAR
(
    80
),
    telephone VARCHAR
(
    20
),
    INDEX
(
    last_name
)
    ) engine=InnoDB;

CREATE TABLE IF NOT EXISTS pets
(
    id
    BINARY
(
    16
) NOT NULL PRIMARY KEY,
    name VARCHAR
(
    30
),
    birth_date DATE,
    type_id BINARY
(
    16
) NOT NULL,
    owner_id BINARY
(
    16
),
    INDEX
(
    name
),
    FOREIGN KEY
(
    owner_id
) REFERENCES owners
(
    id
),
    FOREIGN KEY
(
    type_id
) REFERENCES types
(
    id
)
    ) engine=InnoDB;

CREATE TABLE IF NOT EXISTS visits
(
    id
    BINARY
(
    16
) NOT NULL PRIMARY KEY,
    pet_id BINARY
(
    16
),
    visit_date DATE,
    description VARCHAR
(
    255
),
    FOREIGN KEY
(
    pet_id
) REFERENCES pets
(
    id
)
    ) engine=InnoDB;
