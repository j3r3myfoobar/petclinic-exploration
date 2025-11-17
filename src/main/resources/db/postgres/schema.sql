CREATE TABLE IF NOT EXISTS vets (
  id         UUID NOT NULL PRIMARY KEY,
  first_name TEXT,
  last_name  TEXT
);
CREATE INDEX ON vets (last_name);

CREATE TABLE IF NOT EXISTS specialties (
  id   UUID NOT NULL PRIMARY KEY,
  name TEXT
);
CREATE INDEX ON specialties (name);

CREATE TABLE IF NOT EXISTS vet_specialties (
  vet_id       UUID NOT NULL REFERENCES vets (id),
  specialty_id UUID NOT NULL REFERENCES specialties (id),
  UNIQUE (vet_id, specialty_id)
);

CREATE TABLE IF NOT EXISTS types (
  id   UUID NOT NULL PRIMARY KEY,
  name TEXT
);
CREATE INDEX ON types (name);

CREATE TABLE IF NOT EXISTS owners (
  id         UUID NOT NULL PRIMARY KEY,
  first_name TEXT,
  last_name  TEXT,
  address    TEXT,
  city       TEXT,
  telephone  TEXT
);
CREATE INDEX ON owners (last_name);

CREATE TABLE IF NOT EXISTS pets (
  id         UUID NOT NULL PRIMARY KEY,
  name       TEXT,
  birth_date DATE,
  type_id    UUID NOT NULL REFERENCES types (id),
  owner_id   UUID REFERENCES owners (id)
);
CREATE INDEX ON pets (name);
CREATE INDEX ON pets (owner_id);

CREATE TABLE IF NOT EXISTS visits (
  id          UUID NOT NULL PRIMARY KEY,
  pet_id      UUID REFERENCES pets (id),
  visit_date  DATE,
  description TEXT
);
CREATE INDEX ON visits (pet_id);
