CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS customer;

CREATE TABLE IF NOT EXISTS customer(
    id serial primary key,
    -- id uuid DEFAULT uuid_generate_v4 () primary key,
    name varchar(255)
);
