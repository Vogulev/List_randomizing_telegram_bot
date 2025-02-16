-- ensure that the table with this name is removed before creating a new one.
DROP TABLE IF EXISTS pb_cities;

-- Create user table
CREATE TABLE pb_cities
(
    id bigserial primary key,
    name_rus varchar unique,
    name_eng varchar unique
);

INSERT INTO pb_cities (name_rus, name_eng) VALUES ('Санкт-Петербург', 'Saint-Petersburg');
INSERT INTO pb_cities (name_rus, name_eng) VALUES ('Москва', 'Moscow');
INSERT INTO pb_cities (name_rus, name_eng) VALUES ('Самара', 'Samara');
INSERT INTO pb_cities (name_rus, name_eng) VALUES ('Казань', 'Kazan');
INSERT INTO pb_cities (name_rus, name_eng) VALUES ('Екатеринбург', 'Ekaterinburg');